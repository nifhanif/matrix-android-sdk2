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

package com.energeek.android.sdk.api.session.crypto

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.energeek.android.sdk.api.MatrixCallback
import com.energeek.android.sdk.api.auth.UserInteractiveAuthInterceptor
import com.energeek.android.sdk.api.listeners.ProgressListener
import com.energeek.android.sdk.api.session.crypto.crosssigning.CrossSigningService
import com.energeek.android.sdk.api.session.crypto.keysbackup.KeysBackupService
import com.energeek.android.sdk.api.session.crypto.keyshare.GossipingRequestListener
import com.energeek.android.sdk.api.session.crypto.verification.VerificationService
import com.energeek.android.sdk.api.session.events.model.Content
import com.energeek.android.sdk.api.session.events.model.Event
import com.energeek.android.sdk.internal.crypto.IncomingRoomKeyRequest
import com.energeek.android.sdk.internal.crypto.MXEventDecryptionResult
import com.energeek.android.sdk.internal.crypto.NewSessionListener
import com.energeek.android.sdk.internal.crypto.OutgoingRoomKeyRequest
import com.energeek.android.sdk.internal.crypto.crosssigning.DeviceTrustLevel
import com.energeek.android.sdk.internal.crypto.model.CryptoDeviceInfo
import com.energeek.android.sdk.internal.crypto.model.ImportRoomKeysResult
import com.energeek.android.sdk.internal.crypto.model.MXDeviceInfo
import com.energeek.android.sdk.internal.crypto.model.MXEncryptEventContentResult
import com.energeek.android.sdk.internal.crypto.model.MXUsersDevicesMap
import com.energeek.android.sdk.internal.crypto.model.event.RoomKeyWithHeldContent
import com.energeek.android.sdk.internal.crypto.model.rest.DeviceInfo
import com.energeek.android.sdk.internal.crypto.model.rest.DevicesListResponse
import com.energeek.android.sdk.internal.crypto.model.rest.RoomKeyRequestBody
import kotlin.jvm.Throws

interface CryptoService {

    fun verificationService(): VerificationService

    fun crossSigningService(): CrossSigningService

    fun keysBackupService(): KeysBackupService

    fun setDeviceName(deviceId: String, deviceName: String, callback: MatrixCallback<Unit>)

    fun deleteDevice(deviceId: String, userInteractiveAuthInterceptor: UserInteractiveAuthInterceptor, callback: MatrixCallback<Unit>)

    fun getCryptoVersion(context: Context, longFormat: Boolean): String

    fun isCryptoEnabled(): Boolean

    fun isRoomBlacklistUnverifiedDevices(roomId: String?): Boolean

    fun setWarnOnUnknownDevices(warn: Boolean)

    fun setDeviceVerification(trustLevel: DeviceTrustLevel, userId: String, deviceId: String)

    fun getUserDevices(userId: String): MutableList<CryptoDeviceInfo>

    fun setDevicesKnown(devices: List<MXDeviceInfo>, callback: MatrixCallback<Unit>?)

    fun deviceWithIdentityKey(senderKey: String, algorithm: String): CryptoDeviceInfo?

    fun getMyDevice(): CryptoDeviceInfo

    fun getGlobalBlacklistUnverifiedDevices(): Boolean

    fun setGlobalBlacklistUnverifiedDevices(block: Boolean)

    fun setRoomUnBlacklistUnverifiedDevices(roomId: String)

    fun getDeviceTrackingStatus(userId: String): Int

    fun importRoomKeys(roomKeysAsArray: ByteArray, password: String, progressListener: ProgressListener?, callback: MatrixCallback<ImportRoomKeysResult>)

    fun exportRoomKeys(password: String, callback: MatrixCallback<ByteArray>)

    fun setRoomBlacklistUnverifiedDevices(roomId: String)

    fun getDeviceInfo(userId: String, deviceId: String?): CryptoDeviceInfo?

    fun requestRoomKeyForEvent(event: Event)

    fun reRequestRoomKeyForEvent(event: Event)

    fun cancelRoomKeyRequest(requestBody: RoomKeyRequestBody)

    fun addRoomKeysRequestListener(listener: GossipingRequestListener)

    fun removeRoomKeysRequestListener(listener: GossipingRequestListener)

    fun fetchDevicesList(callback: MatrixCallback<DevicesListResponse>)

    fun getMyDevicesInfo(): List<DeviceInfo>

    fun getLiveMyDevicesInfo(): LiveData<List<DeviceInfo>>

    fun getDeviceInfo(deviceId: String, callback: MatrixCallback<DeviceInfo>)

    fun inboundGroupSessionsCount(onlyBackedUp: Boolean): Int

    fun isRoomEncrypted(roomId: String): Boolean

    fun encryptEventContent(eventContent: Content,
                            eventType: String,
                            roomId: String,
                            callback: MatrixCallback<MXEncryptEventContentResult>)

    fun discardOutboundSession(roomId: String)

    @Throws(MXCryptoError::class)
    fun decryptEvent(event: Event, timeline: String): MXEventDecryptionResult

    fun decryptEventAsync(event: Event, timeline: String, callback: MatrixCallback<MXEventDecryptionResult>)

    fun getEncryptionAlgorithm(roomId: String): String?

    fun shouldEncryptForInvitedMembers(roomId: String): Boolean

    fun downloadKeys(userIds: List<String>, forceDownload: Boolean, callback: MatrixCallback<MXUsersDevicesMap<CryptoDeviceInfo>>)

    fun getCryptoDeviceInfo(userId: String): List<CryptoDeviceInfo>

    fun getLiveCryptoDeviceInfo(): LiveData<List<CryptoDeviceInfo>>

    fun getLiveCryptoDeviceInfo(userId: String): LiveData<List<CryptoDeviceInfo>>

    fun getLiveCryptoDeviceInfo(userIds: List<String>): LiveData<List<CryptoDeviceInfo>>

    fun addNewSessionListener(newSessionListener: NewSessionListener)

    fun removeSessionListener(listener: NewSessionListener)

    fun getOutgoingRoomKeyRequests(): List<OutgoingRoomKeyRequest>
    fun getOutgoingRoomKeyRequestsPaged(): LiveData<PagedList<OutgoingRoomKeyRequest>>

    fun getIncomingRoomKeyRequests(): List<IncomingRoomKeyRequest>
    fun getIncomingRoomKeyRequestsPaged(): LiveData<PagedList<IncomingRoomKeyRequest>>

    fun getGossipingEventsTrail(): LiveData<PagedList<Event>>
    fun getGossipingEvents(): List<Event>

    // For testing shared session
    fun getSharedWithInfo(roomId: String?, sessionId: String): MXUsersDevicesMap<Int>
    fun getWithHeldMegolmSession(roomId: String, sessionId: String): RoomKeyWithHeldContent?

    fun logDbUsageInfo()

    /**
     * Perform any background tasks that can be done before a message is ready to
     * send, in order to speed up sending of the message.
     */
    fun prepareToEncrypt(roomId: String, callback: MatrixCallback<Unit>)
}
