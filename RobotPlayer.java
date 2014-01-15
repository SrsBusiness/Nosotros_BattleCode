package Nosotros_BattleCode;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;
import Nosotros_BattleCode.Point;


public class RobotPlayer {
    static Random rand;
    
    private static Point getForceVector(MapLocation src, MapLocation dst, double c1, double c2) {
        Point unitVector = new Point(dst.x - src.x, dst.y - src.y);
        //TODO: investigate bytecode costs
        double magnitude = unitVector.distanceSq(0.0, 0.0);
        unitVector.setLocation((c1*unitVector.getX()/magnitude)+c2, (c1*unitVector.getY()/magnitude)+c2);
        return unitVector;
    }

    private static Direction chooseDir(Robot[] robots, RobotController rc) throws Exception{
        Direction[] directions = {Direction.NORTH, 
            Direction.NORTH_EAST, 
            Direction.EAST, 
            Direction.SOUTH_EAST, 
            Direction.SOUTH, 
            Direction.SOUTH_WEST, 
            Direction.WEST, 
            Direction.NORTH_WEST};
        //Input: all nearby robots and obstacles
        //Output: best direction
        //TODO: fill in terrain to prevent getting stuck.
        //TODO: pheremone trail
        
        //Use potential fields to judge next position.
        Point bestDir = new Point(0.0, 0.0);
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyHQLocation = rc.senseEnemyHQLocation();
        Direction enemyDir = myLocation.directionTo(enemyHQLocation);
        //If in range of enemy HQ, leave.
        if (myLocation.distanceSquaredTo(enemyHQLocation) <= 15*15) {
            return enemyDir.opposite();
        } else if (rand.nextInt(4) < 3) {
            return directions[rand.nextInt(8)];
        } else {
            return enemyDir;
            //Point enemyVector = getForceVector(myLocation, enemyHQLocation, 3, -45);
        }
    }
    
    private static Robot getLowest(Robot[] robots, RobotController rc) throws Exception{
        double lowest = (double)Integer.MAX_VALUE;
        Robot weakest = null;
        for(Robot r : robots){
            double i;
            RobotInfo rInfo = rc.senseRobotInfo(r);
            if(weakest != null && rc.senseRobotInfo(weakest).type.equals(RobotType.SOLDIER) && !rInfo.type.equals(RobotType.SOLDIER) ){
            }
            if((i = rInfo.health) < lowest){
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
        MapLocation enemyHQLocation = rc.senseEnemyHQLocation();
        Direction toEnemy = rc.getLocation().directionTo(enemyHQLocation);

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
                            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                            if (nearbyEnemies.length > 0) {
                                rc.attackSquare(rc.senseRobotInfo(getLowest(nearbyEnemies, rc)).location);
                                //Move in a random direction
                            } else if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 300) {
                                rc.construct(RobotType.PASTR);
                                //Attack a random nearby enemy
                            } else if (action < 80) {
                                Direction moveDirection = chooseDir(nearbyEnemies, rc);
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
