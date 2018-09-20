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

package gc.david.dfm.address.domain;

import com.google.android.gms.maps.model.LatLng;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;

/**
 * Created by david on 12.01.17.
 */
public class GetAddressNameByCoordinatesInteractor extends GetAddressAbstractInteractor<LatLng> {

    public GetAddressNameByCoordinatesInteractor(final Executor executor,
                                                 final MainThread mainThread,
                                                 final AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                 final AddressRepository repository) {
        super(executor, mainThread, addressCollectionEntityDataMapper, repository);
    }

    @Override
    protected void repositoryCall(final LatLng coordinates, final AddressRepository.Callback callback) {
        repository.getNameByCoordinates(coordinates, callback);
    }
}
