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

package com.energeek.android.sdk.internal.session.room.membership

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import com.zhuinden.monarchy.Monarchy
import com.energeek.android.sdk.api.session.identity.ThreePid
import com.energeek.android.sdk.api.session.room.members.MembershipService
import com.energeek.android.sdk.api.session.room.members.RoomMemberQueryParams
import com.energeek.android.sdk.api.session.room.model.Membership
import com.energeek.android.sdk.api.session.room.model.RoomMemberSummary
import com.energeek.android.sdk.internal.database.mapper.asDomain
import com.energeek.android.sdk.internal.database.model.RoomMemberSummaryEntity
import com.energeek.android.sdk.internal.database.model.RoomMemberSummaryEntityFields
import com.energeek.android.sdk.internal.di.SessionDatabase
import com.energeek.android.sdk.internal.di.UserId
import com.energeek.android.sdk.internal.query.process
import com.energeek.android.sdk.internal.session.room.membership.admin.MembershipAdminTask
import com.energeek.android.sdk.internal.session.room.membership.joining.InviteTask
import com.energeek.android.sdk.internal.session.room.membership.joining.JoinRoomTask
import com.energeek.android.sdk.internal.session.room.membership.leaving.LeaveRoomTask
import com.energeek.android.sdk.internal.session.room.membership.threepid.InviteThreePidTask
import com.energeek.android.sdk.internal.util.fetchCopied
import io.realm.Realm
import io.realm.RealmQuery

internal class DefaultMembershipService @AssistedInject constructor(
        @Assisted private val roomId: String,
        @SessionDatabase private val monarchy: Monarchy,
        private val loadRoomMembersTask: LoadRoomMembersTask,
        private val inviteTask: InviteTask,
        private val inviteThreePidTask: InviteThreePidTask,
        private val joinTask: JoinRoomTask,
        private val leaveRoomTask: LeaveRoomTask,
        private val membershipAdminTask: MembershipAdminTask,
        @UserId
        private val userId: String
) : MembershipService {

    @AssistedFactory
    interface Factory {
        fun create(roomId: String): DefaultMembershipService
    }

    override suspend fun loadRoomMembersIfNeeded() {
        val params = LoadRoomMembersTask.Params(roomId, Membership.LEAVE)
        loadRoomMembersTask.execute(params)
    }

    override fun getRoomMember(userId: String): RoomMemberSummary? {
        val roomMemberEntity = monarchy.fetchCopied {
            RoomMemberHelper(it, roomId).getLastRoomMember(userId)
        }
        return roomMemberEntity?.asDomain()
    }

    override fun getRoomMembers(queryParams: RoomMemberQueryParams): List<RoomMemberSummary> {
        return monarchy.fetchAllMappedSync(
                {
                    roomMembersQuery(it, queryParams)
                },
                {
                    it.asDomain()
                }
        )
    }

    override fun getRoomMembersLive(queryParams: RoomMemberQueryParams): LiveData<List<RoomMemberSummary>> {
        return monarchy.findAllMappedWithChanges(
                {
                    roomMembersQuery(it, queryParams)
                },
                {
                    it.asDomain()
                }
        )
    }

    private fun roomMembersQuery(realm: Realm, queryParams: RoomMemberQueryParams): RealmQuery<RoomMemberSummaryEntity> {
        return RoomMemberHelper(realm, roomId).queryRoomMembersEvent()
                .process(RoomMemberSummaryEntityFields.USER_ID, queryParams.userId)
                .process(RoomMemberSummaryEntityFields.MEMBERSHIP_STR, queryParams.memberships)
                .process(RoomMemberSummaryEntityFields.DISPLAY_NAME, queryParams.displayName)
                .apply {
                    if (queryParams.excludeSelf) {
                        notEqualTo(RoomMemberSummaryEntityFields.USER_ID, userId)
                    }
                }
    }

    override fun getNumberOfJoinedMembers(): Int {
        return Realm.getInstance(monarchy.realmConfiguration).use {
            RoomMemberHelper(it, roomId).getNumberOfJoinedMembers()
        }
    }

    override suspend fun ban(userId: String, reason: String?) {
        val params = MembershipAdminTask.Params(MembershipAdminTask.Type.BAN, roomId, userId, reason)
        membershipAdminTask.execute(params)
    }

    override suspend fun unban(userId: String, reason: String?) {
        val params = MembershipAdminTask.Params(MembershipAdminTask.Type.UNBAN, roomId, userId, reason)
        membershipAdminTask.execute(params)
    }

    override suspend fun kick(userId: String, reason: String?) {
        val params = MembershipAdminTask.Params(MembershipAdminTask.Type.KICK, roomId, userId, reason)
        membershipAdminTask.execute(params)
    }

    override suspend fun invite(userId: String, reason: String?) {
        val params = InviteTask.Params(roomId, userId, reason)
        inviteTask.execute(params)
    }

    override suspend fun invite3pid(threePid: ThreePid) {
        val params = InviteThreePidTask.Params(roomId, threePid)
        return inviteThreePidTask.execute(params)
    }

    override suspend fun join(reason: String?, viaServers: List<String>) {
        val params = JoinRoomTask.Params(roomId, reason, viaServers)
        joinTask.execute(params)
    }

    override suspend fun leave(reason: String?) {
        val params = LeaveRoomTask.Params(roomId, reason)
        leaveRoomTask.execute(params)
    }
}
