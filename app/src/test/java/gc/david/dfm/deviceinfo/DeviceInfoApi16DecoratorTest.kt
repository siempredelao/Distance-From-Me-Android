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

package gc.david.dfm.deviceinfo

import android.os.Build
import androidx.annotation.RequiresApi
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by david on 18.01.17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
class DeviceInfoApi16DecoratorTest {

    @Mock
    lateinit var decoratedDeviceInfo: DeviceInfo
    @Mock
    lateinit var memoryInfo: MemoryInfo

    private lateinit var deviceInfoApi16Decorator: DeviceInfoApi16Decorator

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        deviceInfoApi16Decorator = DeviceInfoApi16Decorator(decoratedDeviceInfo, memoryInfo)
    }

    @Test
    fun `decorator is called`() {
        deviceInfoApi16Decorator.deviceInfo

        verify(decoratedDeviceInfo).deviceInfo
    }

    @Test
    fun `returns device info with memory info`() {
        val fakeDeviceInfo = "fake device info"
        val availableMemInBytes = 2L
        val freeMemInBytes = 1L
        whenever(decoratedDeviceInfo.deviceInfo).thenReturn(fakeDeviceInfo)
        whenever(memoryInfo.availableMemory).thenReturn(availableMemInBytes)
        whenever(memoryInfo.freeMemory).thenReturn(freeMemInBytes)

        val actualDeviceInfo = deviceInfoApi16Decorator.deviceInfo

        val fakeMemoryInfo = DeviceInfoApi16Decorator.MemoryPrinter.print(availableMemInBytes, freeMemInBytes)
        val expectedDeviceInfo =
                DeviceInfoApi16Decorator.DeviceInfoPrinter.print(fakeDeviceInfo, fakeMemoryInfo)
        assertEquals(expectedDeviceInfo, actualDeviceInfo)
    }
}