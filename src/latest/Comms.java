package latest;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

public class Comms {
    /**
     * Comms Array layout:
     * We are given an array of size 64. Each index contains a Word (2-bytes, 16 bits).
     *
     * [Index] - Data Contained (Details in format Field 1: details; Field 2: details)
     *
     * [0-3] - HQ Updates (Info; Location, *possibly round-dependent, see below)
     * [4-10] - Well Updates (Type; Upgraded bit; Location)
     * [11-30] - Island Updates (Location; or Details *see below)
     * [31-36] - Obstacle Updates (Type:currents, clouds, storms/walls; Location)
     * [37] - Enemy HQ Updates (Econ Unit Count; Combat Unit Count; Location)
     * [38] - Carrier Announcing Newly Acquired Anchor (Anchor Type; Location)
     * [39] - Unused (yet)
     * [40-59] - Orders from HQ or Amplifier, order is Group Membership Assignment or Group Task Assignment
     *      Membership Assignment:
     *      Task Assignment:
     * [60-61] - Amplifier or HQ reporting enemy strength at location (technically usable by any unit, not just Amps/HQs).
     * [62-64] - Unused (yet)
     */
    static final int index_hq           =  0;
    static final int index_last_hq      =  3;
    static final int index_well         =  4;
    static final int index_last_well    = 10;
    static final int index_island       = 11;
    static final int index_last_island  = 30;
    static final int index_obstacle     = 31;
    static final int index_last_obstacle = 36;
    static final int index_orders       = 40;
    static final int index_last_orders  = 59;

    static HashSet<Integer> known_islands;

    // Compressed Count
    /**
     * Compressed Count:
     * These are used so we can communicate a general idea of a number, using less bits.
     * Useful to send a count of enemy combatants visible at a location.
     * Each index corresponds to its 2-bit binary representation (00 = index 0, 01 = index 1, ..., 11 = index 3).
     * The value at that index is the minimum 'count' represented by these 2 bits.
     * So, for example, if a location is provided with a count bits value of 10, we know that the true value of
     * whatever was counted at that location is at minimum 3 (index b10 below) and must be less than 6 (value at index
     * b11).
     * We use this because often we don't care about the exact amount of something, but a general understanding of the
     * situation. This breaks the enemy count down to categories of 0 enemies, 1-2 enemies, 3-5 enemies, or 6+.
     */
    public final static int[] countPartitions = {0,1,3,6};

    // Returns compressed count. Saved on bytecode by not checking for negatives (bad practice normally).
    static int compressCount(int trueCount){
        for (int i = 0; i < countPartitions.length; i++){
            if (trueCount < countPartitions[i]){
                return i - 1;
            }
        }
        return countPartitions.length - 1;
    }

    static int decompressCountToMinPossible(int compressedCount){
        return countPartitions[compressedCount];
    }


    ///   ***   ISLANDS  ***
    /*
     2 words
     {[1 isLocation][1 friendly][2 ownerCombatStrength][12 location]}
     {[1 isLocation][6 Island_id][1 Anchor_present][2 friendlies][2 enemies][1 friendly_owned][3 TBD]}
     */

    static boolean island_is_new(int id) throws GameActionException{
        return !known_islands.contains(id);
    }

    static boolean set_island(RobotController rc, MapLocation location, int id, int friendlies, int enemies,
                              boolean friendly_owned,int combatStrength, boolean anchor_present )
            throws GameActionException {
        int island_index = get_available_island_index(rc);
        if (island_index < index_last_island) {
            set_island_location_word(rc, location, friendly_owned, combatStrength, island_index);
            set_island_detail_word(rc, id, anchor_present, friendlies, enemies, friendly_owned, island_index + 1);
            return true;
        }else{
            // no room in the array.
            return false;
        }
    }

    static boolean set_island_from_island_broadcast_pair(RobotController rc, int fullyPackedIsland)
            throws GameActionException {
        int island_index = get_available_island_index(rc);
        if (!rc.canWriteSharedArray(island_index, 0)) {
            return false;
        }

        if (island_index < index_last_island) {
            // Set island location word
            int island_location_packed = fullyPackedIsland >> 16;
            rc.writeSharedArray(island_index, island_location_packed);

            // Set island detail word
            int island_detail_packed = ((fullyPackedIsland << 16) >> 16);
            rc.writeSharedArray(island_index, island_detail_packed);
            return true;
        }else{
            // no room in the array.
            return false;
        }
    }

    /*
        returns 99 if the array island reporting is full
     */
    static int get_available_island_index(RobotController rc) throws GameActionException{
        for(int i = index_island; i <= index_last_island;i = i + 2){
            int this_value = rc.readSharedArray(i);
            if(this_value == 0){
                return i;
            }
        }
        return 99;
    }

    static void set_island_location_word(RobotController rc, MapLocation location, boolean friendly_owned,
                                         int combatStrength, int island_index) throws GameActionException{
        //{[1 isLocation][1 friendly][2 combatStrength][12 location]}
        MapLocation me = rc.getLocation();
        int packed = 0b1000000000000000;
        if (friendly_owned) {packed += 0b0100000000000000;}
        packed = packed + (me.x << 6) + (me.y);
        if (rc.canWriteSharedArray(island_index, packed)){
            rc.writeSharedArray(island_index, packed);
        }
    }

    static void set_island_detail_word(RobotController rc, int id, boolean anchor_present, int friendlies,
                                       int enemies, boolean friendly_owned, int island_index) throws GameActionException {
        //{[1 isLocation][6 id][1 Anchor_present][2 friendlies][2 enemies][1 friendly_owned][3 TBD]}
        int packed = id << 9;
        if(anchor_present) packed += 0b0000000100000000;
        packed += friendlies  << 6;
        packed += enemies << 4;
        if (friendly_owned) packed += 0b0000000000001000;
        if (rc.canWriteSharedArray(island_index, packed)){
            rc.writeSharedArray(island_index, packed);
        }
    }

    static void track_island_ids(RobotController rc) throws GameActionException{
        for(int i = index_island; i <= index_last_island;i = i + 2){
            int this_value = rc.readSharedArray(i);
            if(is_location(this_value)){
                int island_id = get_island_id(rc, i + 1);
                known_islands.add(island_id);  // the next array index contains the id
            }
        }
    }

    static boolean is_location(int array_data){
        return (array_data & 0b1000000000000000) == 0;
    }

    static int get_island_id(RobotController rc, int array_data) throws GameActionException{
        return (array_data & 0b0111111000000000) >>> 9;
    }

    // methods to retrieve MapLocation from LSB
    static MapLocation get_MapLocation(int target) {
        int target_x = (target & 0b0000111111000000) >>> 6;
        int target_y = (target & 0b0000000000111111);
        return new MapLocation(target_x,target_y);
    }


    ///   ***   TASKS   ***
    /*
     2 words
     {[4 radius][12 location]}
     {[1 isTask][4 group][3 botType][1 shouldOverride][1 outsideRadiusUnassign][4 taskType][2 TBD]}
     */

    static int get_task_radius( int array_data) {return (array_data & 0b1111000000000000) >>> 12; }
    static boolean is_task    ( int array_data) {return (array_data & 0b1000000000000000) ==   0; }
    static int get_task_group ( int array_data) {return (array_data & 0b0111100000000000) >>> 12; }
    static int get_bot_type   ( int array_data) {return (array_data & 0b0000011100000000) >>>  8;}
    static int get_task_type  ( int array_data) {return (array_data & 0b0000000000111100) >>>  2; }
    /*
        This command returns zero if there are no commands for the bot
        // TODO: This needs to handle edge cases:
        // Edge cases: -bot receives multiple orders in one round (one to assign membership, one to assign task)
        // - bot is implied in order due to group membership, not due to location (we only check for location here)
     */
    static int get_command_for_me(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        for(int i = index_orders ; i <= index_last_orders; i = i+2){  // 2 words per command
            int this_cmd = rc.readSharedArray(i);
            int radius = get_task_radius(this_cmd);
            MapLocation cmd_location = get_MapLocation(i);
            if((radius * radius)  < me.distanceSquaredTo(cmd_location)){
                return i;
            }else if(RobotPlayer.my_group == get_task_group(this_cmd)){
                return i;
            }
        }
        return 0;
    }

    static void set_group(RobotController rc, MapLocation location, int radius, int group, int bot_type,
                          boolean override, boolean unassign, int type){

    }


    static void set_task(RobotController rc, MapLocation location, int radius,int group, int bot_type,
                         boolean override, boolean unassign, int type){

    }

    /*
    returns 99 if the array island reporting is full
    */
    static int get_available_command_index(RobotController rc) throws GameActionException{
        for(int i = index_orders; i <= index_last_orders;i = i + 2){
            int this_value = rc.readSharedArray(i);
            if(this_value == 0){
                return i;
            }
        }
        return 99;
    }

    static void clear_command(RobotController rc, int array_index) throws GameActionException{
        rc.writeSharedArray(array_index, 0);
        rc.writeSharedArray(array_index + 1, 0);
    }
}
