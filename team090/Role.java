package team090;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

abstract class Role{
    Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };

    Random rand;
    RobotController rc;
    double aggression;

    MapLocation myLocation;
    MapLocation enemyHQLocation;
    Direction enemyDir;
    MapLocation target = null;

    Queue<MapLocation> myTrail = new LinkedList<MapLocation>();

    abstract void execute(); // overwrite

    Role(RobotController rc){
        this.rc = rc;
    }

    static Vector getForceVector(MapLocation src, MapLocation dst) {
        Vector force = new Vector(dst.x - src.x, dst.y - src.y);
        double r = Math.pow(force.getMagnitudeSq(), 0.5);
        return force;
    }
    static Direction chooseDir(RobotController rc,
            Queue<MapLocation> pheromoneTrail) throws Exception {
        //TODO: fill in terrain to prevent getting stuck.
        //Use potential fields to judge next position.
        int mode = 0; //Different modes have different parameters
        Vector bestDir = new Vector();

        Team myTeam = rc.getTeam();
        MapLocation myLoc = rc.getLocation();
        MapLocation allyHQLoc = rc.senseHQLocation();
        MapLocation enemyHQLoc = rc.senseEnemyHQLocation();

        Vector pheremoneForce = new Vector();
        Vector enemyHQForce = new Vector();
        Vector allyHQForce = new Vector();
        Vector enemyForce = new Vector();
        Vector allyForce = new Vector();
        Vector fearForce = new Vector();

        //TODO: bytecode optimize this
        Robot[] allyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam.opponent());

        int count;
        switch (mode) {
            //Charge mode:
            //Go to the enemyHQ, but keep a distance.
            //Have all units attempt to camp and surround the enemy base.
            //Don't walk alone blindly either. Walk with buddies.
            case 0:
                enemyHQForce = getForceVector(myLoc, enemyHQLoc);
                allyHQForce = getForceVector(myLoc, allyHQLoc);
                //TODO: put backing off somewhere else, in another case.
                fearForce = allyHQForce.getUnitVector(); 

                if (allyRobots.length > 0) {
                    count = 0;
                    for (Robot r: allyRobots) {
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                allyForce = allyForce.add(getForceVector(myLoc, info.location));
                                break;
                            case PASTR:
                                break;
                            case HQ: 
                                break;
                        }
                    }
                    if (count > 0) {
                        allyForce = allyForce.scale(1.0/count);
                    }
                }
                if (enemyRobots.length > 0) {
                    count = 0;
                    for (Robot r: enemyRobots) {
                        //TODO: add different mode responses and aggression parameter.
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                enemyForce = enemyForce.add(getForceVector(myLoc, info.location));
                                break;
                            case PASTR: 
                                count++;
                                enemyForce = enemyForce.add(getForceVector(myLoc, info.location));
                                break;
                        }
                    }
                    if (count > 0) {
                        enemyForce = enemyForce.scale(1.0/count);
                    }
                }
                //Calculate pheremone forces
                /*
                   for (Object p: pheromoneTrail.toArray()) {
                   pheremoneForce = pheremoneForce.add(getForceVector(myLoc, (MapLocation)p));
                   }
                   if (pheromoneTrail.size() > 0) {
                   pheremoneForce = pheremoneForce.scale(1.0/pheromoneTrail.size());
                   }*/
                break;
            case 1:
                break;
        }
        bestDir = enemyHQForce.log(3.9+1.2, 6);

        if (rc.getHealth() < 50 || allyRobots.length < 3) {
            bestDir = bestDir.add(allyHQForce.logistic(4, (130-rc.getHealth()), 0));
            if (enemyRobots.length > 0) {
                bestDir = bestDir.add(enemyForce.poly(0,0,0,0,-80,0,0,0,-1));
            }
            //bestDir = bestDir.add(fearForce.scale(12));
            //allyForce = allyForce.poly(0,0,0,0,-12,0,0,0,0);
        } else {
            bestDir = bestDir.add(enemyForce.log(2, 3));
        }
        bestDir = bestDir.add(allyForce.log(2.6, 4));
        return bestDir.toDirectionEnum();
    } 
    static Robot getBestTarget(Robot[] robots, RobotController rc) throws Exception {
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

}
