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

package com.energeek.android.sdk.internal.crypto.algorithms.megolm

import com.energeek.android.sdk.internal.crypto.DeviceListManager
import com.energeek.android.sdk.internal.crypto.MXOlmDevice
import com.energeek.android.sdk.internal.crypto.OutgoingGossipingRequestManager
import com.energeek.android.sdk.internal.crypto.actions.EnsureOlmSessionsForDevicesAction
import com.energeek.android.sdk.internal.crypto.actions.MessageEncrypter
import com.energeek.android.sdk.internal.crypto.store.IMXCryptoStore
import com.energeek.android.sdk.internal.crypto.tasks.SendToDeviceTask
import com.energeek.android.sdk.internal.di.UserId
import com.energeek.android.sdk.internal.util.MatrixCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal class MXMegolmDecryptionFactory @Inject constructor(
        @UserId private val userId: String,
        private val olmDevice: MXOlmDevice,
        private val deviceListManager: DeviceListManager,
        private val outgoingGossipingRequestManager: OutgoingGossipingRequestManager,
        private val messageEncrypter: MessageEncrypter,
        private val ensureOlmSessionsForDevicesAction: EnsureOlmSessionsForDevicesAction,
        private val cryptoStore: IMXCryptoStore,
        private val sendToDeviceTask: SendToDeviceTask,
        private val coroutineDispatchers: MatrixCoroutineDispatchers,
        private val cryptoCoroutineScope: CoroutineScope
) {

    fun create(): MXMegolmDecryption {
        return MXMegolmDecryption(
                userId,
                olmDevice,
                deviceListManager,
                outgoingGossipingRequestManager,
                messageEncrypter,
                ensureOlmSessionsForDevicesAction,
                cryptoStore,
                sendToDeviceTask,
                coroutineDispatchers,
                cryptoCoroutineScope)
    }
}
