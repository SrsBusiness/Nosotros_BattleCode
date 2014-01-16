package Nosotros_BattleCode;
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

    static void HQ_run(RobotController rc) {
        enemyHQLocation = rc.senseEnemyHQLocation(); 
        enemyDir = rc.senseHQLocation().directionTo(enemyHQLocation);

        while(true) {
            try {
                broadcastIn = rc.readBroadcast(0);
                if (broadcastIn != 0) {
                    robotIDs.add(broadcastIn);
                }
                if (robotIDs.size() == 5) {
                    rc.broadcast(1, robotIDs.get(robotIDs.size()-1));
                }
                //Check if a robot is spawnable and spawn one if it is
                if (rc.isActive() &&
                        rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                    if (rc.senseObjectAtLocation(rc.getLocation().add(enemyDir)) == null) {
                        rc.spawn(enemyDir);
                        //Increment global robot count.
                        //rc.setTeamMemory(0, rc.getTeamMemory()[0]+1)
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
