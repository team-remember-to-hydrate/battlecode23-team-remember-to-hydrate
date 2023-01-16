package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotController;

public class CarrierController {
    public void run(RobotController rc, Carrier c) throws GameActionException {
        if(c.wellLoc == null){
            rc.setIndicatorString("searching for a well");
            c.searchForWell(rc);
            c.moveRandom(rc);
        }
        else if(c.amountResourcesHeld < Carrier.MAX_RESOURCE_CAPACITY){
            if(rc.getLocation().distanceSquaredTo(c.wellLoc) <= 2){
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
        }
        else {
            rc.setIndicatorString("INTERNAL ERROR");
            throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Carrier internal error");
        }
    }
}
