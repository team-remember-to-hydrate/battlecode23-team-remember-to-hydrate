package sprint_1.CarrierV2;

import battlecode.common.*;

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
        else if(c.wellLoc == null){ // game mechanic: robot also spawns within sight of a well
            rc.setIndicatorString("searching for a well");
            c.searchForWell(rc);
            c.moveRandom(rc);
        }
        else if(c.amountResourcesHeld < Carrier.MAX_RESOURCE_CAPACITY){
            if(c.sensedAnchorAtHq) {
                rc.setIndicatorString("picking up anchor from hq");
                c.tryPickUpAnchorFromHq(rc, c.hqLoc);
            }
            else if(rc.getLocation().distanceSquaredTo(c.wellLoc) <= 2){
                rc.setIndicatorString("collecting resources");
                c.collectResources(rc, c.wellLoc);
            }
            else {
                rc.setIndicatorString("moving to well");
                c.moveWithBugNav(rc, c.wellLoc);
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
