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
package gc.david.dfm.executor

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Executor implementation based on ThreadPoolExecutor. ThreadPoolExecutorConfig:
 *
 * Core pool size: 3.
 * Max pool size: 5.
 * Keep alive time: 120.
 * Time unit: seconds.
 * Work queue: LinkedBlockingQueue.
 * Thread factory: adds "android_" suffix to threads.
 *
 * @author Pedro Vicente Gómez Sánchez
 * @author Fernando Cejas
 */
class ThreadExecutor : Executor {

    private val threadPoolExecutor: ThreadPoolExecutor

    init {
        threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME.toLong(),
                TIME_UNIT,
                WORK_QUEUE,
                THREAD_FACTORY
        )
    }

    override fun run(interactor: Interactor) {
        threadPoolExecutor.submit { interactor.run() }
    }

    private class JobThreadFactory : ThreadFactory {

        private var counter = 0

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, THREAD_NAME + counter++)
        }

        companion object {

            private const val THREAD_NAME = "android_"
        }
    }

    companion object {

        private const val CORE_POOL_SIZE = 3
        private const val MAX_POOL_SIZE = 5
        private const val KEEP_ALIVE_TIME = 120

        private val TIME_UNIT = TimeUnit.SECONDS
        private val WORK_QUEUE = LinkedBlockingQueue<Runnable>()
        private val THREAD_FACTORY = JobThreadFactory()
    }
}
