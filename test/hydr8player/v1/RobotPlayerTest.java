package hydr8player.v1;

import static hydr8player.v1.RobotPlayer.runCarrierGatherResources;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import org.junit.Test;

public class RobotPlayerTest {
    @Test
    public void testCarrierGatherResourcesStateDefault() {
        CarrierGatherResourcesState carrierGatherResourcesState = new CarrierGatherResourcesState();
        assert(carrierGatherResourcesState.getFoundWell() == null);
        assert(carrierGatherResourcesState.getCurrentState() == CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
    }

    @Test
    public void testCarrierBeginsRetrievingResourcesWhenWellIsFound() throws GameActionException {
        // given
        RobotController mockedRc = mock(RobotController.class);
        WellInfo mockedWellInfo = mock(WellInfo.class);
        CarrierGatherResourcesState state = new CarrierGatherResourcesState();
        state.setCurrentState(CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(mockedWellInfo);

        // when
        runCarrierGatherResources(mockedRc, state);

        // then
        assertEquals(state.getCurrentState(), CarrierGatherResourcesState.State.RETRIEVING_RESOURCE);
    }

    @Test
    public void testCarrierContinuesSearchingWhenWellIsNotFound() throws GameActionException {
        // given
        RobotController mockedRc = mock(RobotController.class);
        CarrierGatherResourcesState state = new CarrierGatherResourcesState();
        state.setCurrentState(CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(null);

        // when
        runCarrierGatherResources(mockedRc, state);

        // then
        assertEquals(state.getCurrentState(), CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
    }

}
