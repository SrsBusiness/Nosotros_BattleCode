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
    double [][] cowRates = null;
    MapLocation optimalFarm = null;
    MapLocation optimalTower = null;

    HQ(RobotController rc){
        super(rc);
        myLocation = rc.getLocation();
        enemyDir = myLocation.directionTo(enemyHQLocation);
    }

    void getOptimalFarmLocation() {
        int dx = enemyHQLocation.x - allyHQLocation.x;
        int dy = enemyHQLocation.y - allyHQLocation.y;
        MapLocation quadrant;
        int fdx, fdy;
        if (dx > dy) { // Split horizontally
            if (dx < 0) { // HQ on right half
                if (allyHQLocation.y < mapHeight/2) {
                    quadrant = new MapLocation(mapWidth/2, mapHeight/2); // HQ on top half
                    fdx = 1; fdy = 1;
                } else {
                    quadrant = new MapLocation(mapWidth/2, 0); // HQ on bottom half
                    fdx = 1; fdy = -1;
                }
            } else { // HQ on left half
                if (allyHQLocation.y < mapHeight/2) {
                    quadrant = new MapLocation(0, mapHeight/2); // HQ on top half
                    fdx = -1; fdy = 1;
                } else {
                    quadrant = new MapLocation(0, 0); // HQ on bottom half
                    fdx = -1; fdy = -1;
                }
            }
        } else { // Split vertically
            if (dy < 0) { // HQ on bottom half
                if (allyHQLocation.x < mapWidth/2) {
                    quadrant = new MapLocation(mapWidth/2, mapHeight/2); // HQ on left half
                    fdx = 1; fdy = 1;
                } else {
                    quadrant = new MapLocation(0, mapHeight/2); // HQ on right half
                    fdx = -1; fdy = 1;
                }
            } else { // HQ on top half
                if (allyHQLocation.x < mapWidth/2) {
                    quadrant = new MapLocation(mapWidth/2, 0); // HQ on left half
                    fdx = 1; fdy = -1;
                } else {
                    quadrant = new MapLocation(0, 0); // HQ on right half
                    fdx = -1; fdy = -1;
                }
            }
        }
        MapLocation testLoc;
        MapLocation bestLoc = quadrant;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int cx = 1; cx < mapWidth/4 - 1; cx++) {
            for (int cy = 1; cy < mapHeight/4 - 1; cy++) {
                testLoc = quadrant.add(cx, cy);
                if (cowRates[testLoc.y][testLoc.x] != 0
                    && cowRates[testLoc.y+fdx][testLoc.x+fdy] != 0)
                {
                    double score = 0;
                    for (int x = 0; x < 9; x++) {
                        for (int y = 0; y < 9; y++) {
                            score += cowRates[testLoc.y+y][testLoc.x+x];
                        }
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        bestLoc = testLoc;
                    }
                }
            }
        }
        optimalTower = bestLoc;
        optimalFarm = bestLoc.add(fdx, fdy);            
    }
    MapLocation selectFarmLocation() {
        if (optimalFarm != null) {
            return optimalFarm;
        } else {
            return allyHQLocation;
        }
    }
    MapLocation selectNoiseTowerLocation() {
        if (optimalTower != null) {
            return optimalTower;
        } else {
            return allyHQLocation;
        }
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
            if ((robotIDs.size() >= 6 && noisetowerMaker == 0)
                      || (noisetowerMaker != 0 && getUnitTypeStatus(7) == false)) {
                noisetowerMaker = robotIDs.get(robotIDs.size()-1);
                rc.broadcast(1, noisetowerMaker);
                rc.broadcast(2, 5);
                target = selectNoiseTowerLocation();
                rc.broadcast(3, target.x);
                rc.broadcast(4, target.y);
            } else if ((robotIDs.size() >= 7 && farmer == 0)
                || (farmer != 0 && getUnitTypeStatus(6) == false)) {
                farmer = robotIDs.get(robotIDs.size()-1);
                rc.broadcast(1, farmer);
                rc.broadcast(2, 1);
                target = selectFarmLocation();
                rc.broadcast(3, target.x);
                rc.broadcast(4, target.y);
            } else if (robotIDs.size() == 8 && pirates[0] == 0) {
            //  pirates[0] = robotIDs.get(robotIDs.size() - 1);
            //  rc.broadcast(1, pirates[0]);
            //  rc.broadcast(2, 3);
            //} else if (robotIDs.size() == 9 && pirates[1] == 0) {
            //  pirates[1] = robotIDs.get(robotIDs.size() - 1);
            //  rc.broadcast(1, pirates[1]);
            //  rc.broadcast(2, 4);
            }
            enemyPastrs = rc.sensePastrLocations(notMyTeam);
            if (enemyPastrs.length >= 2) {
                //If there are 2 or more PASTRS, start a-moving.
                rc.broadcast(1, -1);
                rc.broadcast(2, 4);
            }
            //Only spawn if there are no robots in attack range.
            Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam().opponent());
            if (nearbyEnemyRobots.length == 0) {
                //Check if a robot is spawnable and spawn one if it is
                if (rc.isActive() &&
                    rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                    if (rc.senseObjectAtLocation(rc.getLocation().add(enemyDir)) == null) {
                        rc.spawn(enemyDir);
                    } else {
                        Direction availDir = nextAdjacentEmptyLocation(myLocation, enemyDir);
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
                    RobotInfo target = bestTargetInRange(allyHQLocation, enemyRobotInfo);
                    if (target != null) {
                        rc.attackSquare(target.location);
                    }
                }
            }
            if (cowRates == null) {
                cowRates = rc.senseCowGrowth();
                getOptimalFarmLocation();
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " HQ Exception\n");
            e.printStackTrace();
        }
    }
}
