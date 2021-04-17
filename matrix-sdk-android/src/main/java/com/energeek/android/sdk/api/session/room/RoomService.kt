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

package com.nusaraya.android.sdk.api.session.room

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.nusaraya.android.sdk.api.MatrixCallback
import com.nusaraya.android.sdk.api.session.events.model.Event
import com.nusaraya.android.sdk.api.session.room.members.ChangeMembershipState
import com.nusaraya.android.sdk.api.session.room.model.RoomMemberSummary
import com.nusaraya.android.sdk.api.session.room.model.RoomSummary
import com.nusaraya.android.sdk.api.session.room.model.create.CreateRoomParams
import com.nusaraya.android.sdk.api.session.room.peeking.PeekResult
import com.nusaraya.android.sdk.api.session.room.summary.RoomAggregateNotificationCount
import com.nusaraya.android.sdk.api.util.Cancelable
import com.nusaraya.android.sdk.api.util.Optional
import com.nusaraya.android.sdk.internal.session.room.alias.RoomAliasDescription

/**
 * This interface defines methods to get rooms. It's implemented at the session level.
 */
interface RoomService {

    /**
     * Create a room asynchronously
     */
    fun createRoom(createRoomParams: CreateRoomParams,
                   callback: MatrixCallback<String>): Cancelable

    /**
     * Create a direct room asynchronously. This is a facility method to create a direct room with the necessary parameters
     */
    fun createDirectRoom(otherUserId: String,
                         callback: MatrixCallback<String>): Cancelable {
        return createRoom(
                CreateRoomParams()
                        .apply {
                            invitedUserIds.add(otherUserId)
                            setDirectMessage()
                            enableEncryptionIfInvitedUsersSupportIt = true
                        },
                callback
        )
    }

    /**
     * Join a room by id
     * @param roomIdOrAlias the roomId or the room alias of the room to join
     * @param reason optional reason for joining the room
     * @param viaServers the servers to attempt to join the room through. One of the servers must be participating in the room.
     */
    fun joinRoom(roomIdOrAlias: String,
                 reason: String? = null,
                 viaServers: List<String> = emptyList(),
                 callback: MatrixCallback<Unit>): Cancelable

    /**
     * Get a room from a roomId
     * @param roomId the roomId to look for.
     * @return a room with roomId or null
     */
    fun getRoom(roomId: String): Room?

    /**
     * Get a roomSummary from a roomId or a room alias
     * @param roomIdOrAlias the roomId or the alias of a room to look for.
     * @return a matching room summary or null
     */
    fun getRoomSummary(roomIdOrAlias: String): RoomSummary?

    /**
     * Get a snapshot list of room summaries.
     * @return the immutable list of [RoomSummary]
     */
    fun getRoomSummaries(queryParams: RoomSummaryQueryParams): List<RoomSummary>

    /**
     * Get a live list of room summaries. This list is refreshed as soon as the data changes.
     * @return the [LiveData] of List[RoomSummary]
     */
    fun getRoomSummariesLive(queryParams: RoomSummaryQueryParams): LiveData<List<RoomSummary>>

    /**
     * Get a snapshot list of Breadcrumbs
     * @param queryParams parameters to query the room summaries. It can be use to keep only joined rooms, for instance.
     * @return the immutable list of [RoomSummary]
     */
    fun getBreadcrumbs(queryParams: RoomSummaryQueryParams): List<RoomSummary>

    /**
     * Get a live list of Breadcrumbs
     * @param queryParams parameters to query the room summaries. It can be use to keep only joined rooms, for instance.
     * @return the [LiveData] of [RoomSummary]
     */
    fun getBreadcrumbsLive(queryParams: RoomSummaryQueryParams): LiveData<List<RoomSummary>>

    /**
     * Inform the Matrix SDK that a room is displayed.
     * The SDK will update the breadcrumbs in the user account data
     */
    fun onRoomDisplayed(roomId: String): Cancelable

    /**
     * Mark all rooms as read
     */
    fun markAllAsRead(roomIds: List<String>,
                      callback: MatrixCallback<Unit>): Cancelable

    /**
     * Resolve a room alias to a room ID.
     */
    fun getRoomIdByAlias(roomAlias: String,
                         searchOnServer: Boolean,
                         callback: MatrixCallback<Optional<RoomAliasDescription>>): Cancelable

    /**
     * Delete a room alias
     */
    suspend fun deleteRoomAlias(roomAlias: String)

    /**
     * Return a live data of all local changes membership that happened since the session has been opened.
     * It allows you to track this in your client to known what is currently being processed by the SDK.
     * It won't know anything about change being done in other client.
     * Keys are roomId or roomAlias, depending of what you used as parameter for the join/leave action
     */
    fun getChangeMembershipsLive(): LiveData<Map<String, ChangeMembershipState>>

    /**
     * Return the roomId of an existing DM with the other user, or null if such room does not exist
     * A room is a DM if:
     *  - it is listed in the `m.direct` account data
     *  - the current user has joined the room
     *  - the other user is invited or has joined the room
     *  - it has exactly 2 members
     * Note:
     *  - the returning room can be encrypted or not
     *  - the power level of the users are not taken into account. Normally in a DM, the 2 members are admins of the room
     */
    fun getExistingDirectRoomWithUser(otherUserId: String): String?

    /**
     * Get a room member for the tuple {userId,roomId}
     * @param userId the userId to look for.
     * @param roomId the roomId to look for.
     * @return the room member or null
     */
    fun getRoomMember(userId: String, roomId: String): RoomMemberSummary?

    /**
     * Observe a live room member for the tuple {userId,roomId}
     * @param userId the userId to look for.
     * @param roomId the roomId to look for.
     * @return a LiveData of the optional found room member
     */
    fun getRoomMemberLive(userId: String, roomId: String): LiveData<Optional<RoomMemberSummary>>

    /**
     * Get some state events about a room
     */
    fun getRoomState(roomId: String, callback: MatrixCallback<List<Event>>)

    /**
     * Use this if you want to get information from a room that you are not yet in (or invited)
     * It might be possible to get some information on this room if it is public or if guest access is allowed
     * This call will try to gather some information on this room, but it could fail and get nothing more
     */
    fun peekRoom(roomIdOrAlias: String, callback: MatrixCallback<PeekResult>)

    /**
     * TODO Doc
     */
    fun getPagedRoomSummariesLive(queryParams: RoomSummaryQueryParams,
                                  pagedListConfig: PagedList.Config = defaultPagedListConfig): LiveData<PagedList<RoomSummary>>

    /**
     * TODO Doc
     */
    fun getFilteredPagedRoomSummariesLive(queryParams: RoomSummaryQueryParams,
                                          pagedListConfig: PagedList.Config = defaultPagedListConfig): UpdatableFilterLivePageResult

    /**
     * TODO Doc
     */
    fun getNotificationCountForRooms(queryParams: RoomSummaryQueryParams): RoomAggregateNotificationCount

    private val defaultPagedListConfig
        get() = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(20)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .build()
}
