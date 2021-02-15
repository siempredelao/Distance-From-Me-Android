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

package gc.david.dfm.address.domain

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.whenever
import gc.david.dfm.address.data.AddressRepository
import gc.david.dfm.address.data.NewAddressRemoteDataSource
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.data.model.*
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.NewMainThread
import gc.david.dfm.executor.NewThreadExecutor
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.*

/**
 * Created by david on 15.01.17.
 */
class GetAddressCoordinatesByNameInteractorTest {

    @Mock
    lateinit var executor: NewThreadExecutor
    @Mock
    lateinit var mainThread: NewMainThread
    @Mock
    lateinit var dataMapper: AddressCollectionEntityDataMapper
    @Mock
    lateinit var repository: NewAddressRemoteDataSource
    @Mock
    lateinit var callback: GetAddressCoordinatesByNameInteractor.Callback

    private lateinit var getAddressInteractor: GetAddressCoordinatesByNameInteractor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        getAddressInteractor = GetAddressCoordinatesByNameInteractor(executor, mainThread, dataMapper, repository)

        doAnswer { (it.arguments[0] as Interactor).run() }.whenever(executor).run(anyOrNull())
        doAnswer { (it.arguments[0] as Runnable).run() }.whenever(mainThread).post(anyOrNull())
    }

    @Test
    fun `loads empty address when address collection model is correct`() {
        val addressCollectionEntity = AddressCollectionEntity(emptyList(), GeocodingStatus.ZERO_RESULTS)
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), anyOrNull())

        val addressCollection = AddressCollection(mutableListOf())
        whenever(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        getAddressInteractor.execute(LOCATION_NAME, anyInt(), callback)

        verify(callback).onAddressLoaded(addressCollection)
    }

    @Test
    fun `loads non empty address when address collection model is correct`() {
        val latitude = 1.0
        val longitude = 1.0
        val location = Location(latitude, longitude)
        val geometry = Geometry(location)
        val results = ArrayList<Result>()
        results.add(Result(LOCATION_NAME, geometry))
        val addressCollectionEntity = AddressCollectionEntity(results, GeocodingStatus.OK)
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), anyOrNull())

        val address = Address(LOCATION_NAME, LatLng(latitude, longitude))
        val addressList = mutableListOf(address)
        val addressCollection = AddressCollection(addressList)
        whenever(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        getAddressInteractor.execute(LOCATION_NAME, 1, callback)

        verify(callback).onAddressLoaded(addressCollection)
    }

    @Test
    fun `returns error callback when address collection model is not correct`() {
        val addressCollectionEntity = AddressCollectionEntity(status = GeocodingStatus.INVALID_REQUEST)
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), anyOrNull())

        getAddressInteractor.execute(LOCATION_NAME, anyInt(), callback)

        verify(callback).onError(GeocodingStatus.INVALID_REQUEST.toString())
    }

    @Test
    fun `returns error callback when repository error callback`() {
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onError(ERROR_MESSAGE)
        }.whenever(repository).getCoordinatesByName(anyString(), anyOrNull())


        getAddressInteractor.execute(LOCATION_NAME, anyInt(), callback)

        verify(callback).onError(ERROR_MESSAGE)
    }

    companion object {

        private const val LOCATION_NAME = "Berlin DE"
        private const val ERROR_MESSAGE = "fake error message"
    }
}