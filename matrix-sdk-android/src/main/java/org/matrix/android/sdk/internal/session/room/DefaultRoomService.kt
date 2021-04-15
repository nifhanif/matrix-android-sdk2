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

package com.energeek.android.sdk.internal.session.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.zhuinden.monarchy.Monarchy
import com.energeek.android.sdk.api.MatrixCallback
import com.energeek.android.sdk.api.session.events.model.Event
import com.energeek.android.sdk.api.session.room.Room
import com.energeek.android.sdk.api.session.room.RoomService
import com.energeek.android.sdk.api.session.room.RoomSummaryQueryParams
import com.energeek.android.sdk.api.session.room.UpdatableFilterLivePageResult
import com.energeek.android.sdk.api.session.room.members.ChangeMembershipState
import com.energeek.android.sdk.api.session.room.model.RoomMemberSummary
import com.energeek.android.sdk.api.session.room.model.RoomSummary
import com.energeek.android.sdk.api.session.room.model.create.CreateRoomParams
import com.energeek.android.sdk.api.session.room.peeking.PeekResult
import com.energeek.android.sdk.api.session.room.summary.RoomAggregateNotificationCount
import com.energeek.android.sdk.api.util.Cancelable
import com.energeek.android.sdk.api.util.Optional
import com.energeek.android.sdk.api.util.toOptional
import com.energeek.android.sdk.internal.database.mapper.asDomain
import com.energeek.android.sdk.internal.database.model.RoomMemberSummaryEntityFields
import com.energeek.android.sdk.internal.di.SessionDatabase
import com.energeek.android.sdk.internal.session.room.alias.DeleteRoomAliasTask
import com.energeek.android.sdk.internal.session.room.alias.GetRoomIdByAliasTask
import com.energeek.android.sdk.internal.session.room.alias.RoomAliasDescription
import com.energeek.android.sdk.internal.session.room.create.CreateRoomTask
import com.energeek.android.sdk.internal.session.room.membership.RoomChangeMembershipStateDataSource
import com.energeek.android.sdk.internal.session.room.membership.RoomMemberHelper
import com.energeek.android.sdk.internal.session.room.membership.joining.JoinRoomTask
import com.energeek.android.sdk.internal.session.room.peeking.PeekRoomTask
import com.energeek.android.sdk.internal.session.room.peeking.ResolveRoomStateTask
import com.energeek.android.sdk.internal.session.room.read.MarkAllRoomsReadTask
import com.energeek.android.sdk.internal.session.room.summary.RoomSummaryDataSource
import com.energeek.android.sdk.internal.session.user.accountdata.UpdateBreadcrumbsTask
import com.energeek.android.sdk.internal.task.TaskExecutor
import com.energeek.android.sdk.internal.task.configureWith
import com.energeek.android.sdk.internal.util.fetchCopied
import javax.inject.Inject

internal class DefaultRoomService @Inject constructor(
        @SessionDatabase private val monarchy: Monarchy,
        private val createRoomTask: CreateRoomTask,
        private val joinRoomTask: JoinRoomTask,
        private val markAllRoomsReadTask: MarkAllRoomsReadTask,
        private val updateBreadcrumbsTask: UpdateBreadcrumbsTask,
        private val roomIdByAliasTask: GetRoomIdByAliasTask,
        private val deleteRoomAliasTask: DeleteRoomAliasTask,
        private val resolveRoomStateTask: ResolveRoomStateTask,
        private val peekRoomTask: PeekRoomTask,
        private val roomGetter: RoomGetter,
        private val roomSummaryDataSource: RoomSummaryDataSource,
        private val roomChangeMembershipStateDataSource: RoomChangeMembershipStateDataSource,
        private val taskExecutor: TaskExecutor
) : RoomService {

    override fun createRoom(createRoomParams: CreateRoomParams, callback: MatrixCallback<String>): Cancelable {
        return createRoomTask
                .configureWith(createRoomParams) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun getRoom(roomId: String): Room? {
        return roomGetter.getRoom(roomId)
    }

    override fun getExistingDirectRoomWithUser(otherUserId: String): String? {
        return roomGetter.getDirectRoomWith(otherUserId)
    }

    override fun getRoomSummary(roomIdOrAlias: String): RoomSummary? {
        return roomSummaryDataSource.getRoomSummary(roomIdOrAlias)
    }

    override fun getRoomSummaries(queryParams: RoomSummaryQueryParams): List<RoomSummary> {
        return roomSummaryDataSource.getRoomSummaries(queryParams)
    }

    override fun getRoomSummariesLive(queryParams: RoomSummaryQueryParams): LiveData<List<RoomSummary>> {
        return roomSummaryDataSource.getRoomSummariesLive(queryParams)
    }

    override fun getPagedRoomSummariesLive(queryParams: RoomSummaryQueryParams, pagedListConfig: PagedList.Config)
            : LiveData<PagedList<RoomSummary>> {
        return roomSummaryDataSource.getSortedPagedRoomSummariesLive(queryParams, pagedListConfig)
    }

    override fun getFilteredPagedRoomSummariesLive(queryParams: RoomSummaryQueryParams, pagedListConfig: PagedList.Config)
            : UpdatableFilterLivePageResult {
        return roomSummaryDataSource.getFilteredPagedRoomSummariesLive(queryParams, pagedListConfig)
    }

    override fun getNotificationCountForRooms(queryParams: RoomSummaryQueryParams): RoomAggregateNotificationCount {
        return roomSummaryDataSource.getNotificationCountForRooms(queryParams)
    }

    override fun getBreadcrumbs(queryParams: RoomSummaryQueryParams): List<RoomSummary> {
        return roomSummaryDataSource.getBreadcrumbs(queryParams)
    }

    override fun getBreadcrumbsLive(queryParams: RoomSummaryQueryParams): LiveData<List<RoomSummary>> {
        return roomSummaryDataSource.getBreadcrumbsLive(queryParams)
    }

    override fun onRoomDisplayed(roomId: String): Cancelable {
        return updateBreadcrumbsTask
                .configureWith(UpdateBreadcrumbsTask.Params(roomId))
                .executeBy(taskExecutor)
    }

    override fun joinRoom(roomIdOrAlias: String, reason: String?, viaServers: List<String>, callback: MatrixCallback<Unit>): Cancelable {
        return joinRoomTask
                .configureWith(JoinRoomTask.Params(roomIdOrAlias, reason, viaServers)) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun markAllAsRead(roomIds: List<String>, callback: MatrixCallback<Unit>): Cancelable {
        return markAllRoomsReadTask
                .configureWith(MarkAllRoomsReadTask.Params(roomIds)) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun getRoomIdByAlias(roomAlias: String, searchOnServer: Boolean, callback: MatrixCallback<Optional<RoomAliasDescription>>): Cancelable {
        return roomIdByAliasTask
                .configureWith(GetRoomIdByAliasTask.Params(roomAlias, searchOnServer)) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override suspend fun deleteRoomAlias(roomAlias: String) {
        deleteRoomAliasTask.execute(DeleteRoomAliasTask.Params(roomAlias))
    }

    override fun getChangeMembershipsLive(): LiveData<Map<String, ChangeMembershipState>> {
        return roomChangeMembershipStateDataSource.getLiveStates()
    }

    override fun getRoomMember(userId: String, roomId: String): RoomMemberSummary? {
        val roomMemberEntity = monarchy.fetchCopied {
            RoomMemberHelper(it, roomId).getLastRoomMember(userId)
        }
        return roomMemberEntity?.asDomain()
    }

    override fun getRoomMemberLive(userId: String, roomId: String): LiveData<Optional<RoomMemberSummary>> {
        val liveData = monarchy.findAllMappedWithChanges(
                { realm ->
                    RoomMemberHelper(realm, roomId).queryRoomMembersEvent()
                            .equalTo(RoomMemberSummaryEntityFields.USER_ID, userId)
                },
                { it.asDomain() }
        )
        return Transformations.map(liveData) { results ->
            results.firstOrNull().toOptional()
        }
    }

    override fun getRoomState(roomId: String, callback: MatrixCallback<List<Event>>) {
        resolveRoomStateTask
                .configureWith(ResolveRoomStateTask.Params(roomId)) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun peekRoom(roomIdOrAlias: String, callback: MatrixCallback<PeekResult>) {
        peekRoomTask
                .configureWith(PeekRoomTask.Params(roomIdOrAlias)) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }
}
