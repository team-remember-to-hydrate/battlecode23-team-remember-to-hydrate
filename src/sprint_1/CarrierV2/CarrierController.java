package sprint_1.CarrierV2;

import battlecode.common.*;

public class CarrierController {
    public void run(RobotController rc, Carrier c) throws GameActionException {
        if(c.hqLoc == null) {
            rc.setIndicatorString("searching for hq");
            c.searchForHq(rc);
        }
        else if(c.wellLoc == null){ // game mechanic: robot also spawns within sight of a well
            rc.setIndicatorString("searching for a well");
            c.searchForWell(rc);
            c.moveRandom(rc);
        }
        else if(c.amountResourcesHeld < Carrier.MAX_RESOURCE_CAPACITY){
            if(rc.canTakeAnchor(c.hqLoc, Anchor.ACCELERATING) || rc.canTakeAnchor(c.hqLoc, Anchor.STANDARD)) {
                rc.setIndicatorString("picking up anchor from hq");
                c.tryPickUpAnchor(rc, c.hqLoc);
            }
            else if(rc.getLocation().distanceSquaredTo(c.wellLoc) <= 2){
                rc.setIndicatorString("collecting resources");
                c.tryCollectResources(rc, c.wellLoc);
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
