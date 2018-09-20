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

package gc.david.dfm.address.data.mapper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.address.data.model.AddressCollectionEntity;
import gc.david.dfm.address.data.model.Geometry;
import gc.david.dfm.address.data.model.Location;
import gc.david.dfm.address.data.model.Result;
import gc.david.dfm.address.domain.model.Address;
import gc.david.dfm.address.domain.model.AddressCollection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by david on 15.01.17.
 */
public class AddressCollectionEntityDataMapperTest {

    @Test
    public void transformsAddressCollectionEntityToAddressCollection() {
        // Given
        final String fakeAddress = "address";
        final double latitude = 1D;
        final double longitude = 2D;
        final Location location = new Location.Builder().withLatitude(latitude).withLongitude(longitude).build();
        final Geometry geometry = new Geometry.Builder().withLocation(location).build();
        Result result = new Result.Builder().withFormattedAddress(fakeAddress).withGeometry(geometry).build();
        List<Result> results = new ArrayList<>();
        results.add(result);
        AddressCollectionEntity addressCollectionEntity = new AddressCollectionEntity.Builder().withResults(results)
                                                                                               .build();

        // When
        final AddressCollection addressCollection = new AddressCollectionEntityDataMapper().transform(
                addressCollectionEntity);

        // Then
        assertEquals(1, addressCollection.getAddressList().size());
        final Address address = addressCollection.getAddressList().get(0);
        assertThat(address.getCoordinates().latitude, is(latitude));
        assertThat(address.getCoordinates().longitude, is(longitude));
        assertThat(address.getFormattedAddress(), is(fakeAddress));
    }

}