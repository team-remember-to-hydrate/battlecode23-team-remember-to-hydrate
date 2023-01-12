package hydr8player.v1.Carrier.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import battlecode.common.GameActionException;
import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;
import org.junit.Test;

public class CarrierStateTest {
    @Test
    public void testCarrierShouldRunSearchForWellCapability() throws GameActionException {
        CarrierState state = new CarrierState();
        assertEquals(state.getCurrentState(), CarrierState.State.SEARCHING_FOR_WELL);
        assertEquals(state.getFoundWell(), null);
    }
}
