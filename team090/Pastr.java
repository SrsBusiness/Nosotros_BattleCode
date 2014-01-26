package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Pastr extends Role{
    Pastr(RobotController rc) {
        super(rc);
        keepaliveChannel = 6;
        System.out.println("It begins: Milk, milk, milk.\n");
    }
    void execute() {
        try {
            keepalive();
        } catch(Exception e) {
            System.err.println(e + " PASTR Exception");
            e.printStackTrace();
        }
    }
}
