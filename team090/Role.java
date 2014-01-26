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
    //NOTE: Careful about this one
    Direction enemyDir;
    Team myTeam;
    Team notMyTeam;
    double pheromoneStrength = 0;
    double health;

    MapLocation target = null;
    //Pheromone trail
    Queue<MapLocation> myTrail = new LinkedList<MapLocation>();

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
        //NOTE: Careful about this one
        enemyDir = allyHQLocation.directionTo(enemyHQLocation);
    }
    Direction nextAdjacentEmptyLocation(MapLocation src, Direction initial) {
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
    //This function calculates if the local engagement is favorable for the team.
    //Does not decide on positioning, only local team aggresion.
    //NOTE: Assumes that all robotInfo in the arraylist is in range.
    double calculateAdvantage(MapLocation src,
                              ArrayList<RobotInfo> allyRobotInfo,
                              ArrayList<RobotInfo> enemyRobotInfo) {
        int nearbyAllyCount = 0;
        double nearbyAllyAggregateHealth = 0;
        int nearbyEnemyCount = 0;
        double nearbyEnemyAggregateHealth = 0;

        for (RobotInfo info: allyRobotInfo) {
            //GA TODO: paramize. (5)
            if (info.location.distanceSquaredTo(src) < 36) {
                nearbyAllyCount++;
                //Weigh high health units much higher.
                nearbyAllyAggregateHealth += info.health*info.health;
            }
        }
        for (RobotInfo info: enemyRobotInfo) {
            if (info.location.distanceSquaredTo(src) < 36) {
                nearbyEnemyCount++;
                //Weigh high health units much higher.
                nearbyEnemyAggregateHealth += info.health*info.health;
            }
        }
        //Eval function
        //GA TODO: ax+b - cy+d
        return nearbyAllyAggregateHealth - nearbyEnemyAggregateHealth;
    }
    //Vector Field Go To//
    //Go to and surround the target, keeping a given distance.
    //NOTE: requires target to be set.
    Vector VFcharge(MapLocation src,
                    ArrayList<RobotInfo> allyRobotInfo,
                    ArrayList<RobotInfo> enemyRobotInfo,
                    double range) throws Exception {
        int count;
        Vector netForce = new Vector();
        Vector allyForce = new Vector();
        Vector enemyForce = new Vector();
        Vector targetForce = new Vector();
        if (range > 0) {
            targetForce = Vector.getForceVector(src,
                    target).step(range, 16, -6);
        } else {
            targetForce = Vector.getForceVector(src,
                    target).logistic(0, 12, 0);
        }
        count = 0;
        for (RobotInfo info: allyRobotInfo) {
            if (info.location.distanceSquaredTo(src) < 36) {
                if (info.type == RobotType.SOLDIER) {
                    count++;
                    //GA TODO: paramaterize weights.
                    allyForce.add(Vector.getForceVector(src,
                                info.location).log(2.4, 0.5));
                }
            }
        }
        count = 0;
        for (RobotInfo info: enemyRobotInfo) {
            if (info.location.distanceSquaredTo(src) < 36) {
                switch (info.type) {
                    case SOLDIER:
                        count++;
                        //GA TODO: paramaterize weights.
                        enemyForce.add(Vector.getForceVector(src,
                                    info.location).log(0, -0.3));
                        break;
                    case PASTR:
                        enemyForce.add(Vector.getForceVector(src,
                                    info.location).log(-1, 4));
                }
            }
        }
        if (count > 0) {
            enemyForce = enemyForce.scale(2.0/count);
        }
        netForce.add(allyForce);
        netForce.add(enemyForce);
        netForce.add(targetForce);
        return netForce;
    }
    //Vector Field Go To and Regroup//
    //Go to nearby allies, also gravitate towards the target.
    //NOTE: requires target to be set.
    Vector VFregroup(MapLocation src,
                     ArrayList<RobotInfo> allyRobotInfo) throws Exception {
        //Stay together with allied soldiers and go to the target location.
        Vector netForce = new Vector();
        Vector allyForce = new Vector();
        Vector targetForce = Vector.getForceVector(src, target).log(0, 2);

        //count = 0;
        for (RobotInfo info: allyRobotInfo) {
            switch (info.type) {
                case SOLDIER: 
                    //           count++;
                    allyForce.add(Vector.getForceVector(src,
                                info.location).log(1, 2));
                    break;
                case PASTR:
                    //           count++;
                    allyForce.add(Vector.getForceVector(src,
                                info.location).log(5, 2));
                    break;
            }
        }
        netForce.add(allyForce);
        netForce.add(targetForce);
        return netForce;
    }
    //Vector Field Flee//
    //Runs to target if no enemies are in sight.
    Vector VFflee(MapLocation src,
                  ArrayList<RobotInfo> enemyRobotInfo) throws Exception {
        Vector netForce = new Vector();
        Vector enemyForce = new Vector();
        //GA TODO: parameterize.
        Vector targetForce = Vector.getForceVector(src,
                target).logistic(3, 1, 1);

        //count = 0;
        for (RobotInfo info: enemyRobotInfo) {
            if (info.location.distanceSquaredTo(src) < 36) {
                if (info.type == RobotType.SOLDIER) {
                    //      count++;
                    enemyForce.add(Vector.getForceVector(src,
                                info.location).inv(-2, 0, -1));
                }
            }
        }
        netForce.add(enemyForce);
        netForce.add(targetForce);
        return netForce; 
    }
    Vector getPheremoneForce(MapLocation src) {
        Vector pheromoneForce = new Vector();
        //count = 0;
        if (myTrail.size() > 0) {
            for (MapLocation pheromone: myTrail) {
                if (src.distanceSquaredTo(pheromone) > 9) {
                    continue;
                }
                //count++;
                //GA TODO: params pls.
                pheromoneForce.add(Vector.getForceVector(src,
                            pheromone).inv(-3.4, 0.5, 0));
            }
        }
        return pheromoneForce;
    }
    void tryMove(MapLocation src,
                 ArrayList<RobotInfo> allyRobotInfo,
                 ArrayList<RobotInfo> enemyRobotInfo,
                 int mode,
                 //Param is range for VFcharge, ignored if mode != 0
                 double param) throws Exception {
        Vector force = new Vector();
        Direction desiredDirection;
        boolean onPheromone = false;
        for (MapLocation pheromone: myTrail)
            if (pheromone.equals(src))
                onPheromone = true;
      //while (rc.getActionDelay() > 1) {
      //    rc.yield();
      //}
        switch (mode) {
            case 0:
                force = VFcharge(src, allyRobotInfo, enemyRobotInfo, param);
                break;
            case 1:
                force = VFregroup(src, allyRobotInfo);
                break;
            case 2:
                force = VFflee(src, enemyRobotInfo);
                break;
            default:
                System.out.printf("Invalid mode selected: %d\n", mode);
                force = VFflee(src, enemyRobotInfo);
                break;
        }
        desiredDirection = force.toDirectionEnum();
        if (rc.canMove(desiredDirection)) {
            rc.move(desiredDirection);
        } else {
            //Choose the next available location to escape the local minima.
            desiredDirection = nextAdjacentEmptyLocation(src, desiredDirection);
            if ((desiredDirection != Direction.NONE) && 
                (rc.canMove(desiredDirection))) {
                rc.move(desiredDirection);
            }
        }
        //Lay down pheromone trail.
        myTrail.offer(src);
        //GA TODO: parameterize the trail size.
        if (myTrail.size() > 4) {
            myTrail.remove();
        }
    }
    //Choose the best robot to attack.
    //Priority: SOLDIERS > PASTRS > NOISETOWER, HP.
    //Do not attack HQ (futile).
    RobotInfo bestTargetInRange(MapLocation src,
            ArrayList<RobotInfo> enemyRobotInfo) throws Exception {
        double lowestHP = (double)Integer.MAX_VALUE;
        RobotInfo bestTarget = null;
        for (RobotInfo info : enemyRobotInfo) {
            //Do not consider things out of range.
            if (src.distanceSquaredTo(info.location) > 10) {
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
                    if ((bestTarget == null ||
                         bestTarget.type != RobotType.SOLDIER) &&
                        info.health < lowestHP) {
                        lowestHP = info.health;
                        bestTarget = info;
                    }
                    break;
                case NOISETOWER:
                    if ((bestTarget == null ||
                         (bestTarget.type != RobotType.SOLDIER &&
                          bestTarget.type != RobotType.PASTR)) &&
                        info.health < lowestHP) {
                        lowestHP = info.health;
                        bestTarget = info;
                    }
                    break;
            }
        }
        return bestTarget;
    }
    MapLocation[] corners() {
        class LocComparator implements Comparator<MapLocation> {
            public int compare(MapLocation l1, MapLocation l2){
                return l2.distanceSquaredTo(enemyHQLocation) - l1.distanceSquaredTo(enemyHQLocation);
            }
        }
        MapLocation[] result = new MapLocation[]{
            new MapLocation(3, 3), 
            new MapLocation(3, mapHeight - 4), 
            new MapLocation(mapWidth - 4, 3), 
            new MapLocation(mapWidth - 4, mapHeight - 4)};
        Arrays.sort(result, new LocComparator());
        return result;
    }
}
