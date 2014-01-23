package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Pirate extends Role{
    boolean patrolDir;
    MapLocation corner;
    MapLocation pastr;
    boolean atTarget;
    boolean found;
    boolean xDir, yDir;
    int height, width;
    MapLocation myLocation;
    MapLocation[] corners;
    MapLocation nextWaypoint;

    Pirate(RobotController rc, int mode){
        super(rc);
        while(!rc.isActive());
        corners = corners(rc);
        corner = corners[mode];
        height = rc.getMapHeight();
        width = rc.getMapWidth();

        nextWaypoint = corner;
    }

    void execute(){
        if (rc.isActive()) {
            myLocation = rc.getLocation();
            //if(!found){
                if(!atTarget) {
                    //moveToLocation(rc, nextWaypoint);
                    if (myLocation.equals(corner)) {
                        atTarget = true;
                    }
                } else {
                    atTarget = false;
                    patrol();
                }
            //} else {
            //    ; 
            //}
        }
    }
    MapLocation[] corners(final RobotController rc){
        class LocComparator implements Comparator<MapLocation>{
            public int compare(MapLocation l1, MapLocation l2){
                MapLocation enemy = rc.senseEnemyHQLocation();
                return l2.distanceSquaredTo(enemy) - l1.distanceSquaredTo(enemy);
            }
        }
        MapLocation[] result = new MapLocation[]{new MapLocation(3, 3), 
            new MapLocation(3, mapHeight - 4), 
            new MapLocation(mapWidth - 4, 3), 
            new MapLocation(mapWidth - 4, mapHeight - 4)};
        Arrays.sort(result, new LocComparator());
        for (MapLocation m : result) {
            System.out.println(m);
        }
        return result;
    }
    void patrol(){
        try{
            corner = corners[rand.nextInt(corners.length)];
            /*
            MapLocation current = rc.getLocation();
            if(current.y == 0 && yDir || current.y == height - 1 && !yDir){
                yDir = !yDir;
                if(current.x == 0 && xDir || current.x == width - 1 && !xDir)
                    xDir = !xDir;
                rc.move(xDir ? Direction.WEST : Direction.EAST);
            }
            rc.move(yDir ? Direction.NORTH : Direction.SOUTH);
            */
        }catch(Exception e){
            System.err.println(e + " Pirate Exception");
        }
    }
}
