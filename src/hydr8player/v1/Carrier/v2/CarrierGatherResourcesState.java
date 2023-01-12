package hydr8player.v1.Carrier.v2;

import battlecode.common.WellInfo;

public class CarrierGatherResourcesState {
    private State currentState = State.SEARCHING_FOR_WELL;
    private WellInfo foundWell = null;

    public void setFoundWell(WellInfo wellInfo) {
        this.foundWell = wellInfo;
    }

    public enum State {
        SEARCHING_FOR_WELL,
        RETRIEVING_RESOURCE,
        RETURNING_TO_HQ
    }
    public void setCurrentState(State state) {
        this.currentState = state;
    }
    public WellInfo getFoundWell() {
        return this.foundWell;
    }

    public State getCurrentState() {
        return this.currentState;
    }
}
