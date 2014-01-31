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
    Team myTeam;
    Team notMyTeam;
    //NOTE: Refers to direction from AllyHQ to EnemyHQ.
    Direction enemyDir;

    MapLocation target;

    Queue<MapLocation> myTrail = new LinkedList<MapLocation>();

    int keepaliveChannel = -1;

    LinkedList<MapLocation> wayPoints;

    //NOTE: Unreliable.
    MapLocation currLoc;
    double currHP;

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
        rand = new Random();
    }
    // A* algorithm, sets up waypoints
    // If path found, returns true, else returns false.
    boolean findPath(MapLocation src, MapLocation dest){ 
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
        //toEvaluate.add(src);
        HashMap<MapLocation, MapLocation> cameFrom = new HashMap<MapLocation, MapLocation>();
        HashMap<MapLocation, Double> gScore = new HashMap<MapLocation, Double>();
        gScore.put(src, 0.0);
        toEvaluate.add(new QEntry(src, 0.0)); // first node shouldn't need a heuristic
        toEvalSet.add(src);
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
    /*
    DEPRECATED
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
            if (info.location.distanceSquaredTo(src) < 16 &&
                info.type == RobotType.SOLDIER) {
                nearbyAllyCount++;
                //Weigh high health units much higher.
                nearbyAllyAggregateHealth += info.health;
            }
        }
        for (RobotInfo info: enemyRobotInfo) {
            if (//info.location.distanceSquaredTo(src) < 16 &&
                info.type == RobotType.SOLDIER) {
                nearbyEnemyCount++;
                //Weigh high health units much higher.
                nearbyEnemyAggregateHealth += info.health;
            }
        }
        //Eval function
        //GA TODO: ax+b - cy+d
        return nearbyAllyAggregateHealth+currHP - nearbyEnemyAggregateHealth;
    }
    */
    //Vector Field Go To//
    //Go to and surround the destination, keeping a given distance.
    Vector VFcharge(MapLocation src, MapLocation dst,
                    ArrayList<RobotInfo> allyRobotInfo,
                    ArrayList<RobotInfo> enemyRobotInfo) throws Exception {
        int count;
        Vector netForce = new Vector();
        Vector allyForce = new Vector();
        Vector enemyForce = new Vector();
        Vector targetForce = new Vector();
        targetForce = Vector.getForceVector(src, dst).logistic(0, 8, 0);
        //count = 0;
        for (RobotInfo info: allyRobotInfo) {
            //count++;
            //GA TODO: paramaterize weights.
            allyForce.add(Vector.getForceVector(src,
                        info.location).log(2.4, 0.8));
        }
        count = 0;
        for (RobotInfo info: enemyRobotInfo) {
            count++;
            //GA TODO: paramaterize weights.
            enemyForce.add(Vector.getForceVector(src,
                        info.location).log(0, -0.3));
            break;
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
    //Go to nearby allies, also gravitate towards the destination.
    Vector VFregroup(MapLocation src, MapLocation dst,
                     ArrayList<RobotInfo> allyRobotInfo) throws Exception {
        //Stay together with allied soldiers and go to the destination.
        Vector netForce = new Vector();
        Vector allyForce = new Vector();
        Vector targetForce = Vector.getForceVector(src, dst).log(0, 2);

        //count = 0;
        for (RobotInfo info: allyRobotInfo) {
            //           count++;
            allyForce.add(Vector.getForceVector(src,
                        info.location).log(1, 2));
        }
        netForce.add(allyForce);
        netForce.add(targetForce);
        return netForce;
    }
    //Vector Field Flee//
    //Runs to destination if no enemies are in sight.
    Vector VFflee(MapLocation src, MapLocation dst,
                  ArrayList<RobotInfo> enemyRobotInfo) throws Exception {
        Vector enemyForce = new Vector();
        //count = 0;
        for (RobotInfo info: enemyRobotInfo) {
            //      count++;
            enemyForce.add(Vector.getForceVector(src,
                        info.location).inv(-6, 0, 0));
        }
        if (enemyForce.getX() != 0 && enemyForce.getY() != 0) {
            return enemyForce;
        } else {
            return Vector.getForceVector(src,
                    allyHQLocation).logistic(3, 0.1, 1);
        }
    }
    //Vector Field A-move//
    //Runs to target enemy.
    Vector VFaggro(MapLocation src,
                   ArrayList<RobotInfo> enemyRobotInfo) {
        int count;
        double minHealth = (double)Integer.MAX_VALUE;
        RobotInfo robotTarget = (enemyRobotInfo.size() > 0)?
                                enemyRobotInfo.get(0):null;
        //Vector netForce = new Vector();
        Vector enemyForce = new Vector();
        count = 0;
        for (RobotInfo info: enemyRobotInfo) {
            if (info.health < minHealth) {
                minHealth = info.health;
                robotTarget = info;
            }
        }
        if (robotTarget != null) {
            enemyForce.add(Vector.getForceVector(src,
                        robotTarget.location).log(0, 4));
        }
        //netForce.add(enemyForce);
        //return netForce;
        return enemyForce;
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
                            pheromone).inv(-5, 0.5, -1));
            }
        }
        return pheromoneForce;
    }
    void tryMove(MapLocation src, MapLocation dst,
                 ArrayList<RobotInfo> allyRobotInfo,
                 ArrayList<RobotInfo> enemyRobotInfo,
                 int mode) throws Exception {
        Vector force = new Vector();
        Direction desiredDirection;
        boolean nearWall = false;
        //Use pheremones if touching a wall.
        if (rc.senseTerrainTile(src.add(Direction.NORTH)).ordinal() > 1 ||
            rc.senseTerrainTile(src.add(Direction.WEST)).ordinal() > 1 ||
            rc.senseTerrainTile(src.add(Direction.SOUTH)).ordinal() > 1 ||
            rc.senseTerrainTile(src.add(Direction.EAST)).ordinal() > 1)
            nearWall = true;
        switch (mode) {
                //Retreat
            case -1:
                force = VFflee(src, dst, enemyRobotInfo);
                break;
                //Stay put
            case 0:
                break;
                //Inch forward
            case 1:
                force = VFregroup(src, dst, allyRobotInfo);
                break;
                //Charge to base
            case 2:
                force = VFcharge(src, dst, allyRobotInfo, enemyRobotInfo);
                break;
                //A-move
            case 3:
                force = VFaggro(src, enemyRobotInfo);
                break;
                //S
            default:
                System.out.printf("Invalid mode selected: %d\n", mode);
                force = VFflee(src, dst, enemyRobotInfo);
                break;
        }
        if (nearWall) {
            force.add(getPheremoneForce(src));
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
        if (myTrail.size() >= 5) {
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
