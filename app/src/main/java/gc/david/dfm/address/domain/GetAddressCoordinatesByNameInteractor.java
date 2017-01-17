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

package gc.david.dfm.address.domain;

import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;

/**
 * Created by david on 12.01.17.
 */
public class GetAddressCoordinatesByNameInteractor extends GetAddressAbstractInteractor<String> {

    public GetAddressCoordinatesByNameInteractor(final Executor executor,
                                                 final MainThread mainThread,
                                                 final AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                 final AddressRepository repository) {
        super(executor, mainThread, addressCollectionEntityDataMapper, repository);
    }

    @Override
    protected void repositoryCall(final String locationName, final AddressRepository.Callback callback) {
        repository.getCoordinatesByName(locationName, callback);
    }
}
