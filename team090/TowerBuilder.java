package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class TowerBuilder extends Role{
    MapLocation myLocation;
    RobotType blueprints;
    MapLocation goal;
    boolean gotToHQ = false;

    TowerBuilder(RobotController rc, int x, int y, RobotType structure) {
        super(rc);
        goal = new MapLocation(x, y);
        target = allyHQLocation;
        blueprints = structure;
        System.out.printf("Bonjour! My target: %d, %d\n", target.x, target.y);
    }
    void execute() {
        try {
            //Go to the designated NOISETOWER location.
            if (rc.getLocation().equals(target)) {
                //Construct the NOISETOWER when the location matches.
                rc.construct(blueprints);
                return;
            } else {
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 35, notMyTeam);
                ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
                if (nearbyRobots.length > 0) {
                    for (Robot r: nearbyRobots) {
                        enemyRobotInfo.add(rc.senseRobotInfo(r));
                    }
                }
                //Go to the target.
                myLocation = rc.getLocation();
                if (myLocation.distanceSquaredTo(allyHQLocation) < 36) {
                    gotToHQ = true;
                    target = goal;
                }
                System.out.printf("I'm on it. Current : %d, %d\n", myLocation.x, myLocation.y);
                tryToWalk(myLocation, null, enemyRobotInfo, 3);
                return;
            }
        } catch(Exception e) {
            System.err.println(e + " Builder Exception");
            e.printStackTrace();
        }
    }
}
