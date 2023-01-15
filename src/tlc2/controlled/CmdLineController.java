package tlc2.controlled;

import tlc2.tool.Action;
import tlc2.tool.TLCState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdLineController implements ActionController {

    private BufferedReader reader;
    private TLCState currentState;


    public CmdLineController(TLCState initialState) {
        System.out.println("[Controller] Started the command line controller. Takes the next action from the user.");
        reader = new BufferedReader(new InputStreamReader(System.in));
        currentState = initialState;
    }

    @Override
    public int getNextAction(Action[] actions) {
        System.out.println("[Controller] ---- Selection of the next action ----" );
        System.out.println("[Controller] Current state: " + currentState.toString());
        System.out.println("[Controller] Actions: \n" + getActionsAsStr(actions));
        System.out.println("[Controller] Enter the next action (-1 for quit): " );

        return readInput();
    }

    @Override
    public void setCurrentState(TLCState state) {
        System.out.println("Setting the state to: " + state); // B

        currentState = state;
    }

    private int readInput() {
        while(true){
            try {
                return Integer.parseInt(reader.readLine());
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
            s.append("   Action #").append(i).append(": ").append(a.getName()).append(" id: ").append(a.pred.myUID).append("\n");
        }
        return s.toString();
    }
}
