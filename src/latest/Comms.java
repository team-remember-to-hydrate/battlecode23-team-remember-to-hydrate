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

    static HashSet<Integer> known_islands;

    static boolean island_is_new(int id) throws GameActionException{
        return !known_islands.contains(id);
    }

    static boolean set_island(RobotController rc, MapLocation location, int id, int friendlies, int enemies,int friendly_owned ) throws GameActionException {
        return set_island_location_word(rc,location) & set_island_detail_word(rc,id,friendlies,enemies,friendly_owned);
    }

    static boolean set_island_location_word(RobotController rc, MapLocation location) throws GameActionException{

        return true;
    }

    static boolean set_island_detail_word(RobotController rc, int id, int friendlies, int enemies, int friendly_owned){

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

    /**
     * This method assume sthe two bytes for a map location are contiguous.
     */
    static int get_island_id(RobotController rc, int array_data) throws GameActionException{
        return (array_data & 0b0111111000000000) >>> 9;
    }

    // methods to retrieve MapLocation from LSB
    static MapLocation unpackMapLocation(int target) {
        int target_x = (target & 0b0000111111000000) >>> 6;
        int target_y = (target & 0b0000000000111111);
        return new MapLocation(target_x,target_y);
    }
}
