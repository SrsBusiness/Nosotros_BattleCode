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
        MapLocation myLoc = rc.getLocation();
        MapLocation enemyHQLoc = rc.senseEnemyHQLocation();

        Vector enemyHQForce = new Vector(0.0, 0.0);
        Vector alliedForce = new Vector(0.0, 0.0);
        Vector enemyForce = new Vector(0.0, 0.0);

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
                enemyHQForce = getForceVector(myLoc, enemyHQLoc);
                //enemyHQForce.applyLogistic(15);
                if (alliedRobots.length != 0) {
                    for (Robot r: alliedRobots) {
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                alliedForce.addVector(getForceVector(myLoc, info.location));
                                break;
                            case PASTR:
                                //count++;
                                //alliedForceVector.addVector(getForceVector(myLoc, info.location, 12, -18));
                                break;
                            case HQ: 
                                count++;
                                alliedForce.addVector(getForceVector(myLoc, info.location));
                                break;
                        }
                    }
                    alliedForce.scale(1.0/count);
                }
                if (enemyRobots.length != 0) {
                    count = 0;
                    for (Robot r: enemyRobots) {
                        //TODO: add different mode responses and aggression parameter.
                        RobotInfo info = rc.senseRobotInfo(r);
                        switch (info.type) {
                            case SOLDIER: 
                                count++;
                                enemyForce.addVector(getForceVector(myLoc, info.location));
                                break;
                            case PASTR: 
                                enemyForce.addVector(getForceVector(myLoc, info.location));
                                break;
                        }
                    }
                    enemyForce.scale(1.0/count);
                }
                break;
            case 1:
                break;
        }
        bestDir.addVector(enemyHQForce);
        bestDir.addVector(alliedForce);
        bestDir.addVector(enemyForce);
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
