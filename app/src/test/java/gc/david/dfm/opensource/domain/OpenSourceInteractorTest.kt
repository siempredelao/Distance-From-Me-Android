/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.NewMainThread
import gc.david.dfm.executor.NewThreadExecutor
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 26.01.17.
 */
class OpenSourceInteractorTest {

    private val executor = mock<NewThreadExecutor>()
    private val mainThread = mock<NewMainThread>()
    private val repository = mock<OpenSourceRepository>()
    private val callback = mock<OpenSourceInteractor.Callback>()

    private val openSourceInteractor = OpenSourceInteractor(executor, mainThread, repository)

    @Before
    fun setUp() {
        doAnswer { (it.arguments[0] as Interactor).run() }.whenever(executor).run(any())
        doAnswer { (it.arguments[0] as Runnable).run() }.whenever(mainThread).post(any())
    }

    @Test
    fun `returns open source library list on success`() {
        val openSourceLibraryEntityList = emptyList<OpenSourceLibraryEntity>()
        doAnswer {
                (it.arguments[0] as OpenSourceRepository.Callback).onSuccess(openSourceLibraryEntityList)
        }.whenever(repository).getOpenSourceLibraries(any())

        openSourceInteractor.execute(callback)

        verify(callback).onOpenSourceLibrariesLoaded(openSourceLibraryEntityList)
    }

    @Test
    fun `returns error message on failure`() {
        val errorMessage = "fake error message"
        doAnswer {
                (it.arguments[0] as OpenSourceRepository.Callback).onError(errorMessage)
        }.whenever(repository).getOpenSourceLibraries(any())

        openSourceInteractor.execute(callback)

        verify(callback).onError(errorMessage)
    }
}