package omega_scrimbot_1;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Carrier {

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        final Direction[] directions = RobotPlayer.directions;
        final Random rng = RobotPlayer.rng;
        int turnCount = RobotPlayer.turnCount;

        /**
         * First turn initialization
         */
        MapLocation mySpawnHQ = null;
        if (turnCount == 1){
            mySpawnHQ = Sensing.scanHQ(rc);
        }


        // Start turn by updating my inventory status
        int myAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int myMana = rc.getResourceAmount(ResourceType.MANA);
        int myElixer = rc.getResourceAmount(ResourceType.ELIXIR);
        int total_resources = myElixer + myAdamantium + myMana;
        boolean carryingMaxAmt = total_resources == 40;

        // Update my location
        MapLocation myLocation = rc.getLocation();

        // If I am close to a HQ, I should try to deliver resources or grab an anchor.
        // TODO Implement more than just anchor (out of time now)
        if (rc.canTakeAnchor(mySpawnHQ, Anchor.STANDARD)) {
            rc.takeAnchor(mySpawnHQ, Anchor.STANDARD);
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
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    rc.setIndicatorString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }
        // Try to gather from squares around us. Only if we can carry more.
        if (!carryingMaxAmt){
            MapLocation me = rc.getLocation();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation wellLocation = new MapLocation(me.x + dx, me.y + dy);
                    if (rc.canCollectResource(wellLocation, -1)) {
                        if (rng.nextBoolean()) {
                            rc.collectResource(wellLocation, -1);
                            rc.setIndicatorString("Collecting, now have, AD:" +
                                    rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                                    " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                                    " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
                        }
                    }
                }
            }
        }

        // Occasionally try out the carriers attack
        if (rng.nextInt(20) == 1) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemyRobots.length > 0) {
                if (rc.canAttack(enemyRobots[0].location)) {
                    rc.attack(enemyRobots[0].location);
                }
            }
        }

        // If we can see a well, and can carry more resources, move towards it
        if (!carryingMaxAmt && (rc.getAnchor() == null)){
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 1 && rng.nextInt(3) == 1) {
                WellInfo well_one = wells[1];
                MapLocation me = rc.getLocation();
                Direction dir = me.directionTo(well_one.getMapLocation());
                if (rc.canMove(dir))
                    rc.move(dir);
            }
            // Also try to move randomly.
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }
}
