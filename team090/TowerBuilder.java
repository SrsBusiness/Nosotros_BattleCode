package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class TowerBuilder extends Role{
    MapLocation target;
    MapLocation myLocation;

    TowerBuilder(RobotController rc, int x, int y) {
        super(rc);
        target = new MapLocation(x, y);
        System.out.println("Bonjour, I am the builder of the world's most magnificent tower.");
    }

    void execute(){
        try {
            //Go to the designated PASTR location.
            //If the target is not null, go to it.
            if (rc.getLocation().equals(target)) {
                //Construct the PASTR when the location matches.
                rc.construct(RobotType.NOISETOWER);
            } else {
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
                ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
                if (nearbyRobots.length > 0) {
                    for (Robot r: nearbyRobots) {
                        enemyRobotInfo.add(rc.senseRobotInfo(r));
                    }
                }
                //Go to the target.
                myLocation = rc.getLocation();
                System.out.println(enemyRobotInfo.length);
                tryToWalk(myLocation, enemyRobotInfo, enemyRobotInfo, 3);
            }
        } catch(Exception e) {
            System.err.println(e + " TowerBuilder Exception");
        }
    }
}
