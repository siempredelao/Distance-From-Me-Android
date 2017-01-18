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

package gc.david.dfm.deviceinfo;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import gc.david.dfm.deviceinfo.DeviceInfo;
import gc.david.dfm.deviceinfo.DeviceInfoApi16Decorator;
import gc.david.dfm.deviceinfo.MemoryInfo;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by david on 18.01.17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class DeviceInfoApi16DecoratorTest {

    @Mock
    DeviceInfo decoratedDeviceInfo;
    @Mock
    MemoryInfo memoryInfo;

    private DeviceInfoApi16Decorator deviceInfoApi16Decorator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        deviceInfoApi16Decorator = new DeviceInfoApi16Decorator(decoratedDeviceInfo, memoryInfo);
    }

    @Test
    public void decoratorIsCalled() {
        // When
        deviceInfoApi16Decorator.getDeviceInfo();

        // Then
        verify(decoratedDeviceInfo).getDeviceInfo();
    }

    @Test
    public void returnsDeviceInfoWithMemoryInfo() {
        // Given
        final String fakeDeviceInfo = "fake device info";
        final long availableMemInBytes = 2L;
        final long freeMemInBytes = 1L;
        when(decoratedDeviceInfo.getDeviceInfo()).thenReturn(fakeDeviceInfo);
        when(memoryInfo.getAvailableMemory()).thenReturn(availableMemInBytes);
        when(memoryInfo.getFreeMemory()).thenReturn(freeMemInBytes);

        // When
        final String actualDeviceInfo = deviceInfoApi16Decorator.getDeviceInfo();

        // Then
        final String fakeMemoryInfo = DeviceInfoApi16Decorator.MemoryPrinter.print(availableMemInBytes, freeMemInBytes);
        final String expectedDeviceInfo = DeviceInfoApi16Decorator.DeviceInfoPrinter.print(fakeDeviceInfo,
                                                                                           fakeMemoryInfo);
        assertEquals(expectedDeviceInfo, actualDeviceInfo);
    }
}