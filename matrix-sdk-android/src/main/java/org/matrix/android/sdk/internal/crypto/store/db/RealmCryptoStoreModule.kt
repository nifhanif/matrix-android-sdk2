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

package com.energeek.android.sdk.internal.crypto.store.db

import com.energeek.android.sdk.internal.crypto.store.db.model.CrossSigningInfoEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.CryptoMetadataEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.CryptoRoomEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.DeviceInfoEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.GossipingEventEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.IncomingGossipingRequestEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.KeyInfoEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.KeysBackupDataEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.MyDeviceLastSeenInfoEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.OlmInboundGroupSessionEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.OlmSessionEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.OutgoingGossipingRequestEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.SharedSessionEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.TrustLevelEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.UserEntity
import com.energeek.android.sdk.internal.crypto.store.db.model.WithHeldSessionEntity
import io.realm.annotations.RealmModule
import com.energeek.android.sdk.internal.crypto.store.db.model.OutboundGroupSessionInfoEntity

/**
 * Realm module for Crypto store classes
 */
@RealmModule(library = true,
        classes = [
            CryptoMetadataEntity::class,
            CryptoRoomEntity::class,
            DeviceInfoEntity::class,
            KeysBackupDataEntity::class,
            OlmInboundGroupSessionEntity::class,
            OlmSessionEntity::class,
            UserEntity::class,
            KeyInfoEntity::class,
            CrossSigningInfoEntity::class,
            TrustLevelEntity::class,
            GossipingEventEntity::class,
            IncomingGossipingRequestEntity::class,
            OutgoingGossipingRequestEntity::class,
            MyDeviceLastSeenInfoEntity::class,
            WithHeldSessionEntity::class,
            SharedSessionEntity::class,
            OutboundGroupSessionInfoEntity::class
        ])
internal class RealmCryptoStoreModule
