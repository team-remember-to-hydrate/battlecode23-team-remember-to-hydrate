package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotController;

import hydr8player.v1.Carrier.v2.capabilities.RunDeliverToHq;
import hydr8player.v1.Carrier.v2.capabilities.RunPathingToHq;
import hydr8player.v1.Carrier.v2.capabilities.RunPathingToWell;
import hydr8player.v1.Carrier.v2.capabilities.RunRetrieveResources;
import hydr8player.v1.Carrier.v2.capabilities.RunFindWell;

/**
 * CarrierController runs a carrier strategy with carrier state
 * side effects: runSearchForWell, setCurrentState
 * ResourceControllers such as this should never call setters other than setCurrentState
 */

public class CarrierController {
    private RunFindWell runFindWell = null;
    private RunRetrieveResources runRetrieveResources = null;
    private RunDeliverToHq runDeliverToHq = null;
    private RunPathingToWell runPathingToWell = null;
    private RunPathingToHq runPathingToHq = null;

    public CarrierController(
            RunFindWell runFindWell,
            RunRetrieveResources runRetrieveResources,
            RunDeliverToHq runDeliverToHq,
            RunPathingToWell runPathingToWell,
            RunPathingToHq runPathingToHq
    ){
        this.runFindWell = runFindWell;
        this.runRetrieveResources = runRetrieveResources;
        this.runDeliverToHq = runDeliverToHq;
        this.runPathingToWell = runPathingToWell;
        this.runPathingToHq = runPathingToHq;
    }

    public void run(RobotController rc, CarrierState state) throws GameActionException {
        switch(state.getCurrentState()) {
            case SEARCHING_FOR_WELL:
                rc.setIndicatorString("State: SEARCHING_FOR_WELL");
                this.runPathingToWell.run(rc, state);
                this.runFindWell.run(rc, state);
                if (state.getWell() != null) {
                    rc.setIndicatorString("State Transition: to RETRIEVING_RESOURCE");
                    state.setCurrentState(CarrierState.State.RETRIEVING_RESOURCES);
                }
                break;
            case RETRIEVING_RESOURCES:
                rc.setIndicatorString("State: RETRIEVING_RESOURCE");
                if (state.getWell() == null) {
                    throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR,
                            "Error in CarrierController. " +
                                    "Carrier should not be retrieving a resource without a known well.");
                }
                else {
                    this.runPathingToWell.run(rc, state);
                    this.runRetrieveResources.run(rc, state);
                    if (state.isAtHoldingCapacity()) {
                        rc.setIndicatorString("State Transition: to DELIVERING_TO_HQ");
                        state.setCurrentState(CarrierState.State.DELIVERING_TO_HQ);
                    }
                }
                break;
            case DELIVERING_TO_HQ:
                rc.setIndicatorString("State: DELIVERING_TO_HQ");
                if(state.getHqLoc() == null || state.getWell() == null) {
                    String rcErrMsg = "ERR while DELIVERING_TO_HQ: ";
                    if(state.getHqLoc() == null) {
                        rcErrMsg += "I don't have an hq location. ";
                    }
                    if(state.getWell() == null) {
                        rcErrMsg += "I don't have a target well. ";
                    }
                    rcErrMsg += "I should not be in DELIVERING_TO_HQ state.";
                    rc.setIndicatorString(rcErrMsg);
                    throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, rcErrMsg);
                }
                else {
                    this.runPathingToHq.run(rc, state);
                    this.runDeliverToHq.run(rc, state);
                    if (!state.isAtHoldingCapacity()) {
                        rc.setIndicatorString("State Transition: to DELIVERING_TO_HQ");
                        state.setCurrentState(CarrierState.State.RETRIEVING_RESOURCES);
                    }
                }
                break;
        }
    }
}
