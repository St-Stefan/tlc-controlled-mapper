package tlc2.controlled;

import tlc2.controlled.protocol.ActionMapper;
import tlc2.tool.Action;
import tlc2.tool.TLCState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdLineController extends ActionController {

    private BufferedReader reader;
    private TLCState currentState;


    public CmdLineController(ActionMapper mapper, Action[] actions, TLCState initialState) {
        super(mapper, actions);
        System.out.println("[Controller] Started the command line controller. Takes the next action from the user.");
        reader = new BufferedReader(new InputStreamReader(System.in));
        currentState = initialState;
    }

    @Override
    public Action getNextAction() {
        System.out.println("[Controller] ---- Selection of the next action ----" );
        System.out.println("[Controller] Current state: " + currentState.toString());
        // System.out.println("[Controller] Actions: \n" + getActionsAsStr(actions)); //TODO Global setting for prints
        System.out.println("[Controller] Enter the next action (-1 for quit): " );

        return mapper.map(readInput());
    }

    @Override
    public void setCurrentState(TLCState state) {
        if(state == null) {
            System.out.println("No next state for that action. Choose again.\n\n");
        } else {
            System.out.println("Setting the state to: " + state);
            currentState = state;
        }
    }

    private String readInput() {
        while(true){
            try {
                return reader.readLine();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.out.println("Enter a valid integer for the next action: ");
            }
        }
    }

    private String getActionsAsStr(Action[] actions) {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            s.append("   Action #").append(i).append(": ").append(a.getName()).append("\n");
            // s.append(new AbstractAction(a.));
            /*
            System.out.println(a.con.depth());
            s.append("(");
            if(a.con.getValue() != null)
                s.append(a.con.getValue());
            s.append(")");
            s.append(" id: ").append(a.pred.myUID).append("\n");
            */
        }
        return s.toString();
    }
}
