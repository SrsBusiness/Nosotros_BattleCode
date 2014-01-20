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
    static int mapWidth, mapHeight;

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
                        currentRole = new Farmer(rc);
                        break;
                    case 2:
                        currentRole = new CattleDriver(rc);
                        break;
                    case 3: case 4:
                        currentRole = new Pirate(rc, mode - 3);
                        break;

                }
                break;
            case NOISETOWER:
                currentRole = new NoiseTower(rc);
                break;
            case PASTR:
                //currentRole = new Pastr();
                break;
        }
    }

    public static void run(RobotController rc) {
        //Initialize Role
        myType = rc.getType();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        setCurrentRole(rc, myType, 0);
        while(true) {
            try{
                int newMode = 0;
                //Have soldiers poll for commands from the HQ
                if (myType == RobotType.SOLDIER && rc.readBroadcast(1) == rc.getRobot().getID()) {
                    newMode = rc.readBroadcast(2);
                    // Probably unnessesary clearing of channels
                    rc.broadcast(1, 0); 
                    rc.broadcast(2, 0);
                }
                //Broadcast SOLDIER ID when spawned, if Soldier type.
                if (++lifeTurn == 2 && myType == RobotType.SOLDIER) {
                    rc.broadcast(0, rc.getRobot().getID());
                }
                if (mode != newMode) {
                    setCurrentRole(rc, rc.getType(), newMode);
                }
                if(rc.isActive()) {
                    currentRole.execute();
                }
                rc.yield();
            }catch(Exception e){
                System.err.println(e + " RobotPlayer Exception");
            }
        }
    }
}
