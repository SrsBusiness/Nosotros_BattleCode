package Nosotros_BattleCode;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    static Random rand;

    private static Robot getLowest(Robot[] robots, RobotController rc) throws Exception{
        double lowest = (double)Integer.MAX_VALUE;
        Robot weakest = null;
        for(Robot r : robots){
            double i;
            if((i = rc.senseRobotInfo(r).health) < lowest){
                lowest = i; 
                weakest = r;
            }
        }
        return weakest;
    }
    public static void run(RobotController rc) {
        rand = new Random();
        Direction[] directions = {Direction.NORTH, 
            Direction.NORTH_EAST, 
            Direction.EAST, 
            Direction.SOUTH_EAST, 
            Direction.SOUTH, 
            Direction.SOUTH_WEST, 
            Direction.WEST, 
            Direction.NORTH_WEST};
        Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

        while(true) {
            switch(rc.getType()){
                case HQ:
                    try {					

                        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam().opponent());
                        //Check if a robot is spawnable and spawn one if it is
                        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                            if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                                rc.spawn(toEnemy);
                            }
                        }

                        if(nearbyEnemies.length > 0)
                            rc.attackSquare(rc.senseRobotInfo(getLowest(nearbyEnemies, rc)).location);
                    } catch (Exception e) {
                        System.err.println(e.toString() + "HQ Exception");
                    }
                    break;
                case SOLDIER:
                    try {
                        if (rc.isActive()) {
                            int action = (rc.getRobot().getID()*rand.nextInt(101) + 50)%101;
                            //Construct a PASTR
                            /*
                            if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 2) {
                                rc.construct(RobotType.PASTR);
                                //Attack a random nearby enemy
                            }*/ 
                            if (action < 30) {
                                Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                                if (nearbyEnemies.length > 0) {
                                    rc.attackSquare(rc.senseRobotInfo(getLowest(nearbyEnemies, rc)).location);
                                }
                                //Move in a random direction
                            } else if (action < 80) {
                                Direction moveDirection = directions[rand.nextInt(8)];
                                if (rc.canMove(moveDirection)) {
                                    rc.move(moveDirection);
                                }
                                //Sneak towards the enemy
                            } else {
                                if (rc.canMove(toEnemy)) {
                                    rc.sneak(toEnemy);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e.toString() + "Soldier Exception");
                    }
                    break;
                case NOISETOWER:case PASTR:
                    break;
            }
            System.out.println(Clock.getBytecodeNum());
            rc.yield();
        }
    }
}
