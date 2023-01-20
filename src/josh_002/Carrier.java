package josh_002;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Carrier {

    static byte[][] map;
    static int my_HQ = 99;  // 0-3 are valid starts in not valid state
    static MapLocation my_HQ_location;
    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {

        if (RobotPlayer.birth_location == null){
            RobotPlayer.birth_location = rc.getLocation();
        }
        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
        }
        if (rc.canTakeAnchor(my_HQ_location, Anchor.STANDARD)) {
            rc.takeAnchor(my_HQ_location, Anchor.STANDARD);
        }

        // Try to gather from squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                if (rc.canCollectResource(wellLocation, -1)) {
                    if (RobotPlayer.rng.nextBoolean()) {
                        rc.collectResource(wellLocation, -1);
                        rc.setIndicatorString("Collecting, now have, AD:" +
                                rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                    }
                }
            }
        }

        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(RobotPlayer.movable_direction(rc, dir))) {
                        rc.move(RobotPlayer.movable_direction(rc, dir));
                        rc.setIndicatorString("have anchor going " + dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }

        // try to deliver resource to headquarters
        RobotInfo[] nearby = rc.senseNearbyRobots(1, rc.getTeam());
        for (RobotInfo bot : nearby){
            if (bot.getType() == RobotType.HEADQUARTERS){
                rc.setIndicatorString("Near Headquarters ");
                if (rc.canTransferResource(bot.getLocation(), ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))){
                    rc.transferResource(bot.getLocation(),ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
                    rc.setIndicatorString("Transferred Adamantium to Headquarters");
                }
                if (rc.canTransferResource(bot.getLocation(), ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))){
                    rc.transferResource(bot.getLocation(),ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
                    rc.setIndicatorString("Transferred MANA to Headquarters");
                }
            }
        }


        // If we are full move towards base
        int ad_weight = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int mana_weight = rc.getResourceAmount(ResourceType.MANA);
        int total_weight = ad_weight + mana_weight;
        if (total_weight >= 39) {
            Direction dir = me.directionTo(RobotPlayer.birth_location);
            rc.setIndicatorString("returning to " + RobotPlayer.birth_location );
            if (rc.canMove(dir))

                rc.move(dir);
        }

        // If we can see a well, move towards it
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 1 && RobotPlayer.rng.nextInt(3) == 1) {
            WellInfo well_one = wells[0];
            Direction dir = RobotPlayer.movable_direction(rc, me.directionTo(well_one.getMapLocation()));
            rc.setIndicatorString("Can see a well move cooldown is " + rc.getMovementCooldownTurns());
            if (rc.canMove(dir))
                rc.move(dir);
        }
    }
}
