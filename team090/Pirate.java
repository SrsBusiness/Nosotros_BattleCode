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
    boolean atCorner;
    boolean found;
    boolean xDir, yDir;
    int height, width;
    Pirate(RobotController rc, int mode){
        super(rc);
        while(!rc.isActive());
        xDir = Direction.EAST;
        yDir = Direction.NORTH;
        MapLocation[] corners = corners(rc);
        corner = corners[mode];
        height = rc.getMapHeight();
        width = rc.getMapWidth();
    }

    void execute(){
        if(!found){
            if(!atCorner)
                moveToCorner();
            else{
                patrol();
            }
        }else{
            ; 
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
            new MapLocation(3, height - 4), 
            new MapLocation(width - 4, 3), 
            new MapLocation(width - 4, height - 4)};
        Arrays.sort(result, new LocComparator());
        return result;
    }
    void moveToCorner(RobotController rc, MapLocation corner){
        try {
            rc.move(rc.getLocation().directionTo(corner));
        } catch(Exception e) {
            System.err.println(e + " Pirate Exception");
        }
    }
    void patrol(){
        MapLocation current = rc.getLocation();
        if(current.y == 0 && yDir || current.y == height - 1 && !yDir){
            yDir = !yDir;
            if(current.x == 0 && xDir || current.x == width - 1 && !xDir)
                xDir = !xDir;
            rc.move(xDir ? Direction.WEST : Direction.EAST);
        }
        rc.move(yDir ? Direction.NORTH : Direction.SOUTH);
    }
}
