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

import io.realm.Realm
import com.nusaraya.android.sdk.api.extensions.orFalse
import com.nusaraya.android.sdk.api.session.events.model.EventType
import com.nusaraya.android.sdk.api.session.events.model.toModel
import com.nusaraya.android.sdk.api.session.room.model.RoomAvatarContent
import com.nusaraya.android.sdk.internal.database.mapper.asDomain
import com.nusaraya.android.sdk.internal.database.model.CurrentStateEventEntity
import com.nusaraya.android.sdk.internal.database.model.RoomMemberSummaryEntityFields
import com.nusaraya.android.sdk.internal.database.model.RoomSummaryEntity
import com.nusaraya.android.sdk.internal.database.query.getOrNull
import com.nusaraya.android.sdk.internal.database.query.where
import com.nusaraya.android.sdk.internal.di.UserId
import com.nusaraya.android.sdk.internal.session.room.membership.RoomMemberHelper
import javax.inject.Inject

internal class RoomAvatarResolver @Inject constructor(@UserId private val userId: String) {

    /**
     * Compute the room avatar url
     * @param realm: the current instance of realm
     * @param roomId the roomId of the room to resolve avatar
     * @return the room avatar url, can be a fallback to a room member avatar or null
     */
    fun resolve(realm: Realm, roomId: String): String? {
        val roomName = CurrentStateEventEntity.getOrNull(realm, roomId, type = EventType.STATE_ROOM_AVATAR, stateKey = "")
                ?.root
                ?.asDomain()
                ?.content
                ?.toModel<RoomAvatarContent>()
                ?.avatarUrl
        if (!roomName.isNullOrEmpty()) {
            return roomName
        }
        val roomMembers = RoomMemberHelper(realm, roomId)
        val members = roomMembers.queryActiveRoomMembersEvent().findAll()
        // detect if it is a room with no more than 2 members (i.e. an alone or a 1:1 chat)
        val isDirectRoom = RoomSummaryEntity.where(realm, roomId).findFirst()?.isDirect.orFalse()

        if (isDirectRoom) {
            if (members.size == 1) {
                // Use avatar of a left user
                val firstLeftAvatarUrl = roomMembers.queryLeftRoomMembersEvent()
                        .findAll()
                        .firstOrNull { !it.avatarUrl.isNullOrEmpty() }
                        ?.avatarUrl

                return firstLeftAvatarUrl ?: members.firstOrNull()?.avatarUrl
            } else if (members.size == 2) {
                val firstOtherMember = members.where().notEqualTo(RoomMemberSummaryEntityFields.USER_ID, userId).findFirst()
                return firstOtherMember?.avatarUrl
            }
        }

        return null
    }
}
