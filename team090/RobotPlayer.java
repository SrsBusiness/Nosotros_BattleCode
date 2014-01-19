package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    static int mode = 0;
    static RobotType myType;
    static Role currentRole;
    static int lifeTurn = 0;

    private static void setCurrentRole(RobotType type, int mode) {
        switch (type) {
            case HQ:
                currentRole = new HQ();
                break;
            case SOLDIER:
                switch (mode) {
                    case 0:
                        currentRole = new Infantry();
                        break;
                    case 1:
                        currentRole = new Farmer();
                        break;
                    case 2:
                        currentRole = new CattleDriver();
                        break;
                    case 3:
                        currentRole = new Pirate();
                        break;

                }
                break;
            case NOISETOWER:
                currentRole = new NoiseTower();
                break;
            case PASTR:
                //currentRole = new Pastr();
                break;
        }
    }

    public static void run(RobotController rc) {
        //Initialize Role
        myType = rc.getType();
        setCurrentRole(myType, 0);
        while(true) {
            try {
                int newMode = 0;
                if (rc.readBroadcast(1) == rc.getRobot().getID()) {
                    newMode = rc.readBroadcast(2);
                    // Probably unnessesary clearing of channels
                    rc.broadcast(1, 0); 
                    rc.broadcast(2, 0);
                }
                //Broadcast SOLDIER ID when spawned.
                if (myType == RobotType.SOLDIER && ++lifeTurn == 2) {
                    rc.broadcast(0, rc.getRobot().getID());
                }
                if (mode != newMode) {
                    setCurrentRole(rc.getType(), newMode);
                }
                currentRole.execute();
            } catch (Exception e) {
                System.err.println(e.toString() + "RobotPlayer Exception");
            }
            rc.yield();
        }
    }
}
