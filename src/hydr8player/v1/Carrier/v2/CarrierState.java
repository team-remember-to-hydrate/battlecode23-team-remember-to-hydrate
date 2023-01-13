package hydr8player.v1.Carrier.v2;

import battlecode.common.WellInfo;
/**
 * CarrierState represents the state of a carrier
 */
public class CarrierState {
    private State currentState = State.SEARCHING_FOR_WELL;

    public CarrierState(){}
    public CarrierState(State currentState){
        this.currentState = currentState;
    }
    private WellInfo foundWell = null;
    private boolean isAtHoldingCapacity = false;

    public WellInfo getFoundWell() {
        return this.foundWell;
    }

    public void setFoundWell(WellInfo wellInfo) {
        this.foundWell = wellInfo;
    }

    public boolean getIsAtHoldingCapacity() { return this.isAtHoldingCapacity; }
    public void setIsAtHoldingCapacity(boolean value) { this.isAtHoldingCapacity = value; }

    public State getCurrentState() {
        return this.currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }
    public enum State {
        SEARCHING_FOR_WELL,
        RETRIEVING_RESOURCES,
        DELIVERING_TO_HQ
    }
}
