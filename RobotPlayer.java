package Nosotros_BattleCode;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    static Random rand;
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
    static int lifeTurn = 0;
    static ArrayList<Integer> robotIDs = new ArrayList<Integer>(25);
    static int broadcastIn;

    private static Vector getForceVector(MapLocation src, MapLocation dst, double c1, double c2) {
        Vector force = new Vector(dst.x - src.x, dst.y - src.y);
        double r = Math.pow(force.getMagnitudeSq(), 0.5);
        force = force.getUnitVector();
        //TODO: Apply GA'ed potential function.
        return force;
    }

    private static Direction chooseDir(RobotController rc) throws Exception{
        //Input: all nearby robots and obstacles
        //Output: best direction
        //
        //TODO: pheremone trail
        //TODO: fill in terrain to prevent getting stuck.
        
        lifeTurn = 0;
        //Use potential fields to judge next position.
        Vector bestDir = new Vector(0.0, 0.0);
        MapLocation myLocation = rc.getLocation();
        int mode = 0; //Different modes have different parameters
        Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 35, rc.getTeam());
        Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 35, rc.getTeam().opponent());
        Vector enemyHQVector = new Vector(0.0, 0.0);
        Vector alliedForceVector = new Vector(0.0, 0.0);
        Vector enemyForceVector = new Vector(0.0, 0.0);
        int count;
        switch (mode) {
            //Charge mode: have all units attempt to camp at the enemy base.
            case 0:
                count = 0;
                enemyHQVector = getForceVector(myLocation, enemyHQLocation, 3, -46);
                if (allies.length > 1) {
                    for (Robot r: allies) {
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                alliedForceVector.addVector(getForceVector(myLocation, info.location, 6, -15));
                                break;
                            case PASTR:
                                //count++;
                                //alliedForceVector.addVector(getForceVector(myLocation, info.location, 12, -18));
                                break;
                            case HQ: 
                                count++;
                                alliedForceVector.addVector(getForceVector(myLocation, info.location, -3, 0));
                                break;
                        }
                    }
                    alliedForceVector.scale(1.0/count);
                }
                if (enemies.length > 1) {
                    count = 0;
                    for (Robot r: enemies) {
                        //TODO: add different mode responses and aggression parameter.
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                enemyForceVector.addVector(getForceVector(myLocation, info.location, 1, 0));
                                break;
                            case PASTR: 
                                enemyForceVector.addVector(getForceVector(myLocation, info.location, 6, 0));
                                break;
                        }
                    }
                    enemyForceVector.scale(1.0/count);
                }
                break;
            case 1:
                break;
        }
        bestDir.addVector(enemyHQVector);
        bestDir.addVector(alliedForceVector);
        bestDir.addVector(enemyForceVector);
        return bestDir.toDirectionEnum();
    }
    
    private static Robot getBestTarget(Robot[] robots, RobotController rc) throws Exception {
        //Choose the best robot to attack. Prioritizes low HP units and SOLDIERS over PASTRS. Do not attack HQs (futile).
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
        MapLocation myLocation;
        enemyHQLocation = rc.senseEnemyHQLocation(); 
        rand = new Random();
        lifeTurn = 0;

        while (true) {
            myLocation = rc.getLocation();
            enemyDir = myLocation.directionTo(enemyHQLocation);
            switch (rc.getType()) {
                case HQ:
                    try {
                        broadcastIn = rc.readBroadcast(0);
                        if (broadcastIn != 0) {
                            robotIDs.add(broadcastIn);
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
                            rc.attackSquare(rc.senseRobotInfo(getBestTarget(nearbyEnemies, rc)).location);
                        }
                    } catch (Exception e) {
                        System.err.println(e.toString() + "HQ Exception");
                    }
                    break;
                case SOLDIER:
                    try {
                        if (rc.isActive()) {
                            lifeTurn++;
                            if (lifeTurn == 1) {
                                rc.broadcast(0, rc.getRobot().getID());
                            }
                            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                            if (nearbyEnemies.length > 0) {
                                rc.attackSquare(rc.senseRobotInfo(getBestTarget(nearbyEnemies, rc)).location);
                            } else if (
                                       rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()).length == 0) {
                                rc.construct(RobotType.PASTR);
                            } else {// if (action < 80) {
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
                    break;
                case NOISETOWER:case PASTR:
                    break;
            }
            System.out.println(Clock.getBytecodeNum());
            rc.yield();
        }
    }
}
