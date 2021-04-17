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

package com.nusaraya.android.sdk.internal.crypto.algorithms.olm

import com.nusaraya.android.sdk.internal.crypto.DeviceListManager
import com.nusaraya.android.sdk.internal.crypto.MXOlmDevice
import com.nusaraya.android.sdk.internal.crypto.actions.EnsureOlmSessionsForUsersAction
import com.nusaraya.android.sdk.internal.crypto.actions.MessageEncrypter
import com.nusaraya.android.sdk.internal.crypto.store.IMXCryptoStore
import com.nusaraya.android.sdk.internal.util.MatrixCoroutineDispatchers
import javax.inject.Inject

internal class MXOlmEncryptionFactory @Inject constructor(private val olmDevice: MXOlmDevice,
                                                          private val cryptoStore: IMXCryptoStore,
                                                          private val messageEncrypter: MessageEncrypter,
                                                          private val deviceListManager: DeviceListManager,
                                                          private val coroutineDispatchers: MatrixCoroutineDispatchers,
                                                          private val ensureOlmSessionsForUsersAction: EnsureOlmSessionsForUsersAction) {

    fun create(roomId: String): MXOlmEncryption {
        return MXOlmEncryption(
                roomId,
                olmDevice,
                cryptoStore,
                messageEncrypter,
                deviceListManager,
                ensureOlmSessionsForUsersAction)
    }
}
