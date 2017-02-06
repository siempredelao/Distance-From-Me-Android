/*
 * Copyright (c) 2017 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.opensource.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.opensource.data.OpenSourceRepository;
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Created by david on 26.01.17.
 */
public class OpenSourceInteractorTest {

    @Mock
    Executor                   executor;
    @Mock
    MainThread                 mainThread;
    @Mock
    OpenSourceRepository       repository;
    @Mock
    OpenSourceUseCase.Callback callback;

    private OpenSourceInteractor openSourceInteractor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        openSourceInteractor = new OpenSourceInteractor(executor, mainThread, repository);

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
    public void returnsOpenSourceLibraryListOnSuccess() {
        // Given
        final List<OpenSourceLibraryEntity> openSourceLibraryEntityList = new ArrayList<>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((OpenSourceRepository.Callback) invocation.getArguments()[0]).onSuccess(openSourceLibraryEntityList);
                return null;
            }
        }).when(repository).getOpenSourceLibraries(any(OpenSourceRepository.Callback.class));

        // When
        openSourceInteractor.execute(callback);

        // Then
        verify(callback).onOpenSourceLibrariesLoaded(openSourceLibraryEntityList);
    }

    @Test
    public void returnsErrorMessageOnFailure() {
        // Given
        final String errorMessage = "fake error message";

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((OpenSourceRepository.Callback) invocation.getArguments()[0]).onError(errorMessage);
                return null;
            }
        }).when(repository).getOpenSourceLibraries(any(OpenSourceRepository.Callback.class));

        // When
        openSourceInteractor.execute(callback);

        // Then
        verify(callback).onError(errorMessage);
    }
}