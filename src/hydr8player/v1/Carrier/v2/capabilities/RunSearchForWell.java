package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;

/**
 * RunSearchForWell searches for wells
 * side effects: setFoundWell

public class RunSearchForWell {
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("Capability: RunSearchForWell");
        if(state.getFoundWell() != null) {
            rc.setIndicatorString("RunSearchForWell > No well found yet.");
            WellInfo[] wells = rc.senseNearbyWells();
            if(wells != null && wells.length > 0) {
                rc.setIndicatorString("RunSearchForWell > I found a well.");
                state.setFoundWell(wells[0]);
            }
        } else {
            rc.setIndicatorString("RunSearchForWell > I already know where a well is.");
        }
    }
}
*/