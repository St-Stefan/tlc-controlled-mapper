package tlc2.controlled;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import tlc2.controlled.protocol.ActionMapper;
import tlc2.controlled.protocol.ActionMapperFactory;
import tlc2.tool.Action;
import tlc2.tool.ITool;
import tlc2.tool.SimulationWorker;
import tlc2.tool.TLCState;
import tlc2.controlled.protocol.ActionWrapper;
import tlc2.tool.liveness.ILiveCheck;

public class ResettableControlledWorker extends ControlledWorker{
    public ResettableControlledWorker(int id, ITool tool, BlockingQueue<SimulationWorker.SimulationWorkerResult> resultQueue,
								long seed, int maxTraceDepth, long maxTraceNum, boolean checkDeadlock, String traceFile,
								ILiveCheck liveCheck) {
		super(id, tool, resultQueue, seed, maxTraceDepth, maxTraceNum, null, checkDeadlock, traceFile, liveCheck,
				new LongAdder(), new AtomicLong(), new AtomicLong());
	}

	public ResettableControlledWorker(int id, ITool tool, BlockingQueue<SimulationWorker.SimulationWorkerResult> resultQueue,
			long seed, int maxTraceDepth, long maxTraceNum, String traceActions, boolean checkDeadlock, String traceFile,
			ILiveCheck liveCheck, LongAdder numOfGenStates, AtomicLong numOfGenTraces, AtomicLong m2AndMean) {
		super(id, tool, resultQueue, seed, maxTraceDepth, maxTraceNum, traceActions, checkDeadlock,
				traceFile, liveCheck, numOfGenStates, numOfGenTraces, m2AndMean);
	}

    @Override
	protected Optional<SimulationWorker.SimulationWorkerError> simulateRandomTrace() throws Exception {

		ActionMapper mapper = ActionMapperFactory.getMapper(Arrays.asList(this.tool.getActions()), this.tool.getRootName());
		ActionController controller = new RemoteController(mapper, this.tool.getActions());

		// Actions to run asked by the controller
		Queue<ActionWrapper> actionsToRun = new ArrayDeque<>();
		// States visited in correspondence, to send to the controller
		List<TLCState> statesVisited = new ArrayList<>();
		statesVisited.add(curState);
		boolean quit = false;

		TraceGenLoop:
		while(true) {
			assert(initStates.size() == 1);
			curState = randomState(this.localRng, initStates);
			setCurrentState(curState);

			System.out.println("[Worker] Initial state: " + curState);

			for (int traceIdx = 0; traceIdx < maxTraceDepth && !quit; traceIdx++) {
				nextStates.clear();
				while(nextStates.empty()) {
					try{
						if (actionsToRun.isEmpty()) {
							controller.setVisitedStates(statesVisited);
							statesVisited.clear();
							actionsToRun.addAll(controller.getNextActions());
						}
						ActionWrapper nextActionWrapper = actionsToRun.remove();
						if(nextActionWrapper.isReset()) {
							continue TraceGenLoop;
						} else if(nextActionWrapper.isQuit() || nextActionWrapper.action.equals(Action.UNKNOWN)){
							quit = true;
							break TraceGenLoop;
						} else {
							this.tool.getNextStates(this, curState, nextActionWrapper.action);
							if(nextStates.empty()) {
								statesVisited.add(curState);
							}
						}
					} catch (SimulationWorkerError err) {
						return Optional.of(err);
					}
				}
			}
			assert(nextStates.size() == 1);
			final TLCState s1 = nextStates.elementAt(0);
			s1.execCallable();

			curState = s1;
			setCurrentState(curState);
			statesVisited.add(curState);
		}

		return Optional.empty();
    }
}
