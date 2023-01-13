package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotController;

import hydr8player.v1.Carrier.v2.capabilities.RunRetrieveResources;
import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;

/**
 * CarrierController runs a carrier strategy with carrier state
 * side effects: runSearchForWell, setCurrentState
 * ResourceControllers such as this should never call setters other than setCurrentState
 */

public class CarrierController {
    private RunSearchForWell runSearchForWell = null;
    private RunRetrieveResources runRetrieveResources = null;

    public CarrierController(RunSearchForWell runSearchForWell,
            RunRetrieveResources runRetrieveResources
    ){
        this.runSearchForWell = runSearchForWell;
        this.runRetrieveResources = runRetrieveResources;
    }

    public void run(RobotController rc, CarrierState state) throws GameActionException {
        switch(state.getCurrentState()) {
            case SEARCHING_FOR_WELL:
                rc.setIndicatorString("State: SEARCHING_FOR_WELL");
                this.runSearchForWell.run(rc, state);
                if(state.getFoundWell() != null) {
                    rc.setIndicatorString("State Transition: to RETRIEVING_RESOURCE");
                    state.setCurrentState(CarrierState.State.RETRIEVING_RESOURCES);
                }
                break;
            case RETRIEVING_RESOURCES:
                rc.setIndicatorString("State: RETRIEVING_RESOURCE");
                if(state.getFoundWell() == null) {
                    throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR,
                            "Error in CarrierController. " +
                                    "Carrier should not be retrieving a resource without a known well.");
                }
                else {
                    this.runRetrieveResources.run(rc, state);
                    if(state.getIsAtHoldingCapacity()) {
                        rc.setIndicatorString("State Transition: to DELIVERING_TO_HQ");
                        state.setCurrentState(CarrierState.State.DELIVERING_TO_HQ);
                    }
                }
                break;
        }
    }
}
