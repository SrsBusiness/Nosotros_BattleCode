package team090;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

abstract class Role{
    static final int WAYPOINT_DISTANCE = 1;
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
    Direction enemyDir;
    Team myTeam;
    Team notMyTeam;
    double pheromoneStrength = 0;
    LinkedList<MapLocation> wayPoints;
    MapLocation myLocation;
    int keepaliveChannel = -1;

    int aggression;
    MapLocation target = null;
    //Pheromone trail for pathfinding (private to each robot).
    Queue<MapLocation> myTrail = new LinkedList<MapLocation>();
    boolean findPath(RobotController rc, MapLocation dest){ // A* algorithm, sets up waypoints
        // if path found, returns true, else returns false.
        class QEntry{
            QEntry(MapLocation loc, double fScore){
                this.loc = loc;
                this.fScore = fScore;
            }
            MapLocation loc;
            double fScore;
        }
        HashSet<MapLocation> evaluated = new HashSet<MapLocation>();
        PriorityQueue<QEntry> toEvaluate = new PriorityQueue<QEntry>(mapWidth + mapHeight, new Comparator<QEntry>(){
            public int compare(QEntry o1, QEntry o2){
                return (int)(o1.fScore - o2.fScore);
            }
        });
        HashSet<MapLocation> toEvalSet = new HashSet<MapLocation>();
        //HashSet<MapLocation> toEvaluate = new HashSet<MapLocation>();
        //toEvaluate.add(myLocation);
        HashMap<MapLocation, MapLocation> cameFrom = new HashMap<MapLocation, MapLocation>();
        HashMap<MapLocation, Double> gScore = new HashMap<MapLocation, Double>();
        gScore.put(myLocation, 0.0);
        toEvaluate.add(new QEntry(myLocation, 0.0)); // first node shouldn't need a heuristic
        toEvalSet.add(myLocation);
        MapLocation current;
        while(toEvaluate.size() > 0){
            current = toEvaluate.poll().loc;
            toEvalSet.remove(current);
            if(current.equals(dest)){
                path(cameFrom, dest);
                return true;
            }
            evaluated.add(current);
            for(MapLocation loc : MapLocation.getAllMapLocationsWithinRadiusSq(current, 1)){
                TerrainTile tmp = rc.senseTerrainTile(loc);
                switch(tmp){
                    case OFF_MAP: case VOID:
                        continue;
                    default:
                        if(evaluated.contains(loc))
                            continue;
                        double tGScore = gScore.get(current) + 
                            (tmp.equals(TerrainTile.ROAD) ? .5 : 1) * 
                            Math.sqrt(current.distanceSquaredTo(loc));
                        if(!toEvalSet.contains(loc) || tGScore < gScore.get(loc)){
                            cameFrom.put(loc, current);
                            gScore.put(loc, tGScore);
                            toEvaluate.add(new QEntry(loc, heuristic(loc, dest, gScore)));
                            toEvalSet.add(loc);
                        }
                        break;
                }
            }
        }
        return false;
    }
    
    void path(HashMap<MapLocation, MapLocation> cameFrom, MapLocation dest){
        wayPoints = new LinkedList<MapLocation>();
        int i = 0;  
        while(cameFrom.containsKey(dest)){
            if(i++ % WAYPOINT_DISTANCE == 0)
                wayPoints.add(0, dest);
            dest = cameFrom.get(dest);
        }
    }
    
    private double heuristic(MapLocation current, MapLocation dest, HashMap<MapLocation, Double> gScore){
        return Math.sqrt(current.distanceSquaredTo(dest)) + gScore.get(current); 
    }
    /* oopsident, plz ignore
    static private class TreeValueMap<K, V extends Comparable<? super V>>{
        private class KeyValue{
            K key;
            V value;
            KeyValue(K key, V value){
                this.key = key;
                this.value = value;
            }
            
                return key.hashCode();
            public int hashCode(){
            }

            public boolean equals(Object o){
                if(o == null)
                    return false;
                if(!(o instanceof TreeValueMap.KeyValue))
                    return false;
                return key.equals(((TreeValueMap.KeyValue)o).key);

            }
        }
        TreeSet<KeyValue> pairs;
        TreeValueMap(){
            pairs = new TreeSet<KeyValue>(new Comparator<KeyValue>(){
                public int compare(KeyValue o1, KeyValue o2){
                    return o1.value.compareTo(o2.value);
                }
            }); 
        }

        void put(K key, V value){
            pairs.add(new KeyValue(key, value));
        }

        boolean containsKey(K key){
            return pairs.contains(new KeyValue(key, null));
        }

        K getLowest(){ // returns key with lowest value
            return pairs.first().key;
        }
    }

    public static void main(){
        TreeValueMap<Integer, Integer> map = new TreeValueMap<Integer, Integer>();
        for(int i = 0; i < 100; i++)
            map.put(i, 99 - i);
        System.out.println(map.getLowest());
    }
     */
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
        enemyDir = allyHQLocation.directionTo(enemyHQLocation);
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
        Direction desiredDirection;
        pheromoneStrength = 0;
        /*
        for (MapLocation p: myTrail) {
            if (src.distanceSquaredTo(p) < 4) {
                //GA TODO: ++ bad, += param good.
                pheromoneStrength++;
            }
        }
        */
        if (pheromoneStrength > 0) {
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
            if ((desiredDirection != Direction.NONE) && 
                (rc.canMove(desiredDirection))) {
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
        double nearbyAllyCount = 0;
        double nearbyEnemyCount = 0;
        for (RobotInfo info: allyRobotInfo) {
            if (src.distanceSquaredTo(info.location) < 36) {
                nearbyAllyCount += (info.health*info.health/10000.0);
            }
        }
        //TODO: see if this makes a difference.
        for (RobotInfo info: enemyRobotInfo) {
            if (src.distanceSquaredTo(info.location) < 36) {
                nearbyEnemyCount += (info.health*info.health/10000.0);
            }
        }
        if (nearbyAllyCount < nearbyEnemyCount ||
            (nearbyEnemyCount > 1 && nearbyAllyCount < nearbyEnemyCount+1)) {
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
                                allyForce.add(Vector.getForceVector(src, i.location).logistic(-1, 2, 0));
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
                //TODO: this is redundant with case(3) now.
                //Go home to allied HQ.
                enemyHQForce = new Vector();
                //GA TODO: parameter tweak to work well with the pheromone forces.
                allyHQForce = allyHQForce.logistic(3, 2, 0);
                break;
            case 3:
                //Go to target mode.
                //Have the enemy HQ only repel.
                allyHQForce = new Vector();
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
                                enemyForce.add(Vector.getForceVector(src, i.location).log(0, -1));
                                break;
                        }
                    }
                    //TODO: remove this scaling?
                    if (count > 0) {
                        //GA TODO: set scale factor.
                        enemyForce = enemyForce.scale(1.0/count);
                    }
                }
                targetForce = Vector.getForceVector(src, target).logistic(-2, 2, 0);
                if(src.distanceSquaredTo(target) < 17) {
                    return targetForce;
                }
                break;
        }
        if (myTrail.size() > 0) {
            count = 0;
            for (MapLocation p: myTrail) {
                count++;
                if (src.distanceSquaredTo(p) < 17) {
                    //GA TODO: params pls.
                    pheromoneForce.add(Vector.getForceVector(src, p).poly(0, 0, -3.4, 0.5, 0));
                }
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
        for (RobotInfo info : enemyRobotInfo) {
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
        for (MapLocation m : result) {
            System.out.println(m);
        }
        return result;
    }

    int getStructureCost(RobotType structure) {
        switch (structure) {
            case PASTR: return 50;
            case NOISETOWER: return 100;
            default: return 0;
        }
    }

    void keepalive() {
        if (keepaliveChannel == -1) return;
        try {
            rc.broadcast(keepaliveChannel, Clock.getRoundNum());
        } catch (GameActionException e) {
            System.out.println(e + " Role Exception");
        }
    }
}
