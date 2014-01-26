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
    MapLocation[] enemyPastrs = new MapLocation[0];

    HQ(RobotController rc){
        super(rc);
        myLocation = rc.getLocation();
        enemyDir = myLocation.directionTo(enemyHQLocation);
    }

    MapLocation selectFarmLocation() {
        target = corners()[1];
        return target;
    }
    MapLocation selectNoiseTowerLocation() {
        target = corners()[1].add(0, 1);
        return target;
    }
    boolean getUnitTypeStatus(int type) {
        try {
            return (Clock.getRoundNum() - rc.readBroadcast(type) <= 2);
        } catch (GameActionException e) {
            System.out.println(e + " HQ Exception");
        }
        return true;
    }
    void execute(){
        try {
            //Single-robot commands executed through broadcasts.
            broadcastIn = rc.readBroadcast(0);
            if (broadcastIn != 0) {
                robotIDs.add(broadcastIn);
                rc.broadcast(0, 0);
            }
            //GA TODO: set the PASTR construction time.
            //paramaterize all these starting triggers.
            if ((robotIDs.size() >= 21 && farmer == 0)
                || (farmer != 0 && getUnitTypeStatus(6) == false)) {
                farmer = robotIDs.get(robotIDs.size()-1);
                rc.broadcast(1, farmer);
                rc.broadcast(2, 1);
                target = selectFarmLocation();
                rc.broadcast(3, target.x);
                rc.broadcast(4, target.y);
            } else if ((robotIDs.size() >= 22 && noisetowerMaker == 0)
                      || (noisetowerMaker != 0 && getUnitTypeStatus(7) == false)) {
                noisetowerMaker = robotIDs.get(robotIDs.size()-1);
                rc.broadcast(1, noisetowerMaker);
                rc.broadcast(2, 5);
                target = selectNoiseTowerLocation();
                rc.broadcast(3, target.x);
                rc.broadcast(4, target.y);
            } else if (robotIDs.size() == 8 && pirates[0] == 0) {
                pirates[0] = robotIDs.get(robotIDs.size() - 1);
                rc.broadcast(1, pirates[0]);
                rc.broadcast(2, 3);
            } else if (robotIDs.size() == 9 && pirates[1] == 0) {
                pirates[1] = robotIDs.get(robotIDs.size() - 1);
                rc.broadcast(1, pirates[1]);
                rc.broadcast(2, 4);
            }
            enemyPastrs = rc.sensePastrLocations(notMyTeam);
            if (enemyPastrs.length >= 2) {
                //If there are 2 or more PASTRS, start a-moving.
                rc.broadcast(1, -1);
                rc.broadcast(2, 4);
            }
            Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam().opponent());
            if (nearbyEnemyRobots.length == 0) {
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
            } else {
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
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " HQ Exception\n");
            e.printStackTrace();
        }
    }
}
