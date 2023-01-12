package hydr8player.v1;

import battlecode.common.WellInfo;

public class CarrierGatherResourcesState {
    public enum State {
        SEARCHING_FOR_WELL,
        RETRIEVING_RESOURCE,
        RETURNING_TO_HQ
    }
    private State currentState = State.SEARCHING_FOR_WELL;
    private WellInfo foundWell = null;
    public WellInfo getFoundWell() {
        return this.foundWell;
    }

    public State getCurrentState() {
        return this.currentState;
    }
}
