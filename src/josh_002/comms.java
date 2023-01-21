package josh_002;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

public class comms {
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



    ///   ***   ISLANDS  ***
    /*
     2 words
     {[1 isLocation][1 friendly][2 combatStrength][12 location]}
     {[1 isLocation][6 Island_id][1 Anchor_present][2 friendlies][2 enemies][4 TBD]}
     */

    static boolean island_is_new(int id) throws GameActionException{
        return !known_islands.contains(id);
    }

    static boolean set_island(RobotController rc, MapLocation location, int id, int friendlies, int enemies,boolean friendly_owned,int combatStrength ) throws GameActionException {
        return set_island_location_word(rc,location,friendly_owned,combatStrength) & set_island_detail_word(rc,id,friendlies,enemies,friendly_owned);
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

    static boolean set_island_location_word(RobotController rc, MapLocation location, boolean friendly_owned, int combatStrength) throws GameActionException{
        int island_index = get_available_island_index(rc);
        MapLocation me = rc.getLocation();
        //{[1 isLocation][1 friendly][2 combatStrength][12 location]}
        if(island_index < index_last_island) {
            int packed = 0b1000000000000000;
            if (friendly_owned) {
                packed += 0b0100000000000000;
            }
            packed = packed + (me.x << 6) + (me.y);
            rc.writeSharedArray(island_index,packed);
            return true;
        }
        else{
            // no room in the array.
            return false;
        }

    }

    static boolean set_island_detail_word(RobotController rc, int id, int friendlies, int enemies, boolean friendly_owned){

        return true;
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

    ///   ***   ORDERS   ***
    /*
     2 words
     {[4 radius][12 location]}
     {[1 isTask][4 group][3 botType][1 shouldOverride][1 outsideRadiusUnassign][4 taskType][2 TBD]}
     */
    static int get_task_radius(int array_data) {
        return (array_data & 0b1111000000000000) >>> 12;
    }

    static boolean is_task(int array_data){
        return (array_data & 0b1000000000000000) == 0;
    }
    static int get_task_group(int array_data) { return (array_data & 0b0111100000000000) >>> 12; }

    /*
        This command returns zero if there are no commands for the bot
     */
    static int get_command_for_me(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        for(int i = index_orders ; i <= index_last_orders; i = i+2){  // 2 words per command
            int this_cmd = rc.readSharedArray(i);
            int radius = get_task_radius(this_cmd);
            MapLocation cmd_location = get_MapLocation(i);
            if((radius * radius)  < me.distanceSquaredTo(cmd_location)){
                return i;
            }
        }
        return 0;
    }

    static void create_command(RobotController rc, MapLocation location, int radius){

    }

}
