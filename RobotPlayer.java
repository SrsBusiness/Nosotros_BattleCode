package Nosotros_BattleCode;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;
import Nosotros_BattleCode.Vector;


public class RobotPlayer {
    static Random rand;
    
    private static Vector getForceVector(MapLocation src, MapLocation dst, double c1, double c2) {
        Vector force = new Vector(dst.x - src.x, dst.y - src.y);
        force = force.getUnitVector();
        force.setXY(c1*force.getX()+c2, c1*force.getY()+c2);
        return force;
    }

    private static Direction chooseDir(RobotController rc) throws Exception{
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
        Vector bestDir = new Vector(0.0, 0.0);
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyHQLocation = rc.senseEnemyHQLocation();
        Direction enemyDir = myLocation.directionTo(enemyHQLocation);
        Direction randChoice = directions[rand.nextInt(8)];
        if (rand.nextInt(5) == 0) {
            return randChoice;
        }
        int mode = 0; //Different modes have different parameters
        if (mode == 0) {
            Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam());
            Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
            //TODO: add force caused by allied base.
            Vector enemyHQVector = getForceVector(myLocation, enemyHQLocation, 3, -45);
            Vector alliedForceVector = new Vector(0.0, 0.0);
            Vector enemyForceVector = new Vector(0.0, 0.0);
            //TODO: add different weights to PASTRs and SOLDIERs
            for (Robot r: allies) {
                alliedForceVector.addVector(getForceVector(myLocation, rc.senseRobotInfo(r).location, 6, -9));
            }
            for (Robot r: enemies) {
                //TODO: add different mode responses and aggression parameter.
                alliedForceVector.addVector(getForceVector(myLocation, rc.senseRobotInfo(r).location, 1, -4));
            }
            bestDir.addVector(enemyHQVector);
            bestDir.addVector(alliedForceVector);
            bestDir.addVector(enemyForceVector);
            return bestDir.toDirectionEnum();
        } else {
            return Direction.NORTH;
        }
    }
    
    private static Robot getLowest(Robot[] robots, RobotController rc) throws Exception{
        double lowest = (double)Integer.MAX_VALUE;
        Robot weakest = null;
        for (Robot r : robots){
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
                        if(nearbyEnemies.length > 0) {
                            rc.attackSquare(rc.senseRobotInfo(getLowest(nearbyEnemies, rc)).location);
                            //yeild();
                        }
                        //Check if a robot is spawnable and spawn one if it is
                        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                            if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                                rc.spawn(toEnemy);
                                //yeild();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e.toString() + "HQ Exception");
                    }
                    break;
                case SOLDIER:
                    try {
                        if (rc.isActive()) {
                            int action = (rc.getRobot().getID()*rand.nextInt(101) + 50)%101;
                            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                            if (nearbyEnemies.length > 0) {
                                rc.attackSquare(rc.senseRobotInfo(getLowest(nearbyEnemies, rc)).location);
                            } else if (action < 1 &&
                                       rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 300 &&
                                       rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()).length == 0) {
                                rc.construct(RobotType.PASTR);
                            } else if (action < 80) {
                                Direction moveDirection = chooseDir(rc);
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
                            } else {
                                //Sneak towards the enemy
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
