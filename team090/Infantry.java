package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Infantry extends Role{
    
    Infantry(RobotController rc) {
        super(rc);
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
                MapLocation myLocation = rc.getLocation();
                double fear = howScared(myLocation, allyRobotInfo, enemyRobotInfo);
                if (fear > 5) {
                    //Moving, using potential field.
                    tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, 2);
                } else if (fear > 0) {
                    tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, 1);
                } else if (enemyRobotInfo.size() > 0) {
                    //Attacking, selecting the enemy with the lowest health.
                    RobotInfo attackTarget = getWeakestTargetInRange(myLocation,
                                                                     enemyRobotInfo);
                    if (attackTarget != null) {
                        rc.attackSquare(attackTarget.location);
                        return;
                    }
                }
                tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, 0);
            }
        } catch(Exception e) {
            System.err.println(e + " Infantry Exception");
        }
    }

}

