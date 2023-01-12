package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import hydr8player.v1.Carrier.v2.CarrierState;

/**
 * RunSearchForWell searches for wells
 * side effects: setFoundWell
 */
public class RunSearchForWell {
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("Should I search for a well?");
        if(state.getFoundWell() != null){
            rc.setIndicatorString("Searching for a well.");
            WellInfo[] wells = rc.senseNearbyWells();
            if(wells != null && wells.length > 0) {
                rc.setIndicatorString("I found a well.");
                state.setFoundWell(wells[0]);
            }
        } else {
            rc.setIndicatorString("I already know where a well is.");
        }
    }
}
