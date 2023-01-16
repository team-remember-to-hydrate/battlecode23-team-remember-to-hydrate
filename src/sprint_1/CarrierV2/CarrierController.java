package sprint_1.CarrierV2;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CarrierController {
    public void run(RobotController rc, Carrier c) throws GameActionException {
        if(c.hqLoc == null) {
            RobotInfo[] bots = rc.senseNearbyRobots(2);
            for (RobotInfo bot : bots) {
                if (bot.getType() == RobotType.HEADQUARTERS) {
                    c.hqLoc = bot.getLocation();
                }
            }
        }
        // If I am close to a HQ, I should try to grab an anchor.
        else if (rc.canTakeAnchor(c.hqLoc, Anchor.ACCELERATING)) {
            rc.takeAnchor(c.hqLoc, Anchor.ACCELERATING);
        }
        else if (rc.canTakeAnchor(c.hqLoc, Anchor.STANDARD)) {
            rc.takeAnchor(c.hqLoc, Anchor.STANDARD);
        }
        else if (rc.getAnchor() != null) {
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
        else if(c.getWellLoc() == null){
            rc.setIndicatorString("searching for a well");
            c.searchForWell(rc);
            c.moveRandom(rc);
        }
        else if(c.amountResourcesHeld < Carrier.MAX_RESOURCE_CAPACITY){
            if(rc.getLocation().distanceSquaredTo(c.getWellLoc()) <= 2){
                rc.setIndicatorString("collecting resources");
                c.collectResources(rc, c.getWellLoc());
            }
            else {
                rc.setIndicatorString("moving to well");
                c.moveWithBugNav(rc, c.getWellLoc());
            }
        }
        else if(c.amountResourcesHeld == Carrier.MAX_RESOURCE_CAPACITY && c.hqLoc != null){
            rc.setIndicatorString("carrying resources back to hq");
            c.moveWithBugNav(rc, c.hqLoc);
            c.tryTransferAllResources(rc, c.hqLoc);
        }
        else {
            rc.setIndicatorString("INTERNAL ERROR");
            throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Carrier internal error");
        }
    }
}
