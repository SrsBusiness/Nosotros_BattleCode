package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class NoiseTower extends Role {

    // TODO: GA this
    final static int RADIUS_DECREMENT  = 2;
    final static int RADIUS_MINIMUM    = 6;
    final static int RADIUS_MAXIMUM    = 18;
    final static int ROTATE_PARTITIONS = 8;
    final static double ROTATE_RADIAN  = 2*Math.PI/ROTATE_PARTITIONS;

    MapLocation base = null;
    int radius = RADIUS_MAXIMUM;
    int rotation = 0;

    NoiseTower(RobotController rc) {
        super(rc);
        base = rc.getLocation();
        System.out.println("C'est facil; herding cows. For I am the world's tower.");
    }
    void execute() {
        target = getSpiralLocation();
        try {
            if (rc.canAttackSquare(target)) {
                rc.attackSquare(target);
            }
            do {
                radius -= RADIUS_DECREMENT;
                if (radius < RADIUS_MINIMUM) {
                    radius = RADIUS_MAXIMUM;
                    rotation += 1;
                }
                target = getSpiralLocation();
            } while (target.x < 0 || target.y < 0 || target.x > mapWidth || target.y > mapHeight);
        } catch (Exception e) {
            System.err.println(e.toString() + "Noisetower Exception");
        }
        rc.yield();
    }
    public MapLocation getSpiralLocation() {
        int x = (int)(radius * Math.sin(rotation*ROTATE_RADIAN));
        int y = (int)(radius * Math.cos(rotation*ROTATE_RADIAN));
        return base.add(x, y);
    }
}
