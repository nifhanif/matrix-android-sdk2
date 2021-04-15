/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
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

package com.energeek.android.sdk.internal.database.query

import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import com.energeek.android.sdk.internal.database.model.RawCacheEntity
import com.energeek.android.sdk.internal.database.model.RawCacheEntityFields

/**
 * Get the current RawCacheEntity, return null if it does not exist
 */
internal fun RawCacheEntity.Companion.get(realm: Realm, url: String): RawCacheEntity? {
    return realm.where<RawCacheEntity>()
            .equalTo(RawCacheEntityFields.URL, url)
            .findFirst()
}

/**
 * Get the current RawCacheEntity, create one if it does not exist
 */
internal fun RawCacheEntity.Companion.getOrCreate(realm: Realm, url: String): RawCacheEntity {
    return get(realm, url) ?: realm.createObject(url)
}
