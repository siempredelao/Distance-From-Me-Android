/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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
package gc.david.dfm.executor

/**
 * Executor implementation can be based on different frameworks or techniques of asynchronous
 * execution, but every implementation will execute the Interactor out of the UI thread.
 *
 *
 * Use this class to execute an Interactor.
 *
 *
 * This is just a sample implementation of how a Interactor/Executor environment can be
 * implemented.
 * Ideally interactors should not know about Executor or MainThread dependency. Interactors client
 * code should get a Executor instance to execute interactors.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
interface Executor {

    fun run(interactor: Interactor)

}
