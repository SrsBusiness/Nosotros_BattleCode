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

    Direction getNextAdjacentEmptyLocation(MapLocation myLocation, Direction initial) {
        try {
            //TODO: Figure out how to not bad (10)
            for (Direction d : directions) {
                if (d.equals(initial)) {
                    continue;
                }
                if(rc.senseObjectAtLocation(myLocation.add(d)) == null) {
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
    //senseNearbyGameObjects(Robot.class)
    Direction chooseDirection(MapLocation src, Robot[] surroundingRobots, int mode) throws Exception {
        ArrayList<RobotInfo> allyRobots = new ArrayList<RobotInfo>();
        ArrayList<RobotInfo> enemyRobots = new ArrayList<RobotInfo>();
        Vector North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest;
        boolean isOnPheromone = false;
        RobotInfo info;
        //Separate the robots into enemy and allied.
        for (Robot r: surroundingRobots) {
            info = rc.senseRobotInfo(r);
            if (info.team == myTeam) {
                allyRobots.add(info);
            } else if (info.team == notMyTeam) {
                enemyRobots.add(info);
            }
        }
        //Calculate pheromone forces
        for (Object p: myTrail.toArray()) {
            if (src.equals((MapLocation)p)) {
                isOnPheromone = true;
            }
        }
        if (isOnPheromone) {
            //Calculate min-force obstacle pathfinding.
            return Direction.NONE;
        } else {    
            return netForceAt(src, allyRobots, enemyRobots, mode).toDirectionEnum();
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
                enemyHQForce = enemyHQForce.log(3.9+1.2, 6);
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
                if(enemyHQForce.getMagnitudeSq() < 16) {
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
        netForce.add(allyForce);
        netForce.add(enemyForce);
        netForce.add(allyHQForce);
        netForce.add(enemyHQForce);
        netForce.add(pheromoneForce);
        return netForce;
    } 
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
    Robot getBestTarget(Robot[] robots, RobotController rc) throws Exception {
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
