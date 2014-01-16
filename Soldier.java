
package Nosotros_BattleCode;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Soldier {
    static Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };
    static Random rand;
    static int lifeTurn = 0;

    static void Soldier_run(RobotController rc) {
        while(true){
            rand = new Random();
            try {
                if (rc.isActive()) {
                    // listen for broadcasts
                    lifeTurn++;
                    if (lifeTurn == 1) {
                        rc.broadcast(0, rc.getRobot().getID());
                    }
                    Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                    if (nearbyEnemies.length > 0) {
                        rc.attackSquare(rc.senseRobotInfo(RobotPlayer.getBestTarget(nearbyEnemies, rc)).location);
                    } else if (
                            rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()).length == 0) {
                        rc.construct(RobotType.PASTR);
                    } else {// if (action < 80) {
                        Direction moveDirection = RobotPlayer.chooseDir(rc);
                        if (rc.canMove(moveDirection)) {
                            rc.move(moveDirection);
                        } else {
                            //Try a random direction
                            //TODO: try each dir
                            Direction randChoice = directions[rand.nextInt(8)];
                            if (rc.canMove(randChoice)) {
                                rc.move(randChoice);
                            }
                        }
                    }/* else {
                    //Sneak towards the enemy
                    //if (rc.canMove(toEnemy)) {
                    //    rc.sneak(toEnemy);
                    //}
                    }*/
                    }
                } catch (Exception e) {
                    System.err.println(e.toString() + "Soldier Exception");
                }
                rc.yield();
            }
        }
    }
