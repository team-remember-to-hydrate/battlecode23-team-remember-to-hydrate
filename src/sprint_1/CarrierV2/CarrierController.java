package sprint_1.CarrierV2;

import battlecode.common.*;

public class CarrierController {
    public void run(RobotController rc, Carrier c) throws GameActionException {
        if(c.hqLoc == null) {
            c.searchForHq(rc);
        }
        else if(rc.canTakeAnchor(c.hqLoc, Anchor.ACCELERATING) || rc.canTakeAnchor(c.hqLoc, Anchor.STANDARD)) {
            c.tryPickUpAnchor(rc, c.hqLoc);
        }
        else if(c.hasAnchor){
            c.deliverAnchor(rc);
        }
        else if(c.wellLoc == null){ // game mechanic: robot also spawns within sight of a well
            c.searchForWell(rc);
            c.moveRandom(rc);
        }
        else if(c.amountResourcesHeld < Carrier.MAX_RESOURCE_CAPACITY){
            if(rc.getLocation().distanceSquaredTo(c.wellLoc) <= 2){
                c.tryCollectResources(rc, c.wellLoc);
            }
            else {
                c.moveWithBugNav(rc, c.wellLoc);
            }
        }
        else if(c.amountResourcesHeld == Carrier.MAX_RESOURCE_CAPACITY && c.hqLoc != null){
                c.moveWithBugNav(rc, c.hqLoc);
                c.tryTransferAllResources(rc, c.hqLoc);
        }
        else {
            throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Carrier internal error");
        }
    }
}
