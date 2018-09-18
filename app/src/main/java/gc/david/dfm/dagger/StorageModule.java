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

package gc.david.dfm.dagger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.migration.UpgradeHelper;
import gc.david.dfm.model.DaoMaster;
import gc.david.dfm.model.DaoSession;

/**
 * Created by david on 16.01.17.
 */
@Module
public class StorageModule {

    @Provides
    @Singleton
    DaoSession getDaoSession(Context context) {
        final UpgradeHelper helper = new UpgradeHelper(context, "DistanciasDB.db", null);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final DaoMaster daoMaster = new DaoMaster(db);
        return daoMaster.newSession();
    }
}
