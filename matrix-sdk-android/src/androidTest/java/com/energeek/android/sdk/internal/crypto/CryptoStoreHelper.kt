/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nusaraya.android.sdk.internal.crypto

import com.nusaraya.android.sdk.internal.crypto.store.IMXCryptoStore
import com.nusaraya.android.sdk.internal.crypto.store.db.RealmCryptoStore
import com.nusaraya.android.sdk.internal.crypto.store.db.RealmCryptoStoreModule
import com.nusaraya.android.sdk.internal.crypto.store.db.mapper.CrossSigningKeysMapper
import com.nusaraya.android.sdk.internal.di.MoshiProvider
import io.realm.RealmConfiguration
import kotlin.random.Random

internal class CryptoStoreHelper {

    fun createStore(): IMXCryptoStore {
        return RealmCryptoStore(
                realmConfiguration = RealmConfiguration.Builder()
                        .name("test.realm")
                        .modules(RealmCryptoStoreModule())
                        .build(),
                crossSigningKeysMapper = CrossSigningKeysMapper(MoshiProvider.providesMoshi()),
                userId = "userId_" + Random.nextInt(),
                deviceId = "deviceId_sample"
        )
    }
}
