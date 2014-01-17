package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {

    private static Vector getForceVector(MapLocation src, MapLocation dst) {
        Vector force = new Vector(dst.x - src.x, dst.y - src.y);
        double r = Math.pow(force.getMagnitudeSq(), 0.5);
        return force;
    }

    public static Direction chooseDir(RobotController rc) throws Exception{
        //TODO: pheremone trail
        //TODO: fill in terrain to prevent getting stuck.
        //Use potential fields to judge next position.
        int mode = 0; //Different modes have different parameters
        Vector bestDir = new Vector(0.0, 0.0);

        Team myTeam = rc.getTeam();
        MapLocation myLocation = rc.myLocation();
        MapLocation enemyHQLocation = rc.senseEnemyHQLocation();

        Vector enemyHQVector = new Vector(0.0, 0.0);
        Vector alliedForceVector = new Vector(0.0, 0.0);
        Vector enemyForceVector = new Vector(0.0, 0.0);

        Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam.opponent());

        int count;
        switch (mode) {
            //Charge mode:
            //Go to the enemyHQ, but keep a distance.
            //Have all units attempt to camp and surround the enemy base.
            //Don't walk alone blindly either. Walk with buddies.
            case 0:
                count = 0;
                enemyHQVector = getForceVector(myLocation, enemyHQLocation);
                enemyHQVector = enemyHQVector.applyLogistic(15);
                if (allies.length != 0) {
                    for (Robot r: allies) {
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                alliedForceVector.addVector(getForceVector(myLocation, info.location, 6, -15));
                                break;
                            case PASTR:
                                //count++;
                                //alliedForceVector.addVector(getForceVector(myLocation, info.location, 12, -18));
                                break;
                            case HQ: 
                                count++;
                                alliedForceVector.addVector(getForceVector(myLocation, info.location, -3, 0));
                                break;
                        }
                    }
                    alliedForceVector.scale(1.0/count);
                }
                if (enemies.length != 0) {
                    count = 0;
                    for (Robot r: enemies) {
                        //TODO: add different mode responses and aggression parameter.
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                enemyForceVector.addVector(getForceVector(myLocation, info.location, 1, 0));
                                break;
                            case PASTR: 
                                enemyForceVector.addVector(getForceVector(myLocation, info.location, 6, 0));
                                break;
                        }
                    }
                    enemyForceVector.scale(1.0/count);
                }
                break;
            case 1:
                break;
        }
        bestDir.addVector(enemyHQVector);
        bestDir.addVector(alliedForceVector);
        bestDir.addVector(enemyForceVector);
        return bestDir.toDirectionEnum();
    }

    public static Robot getBestTarget(Robot[] robots, RobotController rc) throws Exception {
        //Choose the best robot to attack. Prioritizes low HP units and SOLDIERS over PASTRS. Do not attack HQs (futile).
        double lowest = (double)Integer.MAX_VALUE;
        Robot weakest = null;
        for (Robot r : robots){
            double i;
            RobotInfo rInfo = rc.senseRobotInfo(r);
            if(weakest != null && rc.senseRobotInfo(weakest).type.equals(RobotType.SOLDIER) && !rInfo.type.equals(RobotType.SOLDIER) ){
            }
            if((i = rInfo.health) < lowest){
                lowest = i; 
                weakest = r;
            }
        }
        return weakest;
    }

    public static void run(RobotController rc) {
        switch (rc.getType()) {
            case HQ:
                HQ.run(rc); 
                break;
            case SOLDIER:
                Soldier.run(rc); 
                break;
            case NOISETOWER:
                NoiseTower.run(rc);
                break;
            case PASTR:
                while(true)
                    rc.yield();
        }
    }
}
