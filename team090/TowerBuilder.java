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
        target = new MapLocation(x, y);
        blueprints = structure;
        rc.setIndicatorString(6, "I am builder, going to: "+target.x+", "+target.y);
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
                tryMove(myLocation, null, null, enemyRobotInfo, -1, 0);
                return;
            }
        } catch(Exception e) {
            System.err.println(e + " Builder Exception");
            e.printStackTrace();
        }
    }
}
