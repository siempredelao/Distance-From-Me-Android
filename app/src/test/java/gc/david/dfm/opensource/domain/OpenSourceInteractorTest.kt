/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.opensource.domain

import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread
import gc.david.dfm.opensource.data.OpenSourceRepository
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by david on 26.01.17.
 */
class OpenSourceInteractorTest {

    @Mock
    lateinit var executor: Executor
    @Mock
    lateinit var mainThread: MainThread
    @Mock
    lateinit var repository: OpenSourceRepository
    @Mock
    lateinit var callback: OpenSourceUseCase.Callback

    private lateinit var openSourceInteractor: OpenSourceInteractor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        openSourceInteractor = OpenSourceInteractor(executor, mainThread, repository)

        doAnswer {
                (it.arguments[0] as Interactor).run()
        }.whenever(executor).run(any(Interactor::class.java))
        doAnswer {
                (it.arguments[0] as Runnable).run()
        }.whenever(mainThread).post(any(Runnable::class.java))
    }

    @Test
    fun returnsOpenSourceLibraryListOnSuccess() {
        // Given
        val openSourceLibraryEntityList = ArrayList<OpenSourceLibraryEntity>()

        doAnswer {
                (it.arguments[0] as OpenSourceRepository.Callback).onSuccess(openSourceLibraryEntityList)
        }.whenever(repository).getOpenSourceLibraries(any(OpenSourceRepository.Callback::class.java))

        // When
        openSourceInteractor.execute(callback)

        // Then
        verify(callback).onOpenSourceLibrariesLoaded(openSourceLibraryEntityList)
    }

    @Test
    fun returnsErrorMessageOnFailure() {
        // Given
        val errorMessage = "fake error message"

        doAnswer {
                (it.arguments[0] as OpenSourceRepository.Callback).onError(errorMessage)
        }.whenever(repository).getOpenSourceLibraries(any(OpenSourceRepository.Callback::class.java))

        // When
        openSourceInteractor.execute(callback)

        // Then
        verify(callback).onError(errorMessage)
    }
}