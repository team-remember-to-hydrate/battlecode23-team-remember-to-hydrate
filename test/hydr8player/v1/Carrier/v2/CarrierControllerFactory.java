package hydr8player.v1.Carrier.v2;

import hydr8player.v1.Carrier.v2.capabilities.*;

import static org.mockito.Mockito.mock;

public class CarrierControllerFactory {
    public static CarrierController createMock(){
        RunFindWell mockedRunFindWell = mock(RunFindWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        RunDeliverToHq mockedRunDeliverToHq = mock(RunDeliverToHq.class);
        RunPathingToWell mockedRunPathingToWell = mock(RunPathingToWell.class);
        RunPathingToHq mockedRunPathingToHq = mock(RunPathingToHq.class);
        CarrierController cc = new CarrierController(
                mockedRunFindWell,
                mockedRunRetrieveResources,
                mockedRunDeliverToHq,
                mockedRunPathingToWell,
                mockedRunPathingToHq
        );
        return cc;
    }
}
