package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Pirate extends Role {
    int mode = 0;    
    double fear = 0;
    MapLocation myLocation;
    MapLocation[] enemyPastrs = new MapLocation[0];
    
    Pirate(RobotController rc) {
        super(rc);
        rc.setIndicatorString(6, "I am pirate.");
    }
    void execute(){
        try {
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Pirate Exception");
        }
    }
}
