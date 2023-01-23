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

    /**
     *
     * Scans for all bots from given team in vision radius, using less bytecode if a scan already occurred this turn
     * (retains most recent scan).
     *
     * @param rc
     * @param team
     * @return Returns RobotInfo[] of all bots from given team in vision range.
     * @throws GameActionException
     */
    public static RobotInfo[] smartScanMembersOfTeam(RobotController rc, Team team) throws GameActionException {
        if (team == rc.getTeam().opponent()) {
            if (RobotPlayer.lastRoundScannedEnemies != rc.getRoundNum()){
                // Our old scan is outdated, do a new one.
                RobotPlayer.scannedEnemies = rc.senseNearbyRobots(-1, team); //102 bc
                RobotPlayer.lastRoundScannedEnemies = rc.getRoundNum();
            }
                // Just use our existing scan.
                return RobotPlayer.scannedEnemies;
        }
        else {
            if (RobotPlayer.lastRoundScannedAllies != rc.getRoundNum()){
                // Our old scan is outdated, do a new one.
                RobotPlayer.scannedAllies = rc.senseNearbyRobots(-1, team); //102 bc
                RobotPlayer.lastRoundScannedAllies = rc.getRoundNum();
            }
            // Just use our existing scan.
            return RobotPlayer.scannedAllies;
        }
    }

    /**
     * Smart scans for all members of team in vision range and returns those that are Launchers or Destabilizers.
     * @param rc
     * @return ArrayList of all visible launchers and destabilizers from given team.
     * @throws GameActionException
     */
    public static ArrayList<RobotInfo> scanCombatUnitsOfTeam(RobotController rc, Team team) throws GameActionException {
        RobotInfo[] teamRobots = smartScanMembersOfTeam(rc, team);
        ArrayList<RobotInfo> teamCombatRobots = new ArrayList<RobotInfo>();
        int teamRobotsLength = teamRobots.length; // saves bytecode
        for (int i = 0; i < teamRobotsLength; i++){
            //RobotInfo targetBot = teamRobots[i]; // TODO: Check if bytecode decreases by alternate implementation.
            if (teamRobots[i].getType() == RobotType.LAUNCHER || teamRobots[i].getType() == RobotType.DESTABILIZER){
                teamCombatRobots.add(teamRobots[i]);
            }
        }
        return teamCombatRobots;
    }


    /**
     * Smart scans for all members of team in vision range and returns those that are Carriers or Boosters.
     * @param rc
     * @param team team to get count of visible Econ bots from.
     * @return ArrayList of all visible carriers and boosters from given team.
     * @throws GameActionException
     */
    public static ArrayList<RobotInfo> scanEconUnitsOfTeam(RobotController rc, Team team) throws GameActionException {
        RobotInfo[] teamRobots = smartScanMembersOfTeam(rc, team);
        ArrayList<RobotInfo> teamEconRobots = new ArrayList<RobotInfo>();
        int teamRobotsLength = teamRobots.length; // saves bytecode
        for (int i = 0; i < teamRobotsLength; i++){
            //RobotInfo targetBot = teamRobots[i]; // TODO: Check if bytecode decreases by alternate implementation.
            if (teamRobots[i].getType() == RobotType.CARRIER || teamRobots[i].getType() == RobotType.BOOSTER){ //||
                    //teamRobots[i].getType() == RobotType.HEADQUARTERS){ // Removing this to prevent accidental targeting of HQs.
                teamEconRobots.add(teamRobots[i]);
            }
        }
        return teamEconRobots;
    }

    /**
     * Smart scans for the first sensed carrier from given team in vision range that has an anchor and returns it.
     * @param rc
     * @param team team to find a visible anchor carrier from
     * @return RobotInfo of anchor carrier from given team.
     * @throws GameActionException
     */
    public static RobotInfo scanAnchorCarrierOfTeam(RobotController rc, Team team) throws GameActionException {
        RobotInfo[] teamRobots = smartScanMembersOfTeam(rc, team);
        int teamRobotsLength = teamRobots.length; // saves bytecode
        for (int i = 0; i < teamRobotsLength; i++){
            if (teamRobots[i].getType() == RobotType.CARRIER && teamRobots[i].getTotalAnchors() > 0){ //||
                return teamRobots[i];
            }
        }
        return null;
    }


    // Return weakest Robot from set
    public static RobotInfo scanWeakestBotInGroup(RobotController rc, RobotInfo[] botGroup) {
        int lengthBotGroup = botGroup.length;
        int lowestHealthIndex = 0;
        int lowestHealthSoFar = botGroup[lowestHealthIndex].getHealth();
        for (int i = 0; i < lengthBotGroup; i++){
            int botHealth = botGroup[i].getHealth();
            if (botHealth < lowestHealthSoFar) {
                lowestHealthSoFar = botHealth;
                lowestHealthIndex = i;
            }
        }
        return botGroup[lowestHealthIndex];
    }




    // Scan all newly visible island squares
    // Decided to abandon this, as at a vision range of 20 it would take over 200 bytecode to senseIsland on each newly
    //  visible square.
    // Update: Maybe there is something to this, since you would know a confirmed location and avoid calling
    //  senseNearbyIslandLocations for each. (additional 100 bytecode per island sensed).

    static int makeIslandBroadcastPair(RobotController rc, MapLocation location, boolean friendly_owned,
                                         int ownerCombatStrength, int islandId, boolean anchor_present, int friendlies,
                                          int enemies) throws GameActionException{
        //{[1 isLocation][1 friendly][2 ownerCombatStrength][12 location]}
        //MapLocation me = rc.getLocation();
        int packedLocation = 0b1000000000000000;
        if (friendly_owned) {packedLocation += 0b0100000000000000;}
        int compressedStrength = Comms.compressCount(ownerCombatStrength);
        packedLocation += (compressedStrength << 12);
        packedLocation = packedLocation + (location.x << 6) + (location.y);


        //{[1 isLocation][6 id][1 Anchor_present][2 friendlies][2 enemies][4 TBD]}
        int packedDetails = islandId << 9;
        if(anchor_present) packedDetails += 0b0000000100000000;
        packedDetails += friendlies  << 6;
        packedDetails += enemies << 4;

        int fullyPackedIsland = (packedLocation << 16) + packedDetails;
        return fullyPackedIsland;
    }

    static int packageIslandDetailBroadcast(RobotController rc, int islandId, boolean anchor_present, int friendlies,
                                       int enemies) throws GameActionException {
        //{[1 isLocation][6 id][1 Anchor_present][2 friendlies][2 enemies][4 TBD]}
        int packedDetails = islandId << 9;
        if(anchor_present) packedDetails += 0b0000000100000000;
        packedDetails += friendlies  << 6;
        packedDetails += enemies << 4;
        return packedDetails;
    }

    //
    public static short senseBitpackedIslandLocation(RobotController rc, MapLocation targetLocation){
        return -1;
    }

    public static short senseBitpackedIslandDetails(RobotController rc, MapLocation targetLocation){
        return -1;
    }


    // ____________Below this are things that won't change during a game________
    // Only need to sense each of these once (per bot). Skip sensing these if you sensed last turn and didn't move.



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
        return impassableSpaces;
    }

    // Scan all nearby clouds

    // Scan all nearby currents
    //  Array of [dir 1] [location] bitpacked.
}
