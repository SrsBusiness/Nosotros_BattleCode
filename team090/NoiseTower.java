package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class NoiseTower extends Role {
    void execute() {
        try {
            //If first turn, set target
            if (lifeTurn++ == 2) {
                target = rc.getLocation();
                target.add(6,0);
            }
            rc.attackSquareLight(target);
        } catch (Exception e) {
            System.err.println(e.toString() + "Noisetower Exception");
        }
        rc.yield();
    }
}
