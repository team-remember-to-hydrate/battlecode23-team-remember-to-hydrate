package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import hydr8player.v1.Carrier.v2.CarrierState;

/**
 * RunRetrieveResources tries to retrieve resources from a well
 * side effects: maybe collectResource
 */
public class RunRetrieveResources {
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("RunRetrieveResources > Trying to collect from well");
        int totalCarrying = rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                rc.getResourceAmount(ResourceType.MANA) +
                rc.getResourceAmount(ResourceType.ELIXIR);
        if (totalCarrying <= 40 && rc.getAnchor() == null) {
            rc.setIndicatorString("Retrieving resources");
            MapLocation foundWellLocation = state.getWell().getMapLocation();
            if (rc.canCollectResource(foundWellLocation, -1)) {
                rc.collectResource(foundWellLocation, -1);
            }
        }
    }
}
