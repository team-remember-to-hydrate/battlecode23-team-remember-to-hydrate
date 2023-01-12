package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;

/**
 * CarrierController runs a carrier strategy with carrier state
 * side effects: capabilities
 */

public class CarrierController {
    private RunSearchForWell runSearchForWell = null;
    public CarrierController(RunSearchForWell runSearchForWell){
        this.runSearchForWell = runSearchForWell;
    }

    public void run(RobotController rc, CarrierState state) throws GameActionException {
        switch(state.getCurrentState()) {
            case SEARCHING_FOR_WELL:
                this.runSearchForWell.run(rc, state);
                if(state.getFoundWell() != null) {
                    state.setCurrentState(CarrierState.State.RETRIEVING_RESOURCE);
                }
                break;
            /*
            case RETRIEVING_RESOURCE:
                rc.setIndicatorString("Should I retrieve resources from a found well?");
                if(foundWell != null) {
                    runCarrierRetrieveResource(rc);
                    carrierGatherResourcesState = CarrierGatherResourcesState.RETURNING_TO_HQ;
                }
                break;
            */
        }
    }
}
