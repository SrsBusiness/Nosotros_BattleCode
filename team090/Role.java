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
        //TODO: cool math that orders things.
        try {
            for (Direction d : directions) {
                if (d.equals(initial)) {
                    continue;
                }
                if(rc.senseObjectAtLocation(src.add(d)) == null) {
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
            //Calculate min-force obstacle pathfinding.
            desiredDirection = Direction.NONE;
        } else {
            //Unobstructed walking
            desiredDirection = netForceAt(src, allyRobotInfo, enemyRobotInfo, mode).toDirectionEnum();
        }
        //Error checking for move.
        if (rc.canMove(desiredDirection)) {
            rc.move(desiredDirection);
        } else {
            //Choose the next available location to escape the local minima.
            //If in charge mode, don't accidentally get too close.
            desiredDirection = getNextAdjacentEmptyLocation(src, desiredDirection);
            if (desiredDirection != Direction.NONE) {
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
    //TODO: bytecode optimize this call so that
    //only one call to senseNearbyGameObjects needs to be made each turn.
    Vector netForceAt(MapLocation src, ArrayList<RobotInfo> allyRobots, ArrayList<RobotInfo> enemyRobots, int mode) throws Exception {
        Vector netForce = new Vector();
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
                enemyHQForce = enemyHQForce.log(3.9+1.2, 4);
                allyHQForce = allyHQForce.log(6, 2);
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
                break;
            case 1:
                //Gather mode:
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
                    allyHQForce = Vector.getForceVector(src, allyHQLocation).logistic(2, 1, 0);
                } else {
                    allyHQForce = new Vector();
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
                }
                break;
            case 2:
                //Go home to allied HQ.
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
        //netForce.add(allyForce);
        //netForce.add(enemyForce);
        netForce.add(allyHQForce);
        netForce.add(enemyHQForce);
        //netForce.add(pheromoneForce);
        return netForce;
    } 
    /*
    void moveToLocation(MapLocation src, MapLocation location){
        try {
            Direction moveDirection = src.directionTo(location);
            if (rc.canMove(moveDirection)) {
                rc.move(moveDirection);
            } else {
                moveDirection = getNextAdjacentEmptyLocation(src, moveDirection);
                if (moveDirection != Direction.NONE) {
                    rc.move(moveDirection);
                }
            }
        } catch(Exception e) {
            System.err.println(e + " Pirate Exception");
            e.printStackTrace();
        }
    }
    */
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
                    if (bestTarget.type != RobotType.SOLDIER &&
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
