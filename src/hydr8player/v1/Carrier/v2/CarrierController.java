package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import hydr8player.v1.Carrier.v2.capabilities.RunRetrieveResources;
import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;

/**
 * CarrierController runs a carrier strategy with carrier state
 * side effects: runSearchForWell, setCurrentState
 */

public class CarrierController {
    private RunSearchForWell runSearchForWell = null;
    private RunRetrieveResources runRetrieveResources = null;

    public CarrierController(
            RunSearchForWell runSearchForWell,
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
                    state.setCurrentState(CarrierState.State.RETRIEVING_RESOURCE);
                }
                break;
            case RETRIEVING_RESOURCE:
                rc.setIndicatorString("State: RETRIEVING_RESOURCE");
                if(state.getFoundWell() == null) {
                    // throw Error, CarrierController made a mistake and entered this state without a found well
                    // TODO: 1/12/2023 how to handle ResourceStateController errors like this?
                }
                else {
                    this.runRetrieveResources.run(rc, state);
                    state.setCurrentState(CarrierState.State.RETURNING_TO_HQ);
                }
                break;
        }
    }
}
