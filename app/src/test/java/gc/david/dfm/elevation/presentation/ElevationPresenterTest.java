package gc.david.dfm.elevation.presentation;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.PreferencesProvider;
import gc.david.dfm.elevation.domain.ElevationUseCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 11.01.17.
 */
public class ElevationPresenterTest {

    @Mock
    Elevation.View      elevationView;
    @Mock
    ElevationUseCase    elevationUseCase;
    @Mock
    ConnectionManager   connectionManager;
    @Mock
    PreferencesProvider preferencesProvider;

    private ElevationPresenter elevationPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        elevationPresenter = new ElevationPresenter(elevationView,
                                                    elevationUseCase,
                                                    connectionManager,
                                                    preferencesProvider);
    }

    @Test
    public void hidesChartWhenShowElevationChartPreferenceIsFalse() {
        // Given
        List<LatLng> dummyList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(false);

        // When
        elevationPresenter.buildChart(dummyList);

        // Then
        verify(elevationView).hideChart();
    }

    @Test
    public void hidesChartWhenNoConnectionAvailable() {
        // Given
        List<LatLng> dummyList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(true);
        when(connectionManager.isOnline()).thenReturn(false);

        // When
        elevationPresenter.buildChart(dummyList);

        // Then
        verify(elevationView).hideChart();
    }

    @Test
    public void executesUseCaseWhenPreferenceIsActivatedAndConnectionAvailable() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(true);
        when(connectionManager.isOnline()).thenReturn(true);

        // When
        elevationPresenter.buildChart(coordinateList);

        // Then
        verify(elevationUseCase).execute(eq(coordinateList), anyInt(), any(ElevationUseCase.Callback.class));
    }

    @Test
    public void buildsChartWhenUseCaseReturnsData() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(true);
        when(connectionManager.isOnline()).thenReturn(true);
        final gc.david.dfm.elevation.domain.model.Elevation elevation = new gc.david.dfm.elevation.domain.model.Elevation(new ArrayList<Double>());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ElevationUseCase.Callback) invocation.getArguments()[2]).onElevationLoaded(elevation);
                return null;
            }
        }).when(elevationUseCase).execute(eq(coordinateList), anyInt(), any(ElevationUseCase.Callback.class));

        // When
        elevationPresenter.buildChart(coordinateList);

        // Then
        verify(elevationView).buildChart(elevation.getResults());
    }

    @Test
    public void doesNotBuildChartWhenUseCaseIsStopped() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(true);
        when(connectionManager.isOnline()).thenReturn(true);
        final gc.david.dfm.elevation.domain.model.Elevation elevation = new gc.david.dfm.elevation.domain.model.Elevation(new ArrayList<Double>());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                elevationPresenter.onReset(); // reset called before thread finishes
                ((ElevationUseCase.Callback) invocation.getArguments()[2]).onElevationLoaded(elevation);
                return null;
            }
        }).when(elevationUseCase).execute(eq(coordinateList), anyInt(), any(ElevationUseCase.Callback.class));

        // When
        elevationPresenter.buildChart(coordinateList);

        // Then
        verify(elevationView, never()).buildChart(elevation.getResults());
    }

    @Test
    @Ignore
    public void showsErrorWhenUseCaseReturnsError() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        when(preferencesProvider.shouldShowElevationChart()).thenReturn(true);
        when(connectionManager.isOnline()).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ElevationUseCase.Callback) invocation.getArguments()[1]).onError();
                return null;
            }
        }).when(elevationUseCase).execute(eq(coordinateList), anyInt(), any(ElevationUseCase.Callback.class));

        // When
        elevationPresenter.buildChart(coordinateList);

        // Then
        // verify(elevationView).showError();
    }

    @Test
    public void doesNotShowChartWhenMinimiseButtonIsShown() {
        // Given
        when(elevationView.isMinimiseButtonShown()).thenReturn(true);

        // When
        elevationPresenter.onChartBuilt();

        // Then
        verify(elevationView, never()).showChart();
    }

    @Test
    public void showsChartWhenMinimiseButtonIsNotShown() {
        // Given
        when(elevationView.isMinimiseButtonShown()).thenReturn(false);

        // When
        elevationPresenter.onChartBuilt();

        // Then
        verify(elevationView).showChart();
    }


}