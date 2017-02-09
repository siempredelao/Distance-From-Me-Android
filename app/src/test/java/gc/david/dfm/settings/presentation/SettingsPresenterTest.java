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

package gc.david.dfm.settings.presentation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gc.david.dfm.distance.domain.ClearDistancesUseCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Created by david on 24.01.17.
 */
public class SettingsPresenterTest {

    @Mock
    Settings.View         settingsView;
    @Mock
    ClearDistancesUseCase clearDistancesUseCase;

    private SettingsPresenter settingsPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        settingsPresenter = new SettingsPresenter(settingsView, clearDistancesUseCase);
    }

    @Test
    public void showsSuccessMessageWhenUseCaseSucceeds() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ClearDistancesUseCase.Callback) invocation.getArguments()[0]).onClear();
                return null;
            }
        }).when(clearDistancesUseCase).execute(any(ClearDistancesUseCase.Callback.class));

        // When
        settingsPresenter.onClearData();

        // Then
        verify(settingsView).showClearDataSuccessMessage();
    }

    @Test
    public void showsErrorMessageWhenUseCaseFails() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ClearDistancesUseCase.Callback) invocation.getArguments()[0]).onError();
                return null;
            }
        }).when(clearDistancesUseCase).execute(any(ClearDistancesUseCase.Callback.class));

        // When
        settingsPresenter.onClearData();

        // Then
        verify(settingsView).showClearDataErrorMessage();
    }
}