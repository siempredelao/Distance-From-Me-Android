package gc.david.dfm.elevation.domain;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.elevation.data.ElevationRepository;
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper;
import gc.david.dfm.elevation.data.model.ElevationEntity;
import gc.david.dfm.elevation.data.model.Result;
import gc.david.dfm.elevation.domain.model.Elevation;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;

import static gc.david.dfm.elevation.domain.ElevationInteractor.STATUS_OK;
import static gc.david.dfm.elevation.domain.ElevationInteractor.STATUS_UNKNOWN_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 11.01.17.
 */
public class ElevationInteractorTest {

    @Mock
    Executor                  executor;
    @Mock
    MainThread                mainThread;
    @Mock
    ElevationEntityDataMapper elevationEntityDataMapper;
    @Mock
    ElevationRepository       repository;
    @Mock
    ElevationUseCase.Callback callback;

    private ElevationInteractor elevationInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        elevationInteractor = new ElevationInteractor(executor, mainThread, elevationEntityDataMapper, repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Interactor) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executor).run(any(Interactor.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mainThread).post(any(Runnable.class));
    }

    @Test
    public void showsErrorWhenCoordinateListIsEmpty() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onError();
    }

    @Test
    public void returnsCallbackWhenElevationModelIsCorrect() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        List<Result> results = new ArrayList<>();
        double elevation = 1D;
        results.add(new Result.Builder().withElevation(elevation).build());
        ElevationEntity elevationEntity = new ElevationEntity.Builder().withStatus(STATUS_OK)
                                                                       .withResults(results)
                                                                       .build();
        when(repository.getElevation(anyString(), anyInt())).thenReturn(elevationEntity);
        List<Double> elevationResults = new ArrayList<>();
        elevationResults.add(elevation);
        Elevation elevation1 = new Elevation(elevationResults);
        when(elevationEntityDataMapper.transform(elevationEntity)).thenReturn(elevation1);

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onElevationLoaded(elevation1);
    }

    @Test
    public void showsErrorWhenElevationModelIsNotCorrect() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        ElevationEntity elevationEntity = new ElevationEntity.Builder().withStatus(STATUS_UNKNOWN_ERROR).build();
        when(repository.getElevation(anyString(), anyInt())).thenReturn(elevationEntity);

        // When
        elevationInteractor.execute(coordinateList, anyInt(), callback);

        // Then
        verify(callback).onError();
    }

    @Test
    public void buildsCoordinatesPathForListWithOneCoordinate() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        String coordinatesPath = "0.0,0.0";
        int maxSamples = 1;

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback);

        // Then
        verify(repository).getElevation(coordinatesPath, maxSamples);
    }

    @Test
    public void buildsCoordinatesPathForListWithMoreThanOneCoordinate() {
        // Given
        List<LatLng> coordinateList = new ArrayList<>();
        coordinateList.add(new LatLng(0D, 0D));
        coordinateList.add(new LatLng(1D, 1D));
        String coordinatesPath = "0.0,0.0|1.0,1.0";
        int maxSamples = 1;

        // When
        elevationInteractor.execute(coordinateList, maxSamples, callback);

        // Then
        verify(repository).getElevation(coordinatesPath, maxSamples);
    }
}