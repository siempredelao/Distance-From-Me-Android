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

package gc.david.dfm.address.domain

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.whenever

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.ArrayList

import gc.david.dfm.address.data.AddressRepository
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.data.model.AddressCollectionEntity
import gc.david.dfm.address.data.model.Geometry
import gc.david.dfm.address.data.model.Location
import gc.david.dfm.address.data.model.Result
import gc.david.dfm.address.domain.model.Address
import gc.david.dfm.address.domain.model.AddressCollection
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread

import gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_INVALID_REQUEST
import gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_OK
import gc.david.dfm.address.domain.GetAddressAbstractInteractor.STATUS_ZERO_RESULTS
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Created by david on 15.01.17.
 */
class GetAddressCoordinatesByNameInteractorTest {

    @Mock
    lateinit var executor: Executor
    @Mock
    lateinit var mainThread: MainThread
    @Mock
    lateinit var dataMapper: AddressCollectionEntityDataMapper
    @Mock
    lateinit var repository: AddressRepository
    @Mock
    lateinit var callback: GetAddressUseCase.Callback

    private lateinit var getAddressInteractor: GetAddressCoordinatesByNameInteractor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        getAddressInteractor = GetAddressCoordinatesByNameInteractor(executor, mainThread, dataMapper, repository)

        doAnswer  {
                (it.arguments[0] as Interactor).run()
        }.whenever(executor).run(any())
        doAnswer {
                (it.arguments[0] as Runnable).run()
        }.whenever(mainThread).post(any())
    }

    @Test
    fun `loads empty address when address collection model is correct`() {
        val fakeLocationName = "Berlin DE"
        val results = ArrayList<Result>()
        val addressCollectionEntity = AddressCollectionEntity.Builder().withStatus(
                STATUS_ZERO_RESULTS).withResults(results).build()
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), any())

        val addressList = ArrayList<Address>()
        val addressCollection = AddressCollection(addressList)
        whenever(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        getAddressInteractor.execute(fakeLocationName, anyInt(), callback)

        verify(callback).onAddressLoaded(addressCollection)
    }

    @Test
    fun `loads non empty address when address collection model is correct`() {
        val fakeLocationName = "Berlin DE"
        val results = ArrayList<Result>()
        val latitude = 1.0
        val longitude = 1.0
        val location = Location.Builder().withLatitude(latitude).withLongitude(longitude).build()
        val geometry = Geometry.Builder().withLocation(location).build()
        results.add(Result.Builder().withFormattedAddress(fakeLocationName).withGeometry(geometry).build())
        val addressCollectionEntity = AddressCollectionEntity.Builder().withStatus(
                STATUS_OK).withResults(results).build()
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), any())

        val address = Address(fakeLocationName, LatLng(latitude, longitude))
        val addressList = ArrayList<Address>()
        addressList.add(address)
        val addressCollection = AddressCollection(addressList)
        whenever(dataMapper.transform(addressCollectionEntity)).thenReturn(addressCollection)

        getAddressInteractor.execute(fakeLocationName, anyInt(), callback)

        verify(callback).onAddressLoaded(addressCollection)
    }

    @Test
    fun `returns error callback when address collection model is not correct`() {
        val addressCollectionEntity = AddressCollectionEntity.Builder().withStatus(
                STATUS_INVALID_REQUEST).build()
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onSuccess(addressCollectionEntity)
        }.whenever(repository).getCoordinatesByName(anyString(), any())
        val fakeLocationName = "Berlin DE"

        getAddressInteractor.execute(fakeLocationName, anyInt(), callback)

        verify(callback).onError(STATUS_INVALID_REQUEST)
    }

    @Test
    fun `returns error callback when repository error callback`() {
        val fakeErrorMessage = "fake error message"
        doAnswer {
                (it.arguments[1] as AddressRepository.Callback).onError(fakeErrorMessage)
        }.whenever(repository).getCoordinatesByName(anyString(), any())
        val fakeLocationName = "Berlin DE"

        getAddressInteractor.execute(fakeLocationName, anyInt(), callback)

        verify(callback).onError(fakeErrorMessage)
    }
}