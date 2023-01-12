package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import hydr8player.v1.Carrier.v2.CarrierState;

/**
 * RunRetrieveResources tries to retrieve resources from a well
 */
public class RunRetrieveResources {
    // PRECONDITION: Found well. if well is not found, carrier should never be in this state
    // PRECONDITION: Transitioned from SEARCHING_FOR_WELL state
    public static void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("Do I have capacity to retrieve resources from a found well?");
        int totalCarrying = rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                rc.getResourceAmount(ResourceType.MANA) +
                rc.getResourceAmount(ResourceType.ELIXIR);
        if(totalCarrying <= 40 && rc.getAnchor() == null) {
            rc.setIndicatorString("Retrieving resources");
            MapLocation foundWellLocation = state.getFoundWell().getMapLocation();
            if (rc.canCollectResource(foundWellLocation, -1)) {
                rc.collectResource(foundWellLocation, -1);
            }
        }
    }
    // POSTCONDITION: carrier cannot hold more resources
    // POSTCONDITION: carrierGatherResourcesState is RETURNING_TO_HQ
}
