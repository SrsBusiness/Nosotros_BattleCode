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
        target = goal;//allyHQLocation;
        blueprints = structure;
        System.out.printf("Bonjour! Curr round: %d. Curr pos: %d, %d. My target: %d, %d\n",
                Clock.getRoundNum(), rc.getLocation().x, rc.getLocation().y, target.x, target.y);
        rc.setIndicatorString(6, "I am builder.");
    }
    void execute() {
        try {
            if (rc.getLocation().equals(target)) {
                //Construct the NOISETOWER when the location matches.
                rc.broadcast(keepaliveChannel, Clock.getRoundNum() + getStructureCost(blueprints));
                rc.construct(blueprints);
                return;
            } else {
                keepalive();
                //Go to the designated NOISETOWER location.
                myLocation = rc.getLocation();
                //GA TODO: parameterize.
                /*
                if (myLocation.distanceSquaredTo(target) < 36) {
                    target = goal;
                    System.out.printf("Arrived at base. Going to goal at: %d, %d\n", target.x, target.y);
                }
                */
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 35, notMyTeam);
                ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
                if (nearbyRobots.length > 0) {
                    for (Robot r: nearbyRobots) {
                        enemyRobotInfo.add(rc.senseRobotInfo(r));
                    }
                }
            //  System.out.printf("I'm on it. Current : %d, %d, Target: %d, %d\n",
            //                    myLocation.x, myLocation.y,
            //                    target.x, target.y);
                tryMove(myLocation, null, enemyRobotInfo, 2, 0);
                return;
            }
        } catch(Exception e) {
            System.err.println(e + " Builder Exception");
            e.printStackTrace();
        }
    }
}
