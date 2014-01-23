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
        Direction.NORTH_WEST};
    //Used for collisions
    Direction[] directionAlts = {
        Direction.NORTH_EAST, 
        Direction.NORTH_WEST,
        Direction.EAST, 
        Direction.WEST,
        Direction.SOUTH_EAST, 
        Direction.SOUTH_WEST, 
        Direction.SOUTH};
    Random rand;
    //Values set on constructor call
    RobotController rc;
    int mapWidth; 
    int mapHeight;     
    MapLocation allyHQLocation;
    MapLocation enemyHQLocation;
    Team myTeam;
    Team notMyTeam;

    int aggression;

    MapLocation target = null;
    //Pheremone trail for pathfinding (private to each robot).
    Queue<MapLocation> myTrail = new LinkedList<MapLocation>();

    //Run loop implementation for each role.
    abstract void execute(); 

    //Constructor
    Role(RobotController rc) {
        this.rc = rc;
        myTeam = rc.getTeam();
        notMyTeam = myTeam.opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        allyHQLocation = rc.senseHQLocation();
        enemyHQLocation = rc.senseEnemyHQLocation();
    }

    Direction getNextAdjacentEmptyLocation(MapLocation src, Direction initial) {
        Direction[] alternatives = new Direction[7];
        int offset = initial.ordinal();
        for(int i = 0; i < 7; i++) {
            alternatives[i] = directions[(directionAlts[i].ordinal()+offset)%8];
        }
        try {
            for (Direction d: alternatives) {
                if(rc.canMove(d)) {
                    return d;
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " could not get adjacent empty location");
            return Direction.NONE;
        }
        return Direction.NONE;
    }
    //TODO: fill in terrain to prevent getting stuck.
    void tryToWalk(MapLocation src,
                   ArrayList<RobotInfo> allyRobotInfo,
                   ArrayList<RobotInfo> enemyRobotInfo,
                   int mode) throws Exception {
        Vector North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest;
        boolean isOnPheromone = false;
        Direction desiredDirection;

        for (MapLocation p: myTrail) {
            if (src.equals(p)) {
                isOnPheromone = true;
            }
        }
        if (isOnPheromone) {
            //TODO: Calculate min-force obstacle pathfinding.
            desiredDirection = netForceAt(src, allyRobotInfo, enemyRobotInfo, mode).toDirectionEnum();
        } else {
            //Unobstructed walking
            desiredDirection = netForceAt(src, allyRobotInfo, enemyRobotInfo, mode).toDirectionEnum();
        }
        //Error checking for move.
        //TODO: figure out how action delay works.x
        while (rc.getActionDelay() > 1) {
            rc.yield();
        }
        if (rc.canMove(desiredDirection)) {
            rc.move(desiredDirection);
        } else {
            //Choose the next available location to escape the local minima.
            desiredDirection = getNextAdjacentEmptyLocation(src, desiredDirection);
            if (desiredDirection != Direction.NONE &&
                rc.canMove(desiredDirection)) {
                rc.move(desiredDirection);
            }
        }
        //Lay down pheromone trail.
        myTrail.offer(src);
        //GA TODO: parameterize the trail size.
        if (myTrail.size() > 9) {
            myTrail.remove();
        }
    }
    double howScared(MapLocation src,
                     ArrayList<RobotInfo> allyRobotInfo,
                     ArrayList<RobotInfo> enemyRobotInfo) {
        if (rc.getHealth() <= 30) {
            return 10;
        }
        int nearbyAllyCount = 0;
        int nearbyEnemyCount = 0;
        for (RobotInfo info: allyRobotInfo) {
            if (src.distanceSquaredTo(info.location) < 36) {
                nearbyAllyCount++;
            }
        }
        //TODO: see if this makes a difference.
        for (RobotInfo info: enemyRobotInfo) {
            if (src.distanceSquaredTo(info.location) < 36) {
                nearbyEnemyCount++;
            }
        }
        if (nearbyAllyCount < nearbyEnemyCount) {
            return 1+nearbyEnemyCount-nearbyAllyCount;
        }
        return 0;
    }
    //TODO: bytecode optimize this call so that
    //only one call to senseNearbyGameObjects needs to be made each turn.
    Vector netForceAt(MapLocation src,
                      ArrayList<RobotInfo> allyRobots,
                      ArrayList<RobotInfo> enemyRobots,
                      int mode) throws Exception {
        Vector netForce = new Vector();
        Vector targetForce = new Vector();
        Vector pheromoneForce = new Vector();
        Vector enemyHQForce = Vector.getForceVector(src, enemyHQLocation);
        Vector allyHQForce = Vector.getForceVector(src, allyHQLocation);
        Vector enemyForce = new Vector();
        Vector allyForce = new Vector();
        int count;

        switch (mode) {
            case 0:
            //Charge mode:
            //Go to the enemyHQ, but keep a distance.
            //Have all units attempt to camp and surround the enemy base.
            //Don't walk alone blindly either. Walk with buddies.
                //GA TODO: paramaterize weights.
                enemyHQForce = enemyHQForce.log(3.9+2, 4);
                allyHQForce = allyHQForce.log(6, 0.2);
                //TODO: make sure this check is necassary
                //Allied forces
                if (allyRobots.size() > 0) {
                    count = 0;
                    for (RobotInfo i: allyRobots) {
                        switch (i.type) {
                            case SOLDIER: 
                                count++;
                                allyForce.add(Vector.getForceVector(src, i.location));
                                break;
                        }
                    }
                    if (count > 0) {
                        //GA TODO: set scale factor.
                        allyForce = allyForce.scale(1.0/count);
                    }
                    allyForce.log(2.7, 2.2);
                }
                //Enemy forces
                //TODO: add different mode responses and aggression parameter.
                if (enemyRobots.size() > 0) {
                    count = 0;
                    for (RobotInfo i: enemyRobots) {
                        switch (i.type) {
                            case SOLDIER: 
                                count++;
                                enemyForce.add(Vector.getForceVector(src, i.location));
                                break;
                            //TODO: split up soldier and pastr forces
                            case PASTR: 
                                count++;
                                enemyForce.add(Vector.getForceVector(src, i.location));
                                break;
                        }
                    }
                    if (count > 0) {
                        //GA TODO: set scale factor.
                        enemyForce = enemyForce.scale(1.0/count);
                    }
                }
                enemyForce = new Vector();
                break;
            case 1:
                //Grouping mode:
                //Stay together with allied soldiers, and if there are no allies,
                //surround the ally HQ.
                //TODO: Vector.step()
                if (enemyHQForce.getMagnitudeSq() < 16) {
                    enemyHQForce = enemyHQForce.log(3.9+1.2, 6);
                } else {
                    enemyHQForce = new Vector();
                }
                //Allied forces
                //GA TODO: Parameterize army size.
                if (allyRobots.size() < 3) {
                    //Hover around the allied HQ.
                    allyHQForce = allyHQForce.logistic(2, 5, 0);
                } else {
                    allyHQForce = allyHQForce.logistic(2, 1, 0);
                    count = 0;
                    for (RobotInfo i: allyRobots) {
                        switch (i.type) {
                            case SOLDIER: 
                                count++;
                                allyForce.add(Vector.getForceVector(src, i.location));
                                break;
                            case PASTR:
                                break;
                            case HQ: 
                                break;
                        }
                    }
                    if (count > 0) {
                        //GA TODO: Parameterize scale.
                        allyForce = allyForce.scale(1.0/count);
                    }
                    allyForce.logistic(2, 2, 0);
                }
                break;
            case 2:
                //Go home to allied HQ.
                enemyHQForce = new Vector();
                allyHQForce = allyHQForce.logistic(3, 4, 0);
                break;
            case 3:
                //Have the enemy HQ only repel.
                if (enemyHQForce.getMagnitudeSq() < 16) {
                    enemyHQForce = enemyHQForce.log(3.9+1.2, 6);
                } else {
                    enemyHQForce = new Vector();
                }
                //Enemy forces
                //TODO: add different mode responses and aggression parameter.
                if (enemyRobots.size() > 0) {
                    count = 0;
                    for (RobotInfo i: enemyRobots) {
                        switch (i.type) {
                            case SOLDIER: 
                                count++;
                                enemyForce.add(Vector.getForceVector(src, i.location));
                                break;
                            //TODO: split up soldier and pastr forces
                            case PASTR: 
                                count++;
                                enemyForce.add(Vector.getForceVector(src, i.location));
                                break;
                        }
                    }
                    if (count > 0) {
                        //GA TODO: set scale factor.
                        enemyForce = enemyForce.scale(1.0/count).log(0, -3);
                    }
                }
                targetForce = Vector.getForceVector(src, target).logistic(0, 2, 0);
                break;
        }
        if (myTrail.size() > 0) {
            count = 0;
            for (Object p: myTrail.toArray()) {
                if (src.equals((MapLocation)p)) {
                    continue;
                } else {
                    count++;
                    pheromoneForce.add(Vector.getForceVector(src, (MapLocation)p));
                }
            }
            if (myTrail.size() > 0) {
                //GA TODO: set scale factor.
                pheromoneForce = pheromoneForce.scale(1.0/count);
            }
        }
        netForce.add(targetForce);
        netForce.add(allyForce);
        netForce.add(enemyForce);
        netForce.add(allyHQForce);
        netForce.add(enemyHQForce);
        //netForce.add(pheromoneForce);
        return netForce;
    } 
    RobotInfo getWeakestTargetInRange(MapLocation src, ArrayList<RobotInfo> enemyRobotInfo) throws Exception {
        //Choose the best robot to attack.
        //Prioritizes low HP units and SOLDIERS over PASTRS.
        //Do not attack HQs (futile).
        double lowestHP = (double)Integer.MAX_VALUE;
        RobotInfo bestTarget = null;
        for (RobotInfo info : enemyRobotInfo){
            if (src.distanceSquaredTo(info.location) > 10){
                continue;
            }
            switch (info.type) {
                case SOLDIER:
                    if (info.health < lowestHP) {
                        lowestHP = info.health;
                        bestTarget = info;
                    }
                    break;
                case PASTR:
                    //Do not take priority over soldiers.
                    if ((bestTarget == null
                         || bestTarget.type != RobotType.SOLDIER) &&
                        info.health < lowestHP) {
                        lowestHP = info.health;
                        bestTarget = info;
                    }
                    break;
            }
        }
        return bestTarget;
    }
}
