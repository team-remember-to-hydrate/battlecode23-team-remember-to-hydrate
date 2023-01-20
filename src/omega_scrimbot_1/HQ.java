package omega_scrimbot_1;

import battlecode.common.*;

import java.util.Random;

public class HQ {
    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        /**
         * Initial turn tasks:
         * -Sense nearby wells
         * -Sense nearby impassable spaces
         * -Sense nearby islands
         * -Sense nearby bases
         * -Sense nearby map info
         */
        final Direction[] directions = RobotPlayer.directions;
        final Random rng = RobotPlayer.rng;
        int turnCount = RobotPlayer.turnCount;

        if (turnCount == 1) {
            WellInfo[] nearbyWells = rc.senseNearbyWells();
            MapLocation[] myVisibleSpaces = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                    rc.getType().visionRadiusSquared);
        }

        /**
         * Update surrounding info
         * - Sense only known well locations (they are constant from turn 1, but type can change)
         * - Sense known islands and get status
         * -- who controls them
         * -- type of anchor
         * - Sense enemy bots, get count
         * - Sense available spawn spaces
         */

        // Note current resource levels
        int myAdamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int myMana = rc.getResourceAmount(ResourceType.MANA);
        int myElixer = rc.getResourceAmount(ResourceType.ELIXIR);

        // Note spawn locations in range
        MapLocation[] spawnRangeSpaces = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                rc.getType().actionRadiusSquared);

        // Note viable spawn locations
        MapLocation[] validSpawns = new MapLocation[spawnRangeSpaces.length];
        for (int i = 0; i < validSpawns.length; i++){
            // TODO: Determine best way to check this.
        }




        // Pick a direction to build in. This should depend on current goal.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (rc.getNumAnchors(Anchor.STANDARD) < 3 && rc.canBuildAnchor(Anchor.STANDARD)) {
            // If we can build an anchor and don't have many, do it!
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getAnchor());
            myAdamantium -= 100;
            myMana -= 100;
        }
        if (myAdamantium >= 50) {
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            for (int i = 0; i < validSpawns.length; i++){
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                    break;
                }
            }
        } else {
            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            for (int i = 0; i < validSpawns.length; i++){
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                    break;
                }
            }
        }
    }

    /**
     * Returns the location closest to the target that is available for robot placement.
     */
    static MapLocation bestBuildLocation(MapLocation[] visibleSpaces, MapLocation targetLocation, MapLocation[] occupiedSpaces){
        return null; // TODO
    }
}
