package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import hydr8player.v1.Carrier.v2.CarrierState;

/**
 * RunSearchForWell searches for wells
 * side effects: setFoundWell
 */
public class RunFindWell {
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        rc.setIndicatorString("Capability: RunSearchForWell");
        if(state.getWell() == null) {
            rc.setIndicatorString("RunSearchForWell > No well found yet.");
            WellInfo[] wells = rc.senseNearbyWells();
            if(wells != null && wells.length > 0) {
                state.setWell(wells[0]);
                rc.setIndicatorString("RunSearchForWell > I found a well.");
            }
        } else {
            rc.setIndicatorString("RunSearchForWell > I already know where a well is.");
        }
    }
}
