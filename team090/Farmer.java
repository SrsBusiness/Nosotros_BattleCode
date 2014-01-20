package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Farmer extends Role{

    void execute(){
        try {
            //Go to the designated PASTR location.
            //Select a target on the first turn
            if (lifeTurn++ == 2) {
                target = nul;
            }
            //If the target is not null, go to it.
            if (rc.getLocation().equals(target)) {
                //Construct the PASTR when the location matches.
                rc.construct(RobotType.PASTR);
            } else {
                //Go to the target.
            }
        } catch(Exception e) {
            System.err.println(e + " Farmer Exception");
        }
    }
}
