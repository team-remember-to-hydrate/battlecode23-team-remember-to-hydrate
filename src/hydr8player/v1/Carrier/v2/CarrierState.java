package hydr8player.v1.Carrier.v2;

import battlecode.common.WellInfo;
import battlecode.common.MapLocation;
/**
 * CarrierState represents the state of a carrier
 */
public class CarrierState {
    private State currentState = State.SEARCHING_FOR_WELL;
    private MapLocation hqLoc = null;
    private MapLocation travelLoc = null;
    public CarrierState(){}
    public CarrierState(State currentState){
        this.currentState = currentState;
    }
    private WellInfo foundWell = null;
    private boolean isAtHoldingCapacity = false;
    public WellInfo getWell() {
        return this.foundWell;
    }
    public void setWell(WellInfo wellInfo) {
        this.foundWell = wellInfo;
    }
    public MapLocation getHqLoc() {
        return this.hqLoc;
    }
    public void setHqLoc(MapLocation hqLoc) { this.hqLoc = hqLoc; }
    public void setTravelLoc(MapLocation travelLoc) { this.travelLoc = travelLoc; }
    public boolean isAtHoldingCapacity() { return this.isAtHoldingCapacity; }
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
