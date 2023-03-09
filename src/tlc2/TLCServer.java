package tlc2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import tlc2.controlled.protocol.ActionMapper;
import tlc2.controlled.protocol.ActionMapperFactory;
import tlc2.controlled.protocol.ActionWrapper;
import tlc2.output.EC;
import tlc2.output.MP;
import tlc2.tool.Action;
import tlc2.tool.ITool;
import tlc2.tool.StateVec;
import tlc2.tool.TLCState;
import tlc2.tool.impl.FastTool;
import tlc2.tool.impl.Tool;
import tlc2.value.impl.CounterExample;
import tlc2.util.RandomGenerator;
import util.FileUtil;
import util.SimpleFilenameToStream;
import util.Assert.TLCRuntimeException;

public class TLCServer extends TLC {

    private Server server;
    public Thread serverThread;

    private BlockingQueue<String> actionQueue = new ArrayBlockingQueue<String>(1);
    private BlockingQueue<String> stateQueue = new ArrayBlockingQueue<String>(1);

    private StateVec initStates;

    public TLCServer() {
        super();
    }

    @Override
    public boolean handleParameters(String[] args) {
        if (!super.handleParameters(args)) {
            return false;
        }
        int serverPort = 2023;
        int index = 0;
		while (index < args.length) {
            if (args[index].equals("-serverport")) {
                index++;
                if (index < args.length) {
                    try {
                        serverPort = Integer.parseInt(args[index]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            index++;
        }
        this.actionQueue = new ArrayBlockingQueue<String>(1);
        this.stateQueue = new ArrayBlockingQueue<String>(1);
        this.server = new Server(this.actionQueue, this.stateQueue, "q", serverPort);
        this.serverThread = new Thread(server);
        return true;
    }

    @Override
    public int process() {
        boolean quit = false;
        while (!quit) {
            try {
                ITool tool = new FastTool(mainFile, configFile, resolver, Tool.Mode.Simulation, params);
                computeInitStates(tool);
                quit = simulate(tool);    
            } catch (Throwable e) {
                if (e instanceof StackOverflowError)
                {
                    System.gc();
                    return MP.printError(EC.SYSTEM_STACK_OVERFLOW, e);
                } else if (e instanceof OutOfMemoryError)
                {
                    System.gc();
                    return MP.printError(EC.SYSTEM_OUT_OF_MEMORY, e);
                } else if (e instanceof TLCRuntimeException) {
                    return MP.printTLCRuntimeException((TLCRuntimeException) e);
                } else if (e instanceof RuntimeException) 
                {
                    // SZ 29.07.2009 
                    // printing the stack trace of the runtime exceptions
                    return MP.printError(EC.GENERAL, e);
                    // e.printStackTrace();
                } else
                {
                    return MP.printError(EC.GENERAL, e);
                }
            }
        }

        return 0;
    }

    private boolean simulate(ITool tool) {
        ActionMapper mapper = ActionMapperFactory.getMapper(Arrays.asList(tool.getActions()), tool.getRootName());
		Queue<ActionWrapper> actionsToRun = new ArrayDeque<ActionWrapper>();
		List<TLCState> statesVisited = new ArrayList<TLCState>();

        StateVec nextStates = new StateVec(1);
        TLCState curState = randomState(initStates);

        statesVisited.add(curState);
        while(true) {
            try {
                nextStates.clear();
                while(nextStates.empty()) {
                    if (actionsToRun.isEmpty()) {
                        stateQueue.add(statesVisited+"\n");
                        statesVisited.clear();

                        String input = actionQueue.take();
                        actionsToRun.addAll(mapper.mapListOfActions(input));
                    }
                    ActionWrapper nextAction = actionsToRun.remove();
                    if (nextAction.isReset()) {
                        stateQueue.add(statesVisited+"\n");
                        statesVisited.clear();
                        return false;
                    } else if (nextAction.isQuit() || nextAction.action.equals(Action.UNKNOWN)) {
                        stateQueue.add(statesVisited+"\n");
                        statesVisited.clear();
                        return true;
                    } else {
                        nextStates.addElements(tool.getNextStates(nextAction.action, curState));
                        if(nextStates.empty()) {
                            statesVisited.add(curState);
                        }
                    }
                }
                assert(nextStates.size() == 1);
                final TLCState s1 = nextStates.elementAt(0);
                s1.execCallable();
                curState = s1;
                statesVisited.add(curState);

            } catch (Exception e) {
                return false;
            }
        }
    }

    private TLCState randomState(StateVec states) {
        RandomGenerator gen = new RandomGenerator();
        final int len = states.size();
		if (len > 0) {
			final int index = (int) Math.floor(gen.nextDouble() * len);
			return states.elementAt(index);
		}
		return null;
    }

    public int computeInitStates(ITool tool) {
        final int res = tool.checkAssumptions();
		if (res != EC.NO_ERROR) {
			return res;
		}
		
		TLCState curState = null;

		//
		// Compute the initial states.
		//
		try {
			// The init states are calculated only ever once and never change
			// in the loops below. Ideally the variable would be final.
			final StateVec inits = tool.getInitStates();
			initStates = new StateVec(inits.size());
			
            Action[] invariants = tool.getInvariants();
			// Check all initial states for validity.
			for (int i = 0; i < inits.size(); i++) {
				curState = inits.elementAt(i);
				if (tool.isGoodState(curState)) {
					for (int j = 0; j < invariants.length; j++) {
						if (!tool.isValid(invariants[j], curState)) {
							// We get here because of invariant violation.
							int err = MP.printError(EC.TLC_INVARIANT_VIOLATED_INITIAL,
									new String[] { tool.getInvNames()[j], tool.evalAlias(curState, curState).toString() });
							tool.checkPostConditionWithCounterExample(new CounterExample(curState));
							return err;
						}
					}
				} else {
					return MP.printError(EC.TLC_STATE_NOT_COMPLETELY_SPECIFIED_INITIAL, curState.toString());
				}
				
				if (tool.isInModel(curState)) {
					initStates.addElement(curState);
				}
			}
		} catch (Exception e) {
			final int errorCode;
			if (curState != null) {
				errorCode = MP.printError(EC.TLC_INITIAL_STATE,
						new String[] { (e.getMessage() == null) ? e.toString() : e.getMessage(), curState.toString() });
			} else {
				errorCode = MP.printError(EC.GENERAL, e); // LL changed call 7 April 2012
			}
			return errorCode;
		}

		// It appears deepNormalize brings the states into a canonical form to
		// speed up equality checks.
		initStates.deepNormalize();
        return 0;
    }


    public static void main(String[] args) throws Exception {
        final TLCServer tlc = new TLCServer();
        if (!tlc.handleParameters(args)) {
            System.exit(1);
        }
        final String dir = FileUtil.parseDirname(tlc.getMainFile());
        if (!dir.isEmpty()) {
            tlc.setResolver(new SimpleFilenameToStream(dir));
        } else {
            tlc.setResolver(new SimpleFilenameToStream());
        }
        tlc.serverThread.start();

        int errCode = tlc.process();
        System.exit(EC.ExitStatus.errorConstantToExitStatus(errCode));

        // TODO: add interrupt handling
        // TOOD: better logging
        // TODO: define server capabilities
    }

    private class Server implements Runnable {

        private final BlockingQueue<String> actionQueue;
        private final BlockingQueue<String> stateQueue;
        private final String escapeStr;
        private final int port;

        public Server(BlockingQueue<String> actionQueue, BlockingQueue<String> stateQueue, String escapeStr, int port) {
            this.actionQueue = actionQueue;
            this.stateQueue = stateQueue;
            this.escapeStr = escapeStr;
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("Server starts listening on port: "+Integer.toString(port));
            try {
                ServerSocket ss =new ServerSocket(port);
                Socket socket = ss.accept();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String actionStr = "";
                String stateStr = "";
                while(!actionStr.equalsIgnoreCase(escapeStr)) {
                    // send the current state tp the remote process
                    stateStr = stateQueue.take();
                    output.write(stateStr.getBytes(StandardCharsets.UTF_8));

                    // get the next action from the remote process
                    actionStr = reader.readLine();  // reads a single character
                    actionQueue.add(actionStr);
                }

                actionQueue.add(escapeStr);
                System.out.println("Stopping server");
                ss.close();
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
