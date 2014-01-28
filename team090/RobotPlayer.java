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
    static int x;
    static int y;
    
    //TODO: make this nicer.
    private static void setCurrentRole(RobotController rc, RobotType type, int mode) {
        switch (type) {
            case HQ:
                currentRole = new HQ(rc);
                break;
            case SOLDIER:
                switch (mode) {
                    case 0:
                        currentRole = new Infantry(rc);
                        break;
                    case 1:
                        try {
                            x = rc.readBroadcast(3);
                            y = rc.readBroadcast(4);
                            currentRole = new TowerBuilder(rc, x, y, RobotType.PASTR);
                        } catch (Exception e) {
                            System.err.println(e + " Tried to create pastrMaker. Failed.");
                        }
                        break;
                    case 2:
                        //Deprecated.
                        currentRole = new Infantry(rc);
                        break;
                    case 3: case 4:
                        currentRole = new Pirate(rc);
                        break;
                    case 5:
                        try {
                            x = rc.readBroadcast(3);
                            y = rc.readBroadcast(4);
                            currentRole = new TowerBuilder(rc, x, y, RobotType.NOISETOWER);
                        } catch (Exception e) {
                            System.err.println(e + " Tried to create noisetowerMaker. Failed.");
                        }
                        break;
                }
                break;
            case NOISETOWER:
                currentRole = new NoiseTower(rc);
                break;
            case PASTR:
                currentRole = new Pastr(rc);
                break;
        }
    }

    public static void run(RobotController rc) throws GameActionException {
        //Initialize Role
        myType = rc.getType();
        int newMode;
        if(rc != null) {
            setCurrentRole(rc, myType, 0);
        } else {
            //This should never happen. Be on the lookout.
            return;
        }
        if (myType == RobotType.SOLDIER) {
            rc.broadcast(0, rc.getRobot().getID());
        }            
        while(true) {
            newMode = 0;
            lifeTurn++;
            try {
                //Only SOLDIERs recieve orders, for now.
                if (myType == RobotType.SOLDIER) {
                    if (rc.readBroadcast(1) == rc.getRobot().getID()) {
                        //Poll for commands from the HQ
                        newMode = rc.readBroadcast(2);
                        // Probably unnessesary clearing of channels
                        rc.broadcast(1, 0);
                        rc.broadcast(2, 0);
                    }

                    //When a different mode is broadcasted, change accordingly.
                    //OR, when the RobotType changes, change the role accordingly.
                    //TODO: bytecode optimize this.
                    if (mode != newMode ||
                        rc.getType() != RobotType.SOLDIER) {
                        myType = rc.getType();
                        setCurrentRole(rc, myType, newMode);
                    }
                }
            } catch (Exception e) {
                System.err.println(e + " RobotPlayer Exception");
            }
            //Bytecode cost: 48 up to here (as of 64fca1348b7d7f60bb3d131a09b03389c85bdb6b)
            if(rc.isActive()) {
                currentRole.execute();
            }
            rc.yield();
        }
    }
}
