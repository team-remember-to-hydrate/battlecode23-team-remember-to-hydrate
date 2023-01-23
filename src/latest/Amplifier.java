package latest;

import battlecode.common.*;

import java.util.Random;

public class Amplifier {

    final Direction[] directions = RobotPlayer.directions;
    final Random rng = RobotPlayer.rng;
    static RobotPlayer.states my_state;
    static int my_HQ = 99;
    static MapLocation my_HQ_location;

    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation myLocation;
    static MapLocation map_center;

    static void runAmplifier(RobotController rc) throws GameActionException {


        // This runs on my first turn only
        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = RobotPlayer.states.INITIAL;
            map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        }

        // Each turn starts with these tasks before dealing with primary task:

        // Read Comms and update worldview


        // Update High Value Map Info
        myLocation = rc.getLocation();
        // Scan Island Info and Queue/Report Changes - 200 bc + potentially over 100 per island in range
        if (RobotPlayer.scannedIslandIDs == null || (myLocation != RobotPlayer.lastLocationScannedIslands)) {
            RobotPlayer.scannedIslandIDs = rc.senseNearbyIslands();

            if (Clock.getBytecodesLeft() > 1000){
                Team ourTeam = rc.getTeam();
                int friendlies = Sensing.scanCombatUnitsOfTeam(rc, ourTeam).size();
                int enemies = Sensing.scanCombatUnitsOfTeam(rc, ourTeam.opponent()).size();
                for (int i = 0; i <RobotPlayer.scannedIslandIDs.length; i++){
                    if (Clock.getBytecodesLeft() > 1000){
                        int id = RobotPlayer.scannedIslandIDs[i];
                        Team occupier = rc.senseTeamOccupyingIsland(id);
                        boolean anchorPresent = (occupier != Team.NEUTRAL);
                        boolean friendlyOwned = (occupier == ourTeam);

                        if (RobotPlayer.teamKnownIslandLocations.get(id).size() != 0){
                            // This is a new island, report location plus details.
                            MapLocation[] islandLocations = rc.senseNearbyIslandLocations(id);
                            int ownerCombatStrength;
                            if (friendlyOwned) {
                                ownerCombatStrength = friendlies;
                            }
                            else {
                                ownerCombatStrength = enemies;
                            }

                            int islandBroadcastPair = Sensing.makeIslandBroadcastPair(rc, islandLocations[0],
                                    friendlyOwned, ownerCombatStrength, id, anchorPresent, friendlies, enemies);

                            // Broadcast it if we can
                            if (Comms.set_island_from_island_broadcast_pair(rc, islandBroadcastPair)){
                                // It has now been written to the comm array
                            }
                            else{
                                // Store this until we can broadcast
                                RobotPlayer.myIslandFullInfoBroadcastQueue.add(islandBroadcastPair);
                            }
                        }
                        else {
                            // Report it if the details are new
                            int islandDetailBroadcast = Sensing.packageIslandDetailBroadcast(rc, id, anchorPresent,
                                    friendlies, enemies, friendlyOwned);
                            if (RobotPlayer.teamKnownIslandDetails[id] != islandDetailBroadcast){
                                // Just broadcast it if we can, otherwise it may be outdated if we queue it.
                                int target_index = Comms.get_available_island_index(rc);
                                if (rc.canWriteSharedArray(target_index, 0)){
                                    rc.writeSharedArray(target_index, islandDetailBroadcast);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scan wells

        // Read Orders and change state and goals accordingly

        // Sense nearby enemies, possibly respond accordingly




        switch(my_state){
            case INITIAL:
                my_state = RobotPlayer.states.SCOUT;
            case SCOUT:
                // Refresh MapInfo
                // Try to move with clockwise momentum
                Direction dir = Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, true);
                Pathing.trackedMove(rc, dir);
//                if (rc.canMove(dir)) {
//                    rc.move(dir);
//                    RobotPlayer.lastMoved = dir;
//                }
            case DEFAULT:
                my_state = RobotPlayer.states.SCOUT;
        }

        // Communicate High Value Info

        // Update Low Value Map Info
        myLocation = rc.getLocation();
        if (RobotPlayer.scannedMapInfos == null || (myLocation != RobotPlayer.lastLocationScannedMapInfos)) {
            RobotPlayer.scannedMapInfos = rc.senseNearbyMapInfos();
            RobotPlayer.lastLocationScannedMapInfos = myLocation;
        }

        // Communicate Low Value Map Info

    }
}
