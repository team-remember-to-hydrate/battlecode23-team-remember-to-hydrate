package latest;

import battlecode.common.*;

import java.util.ArrayList;

public class Sensing {

    // Last position scanned from
    public static MapLocation lastScannedFrom = null;

    /**
     * Scans for friendly HQ Location in vision range
     * @param rc Robot Player
     * @return Location of first sensed friendly HQ.
     * @throws GameActionException
     */
    public static MapLocation scanSingleFriendlyHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS){
                return robot.getLocation();
            }
        }
        return null;
    }

    // Scan all nearby enemies

    /**
     * Scans for all enemies in vision radius, using less bytecode if a scan already occurred this turn (retains most
     * recent scan).
     * @param rc
     * @return Returns RobotInfo[] of all enemies in vision range.
     * @throws GameActionException
     */
    public static RobotInfo[] smartScanEnemies(RobotController rc) throws GameActionException {
        if (RobotPlayer.lastRoundScannedEnemies != rc.getRoundNum()){
            RobotPlayer.scannedEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent()); //102 bc
            RobotPlayer.lastRoundScannedEnemies = rc.getRoundNum();
        }
        return RobotPlayer.scannedEnemies;
    }


    // Filter to nearby combat enemies

    /**
     * Smart scans for all enemies in vision range and returns those that are Launchers or Destabilizers.
     * @param rc
     * @return ArrayList of all visible enemy launchers and destabilizers.
     * @throws GameActionException
     */
    public static ArrayList<RobotInfo> scanCombatEnemies(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = smartScanEnemies(rc);
        ArrayList<RobotInfo> enemyCombatRobots = new ArrayList<RobotInfo>();
        int enemyRobotsLength = enemyRobots.length; // saves bytecode
        for (int i = 0; i < enemyRobotsLength; i++){
            //RobotInfo targetBot = enemyRobots[i];
            if (enemyRobots[i].getType() == RobotType.LAUNCHER || enemyRobots[i].getType() == RobotType.DESTABILIZER){
                enemyCombatRobots.add(enemyRobots[i]);
            }
        }
        return enemyCombatRobots;
    }

    // Return weakest Robot from set

    // Filter to nearby econ enemies

    // Sense enemy Carriers with Anchors

    // Scan all nearby wells

    // ____________Below this are things that won't change during a game________
    // Only need to sense each of these once (per bot). Skip sensing these if you sensed last turn and didn't move.

    // Scan all newly visible island squares
    // Decided to abandon this, as at a vision range of 20 it would take over 200 bytecode to senseIsland on each newly
    //  visible square.

    // Scan all nearby walls
    public static ArrayList<MapLocation> scanNearbyWalls(RobotController rc) throws GameActionException {
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();
        int length = nearbyInfos.length;
        ArrayList<MapLocation> impassableSpaces = new ArrayList<MapLocation>();
        for (int i = 0; i < length; i++){
            if (!nearbyInfos[i].isPassable()){
                impassableSpaces.add(nearbyInfos[i].getMapLocation());
            }
        }
        return impassableSpaces; // TODO: Confirm impassable spaces don't include bots (otherwise filter).
    }

    // Scan all nearby clouds

    // Scan all nearby currents
    //  Array of [dir 1] [direction] bitpacked.
}
