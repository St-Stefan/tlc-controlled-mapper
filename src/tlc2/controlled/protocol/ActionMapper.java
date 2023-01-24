package tlc2.controlled.protocol;

import tlc2.tool.Action;

public interface ActionMapper {

    // Takes an action in the json form and maps it to Action in TLAChecker
    Action map(String actionString);

}
