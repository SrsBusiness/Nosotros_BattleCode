package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class CattleDriver extends Role {
    int lifeTurn = 0;
    int commandMode;
    
    CattleDriver(RobotController rc){
        super(rc);
    }

    void execute() {
        try {
            if (rc.canMove(Direction.WEST) && (rc.getLocation().x+8)%8 != 1) {
                rc.move(Direction.WEST);
            } else {
                rc.construct(RobotType.NOISETOWER);
            }
        } catch(Exception e) {
            System.err.println(e + " CowBoy Exception");
        }
    }
}
