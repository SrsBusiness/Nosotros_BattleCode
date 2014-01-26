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

    TowerBuilder(RobotController rc, int x, int y, RobotType structure) {
        super(rc);
        switch(structure) {
            case PASTR:
                keepaliveChannel = 6;
                break;
            case NOISETOWER:
                keepaliveChannel = 7;
                break;
            default:
                break;
        }
        goal = new MapLocation(x, y);
        target = allyHQLocation;
        blueprints = structure;
        System.out.printf("Bonjour! My target: %d, %d\n", target.x, target.y);
        rc.setIndicatorString(6, "I am builder.");
    }
    void execute() {
        try {
            //Go to the designated NOISETOWER location.
            if (rc.getLocation().equals(target)) {
                //Construct the NOISETOWER when the location matches.
                rc.broadcast(keepaliveChannel, Clock.getRoundNum() + getStructureCost(blueprints));
                rc.construct(blueprints);
                return;
            } else {
                keepalive();
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
                    target = goal;
                }
                System.out.printf("I'm on it. Current : %d, %d, Target: %d, %d\n",
                                  myLocation.x, myLocation.y,
                                  target.x, target.y);
                tryToWalk(myLocation, null, enemyRobotInfo, 3);
                return;
            }
        } catch(Exception e) {
            System.err.println(e + " Builder Exception");
            e.printStackTrace();
        }
    }
}
