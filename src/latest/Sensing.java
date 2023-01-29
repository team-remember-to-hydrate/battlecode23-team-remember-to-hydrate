package latest;

import battlecode.common.*;
import latest.RobotPlayer.map_tiles;
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
    public static RobotInfo[] smartScanMembersOfTeam(RobotController rc, Team team, int range) throws GameActionException {
        if (team == rc.getTeam().opponent()) {
            if (RobotPlayer.lastRoundScannedEnemies != rc.getRoundNum()){
                // Our old scan is outdated, do a new one.
                RobotPlayer.scannedEnemies = rc.senseNearbyRobots(range, team); //102 bc
                RobotPlayer.lastRoundScannedEnemies = rc.getRoundNum();
            }
                // Just use our existing scan.
                return RobotPlayer.scannedEnemies;
        }
        else {
            if (RobotPlayer.lastRoundScannedAllies != rc.getRoundNum()){
                // Our old scan is outdated, do a new one.
                RobotPlayer.scannedAllies = rc.senseNearbyRobots(range, team); //102 bc
                RobotPlayer.lastRoundScannedAllies = rc.getRoundNum();
            }
            // Just use our existing scan.
            return RobotPlayer.scannedAllies;
        }
    }

    public static RobotInfo[] smartScanMembersOfTeam(RobotController rc, Team team) throws GameActionException {
        return smartScanMembersOfTeam(rc, team, -1);
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

    public static ArrayList<RobotInfo> scanCombatUnitsOfTeamInRange(RobotController rc, Team team, int range) throws GameActionException {
        RobotInfo[] teamRobots = smartScanMembersOfTeam(rc, team, range);
        ArrayList<RobotInfo> teamCombatRobots = new ArrayList<RobotInfo>();
        int teamRobotsLength = teamRobots.length; // saves bytecode
        for (int i = 0; i < teamRobotsLength; i++){
            if (teamRobots[i].getType() == RobotType.LAUNCHER || teamRobots[i].getType() == RobotType.DESTABILIZER){
                teamCombatRobots.add(teamRobots[i]);
            }
        }
        return teamCombatRobots;
    }

    public static ArrayList<RobotInfo> scanAnyUnitsOfTeamInRange(RobotController rc, Team team, int range) throws GameActionException {
        RobotInfo[] teamRobots = smartScanMembersOfTeam(rc, team, range);
        ArrayList<RobotInfo> teamNotHQRobots = new ArrayList<RobotInfo>();
        int teamRobotsLength = teamRobots.length; // saves bytecode
        for (int i = 0; i < teamRobotsLength; i++){
            if (teamRobots[i].getType() != RobotType.HEADQUARTERS){
                teamNotHQRobots.add(teamRobots[i]);
            }
        }
        return teamNotHQRobots;
    }

    public static int scanRelativeCombatStrength(RobotController rc) throws GameActionException {
        int friendlyStrength = scanCombatUnitsOfTeam(rc, rc.getTeam()).size();
        int enemyStrength = scanCombatUnitsOfTeam(rc, rc.getTeam().opponent()).size();
        return friendlyStrength - enemyStrength;
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
    public static RobotInfo scanWeakestBotInGroup(RobotController rc, ArrayList<RobotInfo> botGroup) {
        int lengthBotGroup = botGroup.size();
        if (lengthBotGroup == 0) {
            return null;
        }
        int lowestHealthIndex = 0;
        int lowestHealthSoFar = botGroup.get(lowestHealthIndex).getHealth();
        for (int i = 0; i < lengthBotGroup; i++){
            int botHealth = botGroup.get(i).getHealth();
            if (botHealth < lowestHealthSoFar) {
                lowestHealthSoFar = botHealth;
                lowestHealthIndex = i;
            }
        }
        return botGroup.get(lowestHealthIndex);
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


        //{[1 isLocation][6 id][1 Anchor_present][2 friendlies][2 enemies][1 friendly][3 TBD]}
        int packedDetails = islandId << 9;
        if(anchor_present) packedDetails += 0b0000000100000000;
        int compressedFriendlies = Comms.compressCount(friendlies);
        packedDetails += compressedFriendlies  << 6;
        int compressedEnemies = Comms.compressCount(enemies);
        packedDetails += compressedEnemies << 4;

        int fullyPackedIsland = (packedLocation << 16) + packedDetails;
        return fullyPackedIsland;
    }

    static int islandDetailsFromBroadcastPair(int islandBroadcastPair){
        return (islandBroadcastPair & 0x10);
    }

    static int packageIslandDetailBroadcast(RobotController rc, int islandId, boolean anchor_present, int friendlies,
                                       int enemies, boolean friendlyOwned) throws GameActionException {
        //{[1 isLocation][6 id][1 Anchor_present][2 friendlies][2 enemies][1 friendly][3 TBD]}
        int packedDetails = islandId << 9;
        if(anchor_present) packedDetails += 0b0000000100000000;
        int compressedFriendlies = Comms.compressCount(friendlies);
        packedDetails += compressedFriendlies  << 6;
        int compressedEnemies = Comms.compressCount(enemies);
        packedDetails += compressedEnemies << 4;
        if (friendlyOwned) packedDetails += 0b0000000000001000;
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

    /**
     * Scans for and returns the map_tiles enum best matching the location or UNKNOWN if unscannable.
     * @param rc
     * @param location the location to scan
     * @return The map_tile enum best matching the location, or UNKNOWN if unscannable.
     * @throws GameActionException
     */
    public static map_tiles scanMapTileType(RobotController rc, MapLocation location) throws GameActionException {
        map_tiles tile_type = map_tiles.UNKNOWN;
        if (!rc.canSenseLocation(location)) {return tile_type;}

        MapInfo tileInfo = rc.senseMapInfo(location);
        // Check for wall
        if (!tileInfo.isPassable()) {return map_tiles.WALL;}
        // Check for island
        if (rc.senseIsland(location) > 0) {return map_tiles.ISLAND_NEUTRAL;}
        // Check for well
        WellInfo wellInfo = rc.senseWell(location);
        if (!wellInfo.getResourceType().equals(ResourceType.NO_RESOURCE)) {
            ResourceType type = wellInfo.getResourceType();
            switch (type) {
                case ADAMANTIUM:
                    return map_tiles.ADAMANTIUM;
                case MANA:
                    return map_tiles.MANA;
                case ELIXIR:
                    return map_tiles.ELIXIR;
            }
        }
        // Check for HQ
        RobotInfo robotAtLocation = rc.senseRobotAtLocation(location);
        if (robotAtLocation != null){
            if (robotAtLocation.getType().equals(RobotType.HEADQUARTERS)){
                if (robotAtLocation.getType().equals(rc.getTeam())) {
                    return map_tiles.HQ_FRIENDLY;
                }
                else {
                    return map_tiles.HQ_ENEMY;
                }
            }
        }
        // Check for cloud
        if (tileInfo.hasCloud()) {return map_tiles.CLOUD;}
        // Check for current
        Direction currentDirection = tileInfo.getCurrentDirection();
        if (currentDirection != Direction.CENTER) {
            switch (currentDirection) {
                case NORTH:
                    return map_tiles.CURRENT_N;
                case NORTHEAST:
                    return map_tiles.CURRENT_NE;
                case EAST:
                    return map_tiles.CURRENT_E;
                case SOUTHEAST:
                    return map_tiles.CURRENT_SE;
                case SOUTH:
                    return map_tiles.CURRENT_S;
                case SOUTHWEST:
                    return map_tiles.CURRENT_SW;
                case WEST:
                    return map_tiles.CURRENT_W;
                case NORTHWEST:
                    return map_tiles.CURRENT_NW;

            }
        }
        return map_tiles.PLAIN;
    }


    // Scan all nearby walls

    /**
     * Scans for and returns an ArrayList of MapLocations of all walls in vision range
     * @param rc
     * @return ArrayList<MapLocation> of all walls in vision range.
     * @throws GameActionException
     */
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

    static void scanAndUpdateIslands(RobotController rc) throws GameActionException {
        RobotPlayer.myCurrentLocation = rc.getLocation();
        // Scan Island Info and Queue/Report Changes - 200 bc + potentially over 100 per island in range
        if (RobotPlayer.scannedIslandIDs == null
                || (RobotPlayer.myCurrentLocation != RobotPlayer.lastLocationScannedIslands)) {
            RobotPlayer.scannedIslandIDs = rc.senseNearbyIslands();
            RobotPlayer.lastLocationScannedIslands = rc.getLocation();
            if (Clock.getBytecodesLeft() > 1000){
                Team ourTeam = rc.getTeam();
                int friendlies = scanCombatUnitsOfTeam(rc, ourTeam).size();
                int enemies = scanCombatUnitsOfTeam(rc, ourTeam.opponent()).size();
                for (int i = 0; i <RobotPlayer.scannedIslandIDs.length; i++){
                    if (Clock.getBytecodesLeft() > 1000){
                        int id = RobotPlayer.scannedIslandIDs[i];
                        Team occupier = rc.senseTeamOccupyingIsland(id);
                        boolean anchorPresent = (occupier != Team.NEUTRAL);
                        boolean friendlyOwned = (occupier == ourTeam);

                        if (RobotPlayer.island_locations[id] == null){
                            // This is a new island, report location plus details.
                            MapLocation[] islandLocations = rc.senseNearbyIslandLocations(id);
                            int ownerCombatStrength;
                            if (friendlyOwned) {
                                ownerCombatStrength = friendlies;
                            }
                            else {
                                ownerCombatStrength = enemies;
                            }

                            int islandBroadcastPair = makeIslandBroadcastPair(rc, islandLocations[0],
                                    friendlyOwned, ownerCombatStrength, id, anchorPresent, friendlies, enemies);

                            // Broadcast it if we can
                            if (Comms.set_island_from_island_broadcast_pair(rc, islandBroadcastPair)){
                                // It has now been written to the comm array
                                RobotPlayer.island_locations[id] = islandLocations[0];
                                RobotPlayer.teamKnownIslandDetails[id] =
                                        islandDetailsFromBroadcastPair(islandBroadcastPair);
                            }
                            else{
                                // Store this until we can broadcast
                                RobotPlayer.myIslandFullInfoBroadcastQueue.add(islandBroadcastPair);
                            }
                        }
                        else {
                            // Report it if the details are new
                            int islandDetailBroadcast = packageIslandDetailBroadcast(rc, id, anchorPresent,
                                    friendlies, enemies, friendlyOwned);
                            if (RobotPlayer.teamKnownIslandDetails[id] != islandDetailBroadcast){
//                                System.out.println("Detected new details for id :" + id + " details: " + islandDetailBroadcast);
//                                System.out.println("Check Details. ID: " + id + " AnchorPresent:" + anchorPresent + " friendlies:" + friendlies + " enemies:" + enemies + " friendlyOwned:" + friendlyOwned );
//                                System.out.println("Previously Known Details for id" + RobotPlayer.teamKnownIslandDetails[id]);
                                // Just broadcast it if we can, otherwise it may be outdated if we queue it.
                                int target_index = Comms.get_available_island_index(rc);
                                if (rc.canWriteSharedArray(target_index, 0)){
                                    rc.writeSharedArray(target_index, islandDetailBroadcast);
                                }
//                                System.out.println("Was broadcasted. Comms value is: " + rc.readSharedArray(target_index));
//                                System.out.println("This translates (according to Comms.get_island_id) to an island ID of: " + Comms.get_island_id(rc.readSharedArray(target_index)));
                            }
                        }
                    }
                }
            }
        }
    }

    // Scan all nearby clouds

    // Scan all nearby currents
    //  Array of [dir 1] [location] bitpacked.
}
