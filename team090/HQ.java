package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class HQ extends Role{
    ArrayList<Integer> robotIDs = new ArrayList<Integer>(25);
    int farmer;
    int noisetowerMaker;
    int[] pirates = new int[2];
    MapLocation myLocation;
    Direction enemyDir;
    int broadcastIn;

    HQ(RobotController rc){
        super(rc);
        myLocation = rc.getLocation();
        enemyDir = myLocation.directionTo(enemyHQLocation);
    }

    MapLocation selectFarmLocation() {
        return rc.getLocation().subtract(enemyDir);
    }
    
    void execute(){
        try {
            //Single-robot commands executed through broadcasts.
            broadcastIn = rc.readBroadcast(0);
            if (broadcastIn != 0) {
                robotIDs.add(broadcastIn);
                rc.broadcast(0, 0);
            }
            /*
            if (robotIDs.size() == 5 && farmer == 0) {
                farmer = robotIDs.get(robotIDs.size()-1);
                rc.broadcast(1, farmer);
                rc.broadcast(2, 1);
                target = selectFarmLocation();
                // Tell the farmer the intended farm location.
                rc.broadcast(3, target.x);
                rc.broadcast(4, target.y);
            } else if (robotIDs.size() == 6 && noisetowerMaker == 0) {
                //noisetowerMaker = robotIDs.get(robotIDs.size()-1);
                //rc.broadcast(1, noisetowerMaker);
                //rc.broadcast(2, 2);
            } else if (robotIDs.size() == 14 && pirates[0] == 0) {
                pirates[0] = robotIDs.get(robotIDs.size() - 1);
                rc.broadcast(1, pirates[0]);
                rc.broadcast(2, 3);
            } else if (robotIDs.size() == 15 && pirates[1] == 0) {
                pirates[1] = robotIDs.get(robotIDs.size() - 1);
                rc.broadcast(1, pirates[0]);
                rc.broadcast(2, 4);
            }
            */
            //Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() &&
                rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                if (rc.senseObjectAtLocation(rc.getLocation().add(enemyDir)) == null) {
                    rc.spawn(enemyDir);
                } else {
                    Direction availDir = getNextAdjacentEmptyLocation(myLocation, enemyDir);
                    if (availDir != Direction.NONE) {
                        rc.spawn(availDir);
                    }
                }
            }

            Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam().opponent());
            ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
            //Populate the knowledge of nearby enemy robots.
            for (Robot r: nearbyEnemyRobots) {
                enemyRobotInfo.add(rc.senseRobotInfo(r));
            }
            //Attack nearby enemies (range^2 = 15).
            if(enemyRobotInfo.size() > 0) {
                RobotInfo target = getWeakestTargetInRange(allyHQLocation, enemyRobotInfo);
                if (target != null) {
                    rc.attackSquare(target.location);
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " HQ Exception\n");
            e.printStackTrace();
        }
    }
}
