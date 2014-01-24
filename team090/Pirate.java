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
            if(rc.isActive()) {
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class);
                ArrayList<RobotInfo> allyRobotInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
                //Populate the knowledge of all nearby robots.
                RobotInfo info;
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                    if (info.team == myTeam) {
                        allyRobotInfo.add(info);
                    } else if (info.team == notMyTeam) {
                        enemyRobotInfo.add(info);
                    }
                }
                //Set current location
                myLocation = rc.getLocation();
                fear = howScared(myLocation, allyRobotInfo, enemyRobotInfo);
                //GA TODO: parameterize the health threshold.
                if (fear == 0 && rc.getHealth() > 70) {
                    mode = 0;
                } else if (fear == 10) {
                    mode = 2;
                } else if (fear > 0) {
                    mode = 1;
                }
                if (fear > 4) {
                    myLocation = rc.getLocation(); //JUST IN CASE PLS
                    tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, mode);
                    return;
                }
                if (mode == 0) {
                    //Sniping PASTRs
                    enemyPastrs = rc.sensePastrLocations(notMyTeam);
                    if (enemyPastrs.length > 0) {
                        target = enemyPastrs[0];
                        mode = 3;
                    } else {
                        target = allyHQLocation;
                    }
                }
                if (enemyRobotInfo.size() > 0) {
                    //Attacking, selecting the enemy with the lowest health.
                    RobotInfo attackTarget = getWeakestTargetInRange(myLocation,
                                                                     enemyRobotInfo);
                    if (attackTarget != null) {
                        rc.attackSquare(attackTarget.location);
                        return;
                    }
                }
                //Moving, using potential field.
                myLocation = rc.getLocation(); //JUST IN CASE PLS
                tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, mode);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Pirate Exception");
        }
    }
}
