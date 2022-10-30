/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

/**
 * Created by david on 25.01.17.
 */
class GetOpenSourceLibrariesUseCase(
    private val repository: OpenSourceRepository,
    private val mapper: OpenSourceLibraryMapper
) {

    suspend operator fun invoke(): Result<List<OpenSourceLibrary>> {
        return try {
            val openSourceLibraryEntities = repository.getOpenSourceLibraries()
            val openSourceLibraries = mapper(openSourceLibraryEntities)

            Result.success(openSourceLibraries)
        } catch (exception: Throwable) {
            Result.failure(exception)
        }
    }
}
