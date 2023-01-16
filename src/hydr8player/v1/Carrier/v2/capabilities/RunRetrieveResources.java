package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;

/**
 * RunRetrieveResources tries to retrieve resources from a well
 * side effects: maybe collectResource

public class RunRetrieveResources {
    // PRECONDITION: Found well. if well is not found, carrier should never be in this state
    // PRECONDITION: Transitioned from SEARCHING_FOR_WELL state
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("Do I have capacity to retrieve resources from a found well?");

    }
    // POSTCONDITION: carrier cannot hold more resources
    // POSTCONDITION: carrierGatherResourcesState is RETURNING_TO_HQ
}
*/