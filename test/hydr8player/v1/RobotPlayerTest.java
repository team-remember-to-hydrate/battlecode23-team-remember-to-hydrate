package hydr8player.v1;

import static org.junit.Assert.*;

import battlecode.common.WellInfo;
import org.junit.Test;

public class RobotPlayerTest {
    @Test
    public void testCarrierGatherResourcesStateDefault() {
        CarrierGatherResourcesState carrierGatherResourcesState = new CarrierGatherResourcesState();
        assert(carrierGatherResourcesState.getFoundWell() == null);
        assert(carrierGatherResourcesState.getCurrentState() == CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
    }
}
