package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class HQ {
    static Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };
    static MapLocation enemyHQLocation;
    static Direction enemyDir;
    static int broadcastIn;
    static ArrayList<Integer> robotIDs = new ArrayList<Integer>(25);
    static int pastrMaker;
    static int noisetowerMaker;
    static int[] pirates = new int[2];

    static void run(RobotController rc) {
        enemyHQLocation = rc.senseEnemyHQLocation(); 
        enemyDir = rc.senseHQLocation().directionTo(enemyHQLocation);
        // location of enemy pastr. if non-negative, pirates swarm it
        try {
            rc.broadcast(20, -1);
            rc.broadcast(21, -1);
        } catch(Exception e) {
            System.err.println(e + " HQ Could Not BroadCast");
        }
        while(true) {
            try {
                //Single-robot commands executed through broadcasts.
                broadcastIn = rc.readBroadcast(0);
                if (broadcastIn != 0) {
                    robotIDs.add(broadcastIn);
                    rc.broadcast(0, 0);
                }
                if (robotIDs.size() == 5 && pastrMaker == 0) {
                //    pastrMaker = robotIDs.get(robotIDs.size()-1);
                //    rc.broadcast(1, pastrMaker);
                //    rc.broadcast(2, 1);
                //} else if (robotIDs.size() == 6 && noisetowerMaker == 0) {
                //    noisetowerMaker = robotIDs.get(robotIDs.size()-1);
                //    rc.broadcast(1, noisetowerMaker);
                //    rc.broadcast(2, 2);
                } else if (robotIDs.size() == 14 && pirates[0] == 0) {
                    pirates[0] = robotIDs.get(robotIDs.size() - 1);
                    rc.broadcast(1, pirates[0]);
                    rc.broadcast(2, 3);
                } else if (robotIDs.size() == 15 && pirates[1] == 0) {
                    pirates[1] = robotIDs.get(robotIDs.size() - 1);
                    rc.broadcast(1, pirates[0]);
                    rc.broadcast(2, 4);
                }
                //Check if a robot is spawnable and spawn one if it is
                if (rc.isActive() &&
                    rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                    if (rc.senseObjectAtLocation(rc.getLocation().add(enemyDir)) == null) {
                        rc.spawn(enemyDir);
                    }
                }
                //Attack nearby enemies (range^2 = 15).
                Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam().opponent());
                if(nearbyEnemies.length > 0) {
                    rc.attackSquare(rc.senseRobotInfo(RobotPlayer.getBestTarget(nearbyEnemies, rc)).location);
                }
            } catch (Exception e) {
                System.err.println(e.toString() + "HQ Exception");
            }
            rc.yield();
        }
    }
}
