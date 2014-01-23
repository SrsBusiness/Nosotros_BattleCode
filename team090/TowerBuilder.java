package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class TowerBuilder extends Role{
    MapLocation target;

    TowerBuilder(RobotController rc, MapLocation towerSpot) {
        super(rc);
        target = towerSpot;
    }

    void execute(){
        try {
            //Go to the designated PASTR location.
            //If the target is not null, go to it.
            if (rc.getLocation().equals(target)) {
                //Construct the PASTR when the location matches.
                rc.construct(RobotType.NOISETOWER);
            } else {
                //Go to the target.
                //moveTo(target);
            }
        } catch(Exception e) {
            System.err.println(e + " TowerBuilder Exception");
        }
    }
}
