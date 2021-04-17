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

package com.nusaraya.android.sdk.internal.session.room

import androidx.lifecycle.LiveData
import com.nusaraya.android.sdk.api.session.crypto.CryptoService
import com.nusaraya.android.sdk.api.session.events.model.EventType
import com.nusaraya.android.sdk.api.session.room.Room
import com.nusaraya.android.sdk.api.session.room.alias.AliasService
import com.nusaraya.android.sdk.api.session.room.call.RoomCallService
import com.nusaraya.android.sdk.api.session.room.members.MembershipService
import com.nusaraya.android.sdk.api.session.room.model.RoomSummary
import com.nusaraya.android.sdk.api.session.room.model.relation.RelationService
import com.nusaraya.android.sdk.api.session.room.notification.RoomPushRuleService
import com.nusaraya.android.sdk.api.session.room.read.ReadService
import com.nusaraya.android.sdk.api.session.room.reporting.ReportingService
import com.nusaraya.android.sdk.api.session.room.send.DraftService
import com.nusaraya.android.sdk.api.session.room.send.SendService
import com.nusaraya.android.sdk.api.session.room.state.StateService
import com.nusaraya.android.sdk.api.session.room.tags.TagsService
import com.nusaraya.android.sdk.api.session.room.timeline.TimelineService
import com.nusaraya.android.sdk.api.session.room.typing.TypingService
import com.nusaraya.android.sdk.api.session.room.uploads.UploadsService
import com.nusaraya.android.sdk.api.session.search.SearchResult
import com.nusaraya.android.sdk.api.util.Optional
import com.nusaraya.android.sdk.internal.crypto.MXCRYPTO_ALGORITHM_MEGOLM
import com.nusaraya.android.sdk.internal.session.room.state.SendStateTask
import com.nusaraya.android.sdk.internal.session.room.summary.RoomSummaryDataSource
import com.nusaraya.android.sdk.internal.session.search.SearchTask
import com.nusaraya.android.sdk.internal.util.awaitCallback
import java.security.InvalidParameterException
import javax.inject.Inject

internal class DefaultRoom @Inject constructor(override val roomId: String,
                                               private val roomSummaryDataSource: RoomSummaryDataSource,
                                               private val timelineService: TimelineService,
                                               private val sendService: SendService,
                                               private val draftService: DraftService,
                                               private val stateService: StateService,
                                               private val uploadsService: UploadsService,
                                               private val reportingService: ReportingService,
                                               private val roomCallService: RoomCallService,
                                               private val readService: ReadService,
                                               private val typingService: TypingService,
                                               private val aliasService: AliasService,
                                               private val tagsService: TagsService,
                                               private val cryptoService: CryptoService,
                                               private val relationService: RelationService,
                                               private val roomMembersService: MembershipService,
                                               private val roomPushRuleService: RoomPushRuleService,
                                               private val sendStateTask: SendStateTask,
                                               private val searchTask: SearchTask) :
        Room,
        TimelineService by timelineService,
        SendService by sendService,
        DraftService by draftService,
        StateService by stateService,
        UploadsService by uploadsService,
        ReportingService by reportingService,
        RoomCallService by roomCallService,
        ReadService by readService,
        TypingService by typingService,
        AliasService by aliasService,
        TagsService by tagsService,
        RelationService by relationService,
        MembershipService by roomMembersService,
        RoomPushRuleService by roomPushRuleService {

    override fun getRoomSummaryLive(): LiveData<Optional<RoomSummary>> {
        return roomSummaryDataSource.getRoomSummaryLive(roomId)
    }

    override fun roomSummary(): RoomSummary? {
        return roomSummaryDataSource.getRoomSummary(roomId)
    }

    override fun isEncrypted(): Boolean {
        return cryptoService.isRoomEncrypted(roomId)
    }

    override fun encryptionAlgorithm(): String? {
        return cryptoService.getEncryptionAlgorithm(roomId)
    }

    override fun shouldEncryptForInvitedMembers(): Boolean {
        return cryptoService.shouldEncryptForInvitedMembers(roomId)
    }

    override suspend fun prepareToEncrypt() {
        awaitCallback<Unit> {
            cryptoService.prepareToEncrypt(roomId, it)
        }
    }

    override suspend fun enableEncryption(algorithm: String) {
        when {
            isEncrypted()                          -> {
                throw IllegalStateException("Encryption is already enabled for this room")
            }
            algorithm != MXCRYPTO_ALGORITHM_MEGOLM -> {
                throw InvalidParameterException("Only MXCRYPTO_ALGORITHM_MEGOLM algorithm is supported")
            }
            else                                   -> {
                val params = SendStateTask.Params(
                        roomId = roomId,
                        stateKey = null,
                        eventType = EventType.STATE_ROOM_ENCRYPTION,
                        body = mapOf(
                                "algorithm" to algorithm
                        ))

                sendStateTask.execute(params)
            }
        }
    }

    override suspend fun search(searchTerm: String,
                                nextBatch: String?,
                                orderByRecent: Boolean,
                                limit: Int,
                                beforeLimit: Int,
                                afterLimit: Int,
                                includeProfile: Boolean): SearchResult {
        return searchTask.execute(
                SearchTask.Params(
                        searchTerm = searchTerm,
                        roomId = roomId,
                        nextBatch = nextBatch,
                        orderByRecent = orderByRecent,
                        limit = limit,
                        beforeLimit = beforeLimit,
                        afterLimit = afterLimit,
                        includeProfile = includeProfile
                )
        )
    }
}
