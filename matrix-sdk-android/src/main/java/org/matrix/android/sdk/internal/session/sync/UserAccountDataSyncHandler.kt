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

package com.energeek.android.sdk.internal.session.sync

import com.zhuinden.monarchy.Monarchy
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where
import com.energeek.android.sdk.api.pushrules.RuleScope
import com.energeek.android.sdk.api.pushrules.RuleSetKey
import com.energeek.android.sdk.api.pushrules.rest.GetPushRulesResponse
import com.energeek.android.sdk.api.session.accountdata.UserAccountDataEvent
import com.energeek.android.sdk.api.session.accountdata.UserAccountDataTypes
import com.energeek.android.sdk.api.session.events.model.Content
import com.energeek.android.sdk.api.session.events.model.toModel
import com.energeek.android.sdk.api.session.room.model.RoomMemberContent
import com.energeek.android.sdk.api.session.room.model.RoomSummary
import com.energeek.android.sdk.internal.database.mapper.ContentMapper
import com.energeek.android.sdk.internal.database.mapper.PushRulesMapper
import com.energeek.android.sdk.internal.database.mapper.asDomain
import com.energeek.android.sdk.internal.database.model.BreadcrumbsEntity
import com.energeek.android.sdk.internal.database.model.IgnoredUserEntity
import com.energeek.android.sdk.internal.database.model.PushRulesEntity
import com.energeek.android.sdk.internal.database.model.RoomSummaryEntity
import com.energeek.android.sdk.internal.database.model.RoomSummaryEntityFields
import com.energeek.android.sdk.internal.database.model.UserAccountDataEntity
import com.energeek.android.sdk.internal.database.model.UserAccountDataEntityFields
import com.energeek.android.sdk.internal.database.model.deleteOnCascade
import com.energeek.android.sdk.internal.database.query.getDirectRooms
import com.energeek.android.sdk.internal.database.query.getOrCreate
import com.energeek.android.sdk.internal.database.query.where
import com.energeek.android.sdk.internal.di.SessionDatabase
import com.energeek.android.sdk.internal.di.UserId
import com.energeek.android.sdk.internal.session.room.RoomAvatarResolver
import com.energeek.android.sdk.internal.session.room.membership.RoomDisplayNameResolver
import com.energeek.android.sdk.internal.session.room.membership.RoomMemberHelper
import com.energeek.android.sdk.internal.session.sync.model.InvitedRoomSync
import com.energeek.android.sdk.internal.session.sync.model.accountdata.BreadcrumbsContent
import com.energeek.android.sdk.internal.session.sync.model.accountdata.DirectMessagesContent
import com.energeek.android.sdk.internal.session.sync.model.accountdata.IgnoredUsersContent
import com.energeek.android.sdk.internal.session.sync.model.accountdata.UserAccountDataSync
import com.energeek.android.sdk.internal.session.user.accountdata.DirectChatsHelper
import com.energeek.android.sdk.internal.session.user.accountdata.UpdateUserAccountDataTask
import timber.log.Timber
import javax.inject.Inject

internal class UserAccountDataSyncHandler @Inject constructor(
        @SessionDatabase private val monarchy: Monarchy,
        @UserId private val userId: String,
        private val directChatsHelper: DirectChatsHelper,
        private val updateUserAccountDataTask: UpdateUserAccountDataTask,
        private val roomAvatarResolver: RoomAvatarResolver,
        private val roomDisplayNameResolver: RoomDisplayNameResolver
) {

    fun handle(realm: Realm, accountData: UserAccountDataSync?) {
        accountData?.list?.forEach { event ->
            // Generic handling, just save in base
            handleGenericAccountData(realm, event.type, event.content)
            when (event.type) {
                UserAccountDataTypes.TYPE_DIRECT_MESSAGES   -> handleDirectChatRooms(realm, event)
                UserAccountDataTypes.TYPE_PUSH_RULES        -> handlePushRules(realm, event)
                UserAccountDataTypes.TYPE_IGNORED_USER_LIST -> handleIgnoredUsers(realm, event)
                UserAccountDataTypes.TYPE_BREADCRUMBS       -> handleBreadcrumbs(realm, event)
            }
        }
    }

    // If we get some direct chat invites, we synchronize the user account data including those.
    suspend fun synchronizeWithServerIfNeeded(invites: Map<String, InvitedRoomSync>) {
        if (invites.isNullOrEmpty()) return
        val directChats = directChatsHelper.getLocalUserAccount()
        var hasUpdate = false
        monarchy.doWithRealm { realm ->
            invites.forEach { (roomId, _) ->
                val myUserStateEvent = RoomMemberHelper(realm, roomId).getLastStateEvent(userId)
                val inviterId = myUserStateEvent?.sender
                val myUserRoomMember: RoomMemberContent? = myUserStateEvent?.let { it.asDomain().content?.toModel() }
                val isDirect = myUserRoomMember?.isDirect
                if (inviterId != null && inviterId != userId && isDirect == true) {
                    directChats
                            .getOrPut(inviterId, { arrayListOf() })
                            .apply {
                                if (contains(roomId)) {
                                    Timber.v("Direct chats already include room $roomId with user $inviterId")
                                } else {
                                    add(roomId)
                                    hasUpdate = true
                                }
                            }
                }
            }
        }
        if (hasUpdate) {
            val updateUserAccountParams = UpdateUserAccountDataTask.DirectChatParams(
                    directMessages = directChats
            )
            updateUserAccountDataTask.execute(updateUserAccountParams)
        }
    }

    private fun handlePushRules(realm: Realm, event: UserAccountDataEvent) {
        val pushRules = event.content.toModel<GetPushRulesResponse>() ?: return
        realm.where(PushRulesEntity::class.java)
                .findAll()
                .forEach { it.deleteOnCascade() }

        // Save only global rules for the moment
        val globalRules = pushRules.global

        val content = PushRulesEntity(RuleScope.GLOBAL).apply { kind = RuleSetKey.CONTENT }
        globalRules.content?.forEach { rule ->
            content.pushRules.add(PushRulesMapper.map(rule))
        }
        realm.insertOrUpdate(content)

        val override = PushRulesEntity(RuleScope.GLOBAL).apply { kind = RuleSetKey.OVERRIDE }
        globalRules.override?.forEach { rule ->
            PushRulesMapper.map(rule).also {
                override.pushRules.add(it)
            }
        }
        realm.insertOrUpdate(override)

        val rooms = PushRulesEntity(RuleScope.GLOBAL).apply { kind = RuleSetKey.ROOM }
        globalRules.room?.forEach { rule ->
            rooms.pushRules.add(PushRulesMapper.map(rule))
        }
        realm.insertOrUpdate(rooms)

        val senders = PushRulesEntity(RuleScope.GLOBAL).apply { kind = RuleSetKey.SENDER }
        globalRules.sender?.forEach { rule ->
            senders.pushRules.add(PushRulesMapper.map(rule))
        }
        realm.insertOrUpdate(senders)

        val underrides = PushRulesEntity(RuleScope.GLOBAL).apply { kind = RuleSetKey.UNDERRIDE }
        globalRules.underride?.forEach { rule ->
            underrides.pushRules.add(PushRulesMapper.map(rule))
        }
        realm.insertOrUpdate(underrides)
    }

    private fun handleDirectChatRooms(realm: Realm, event: UserAccountDataEvent) {
        val content = event.content.toModel<DirectMessagesContent>() ?: return
        content.forEach { (userId, roomIds) ->
            roomIds.forEach { roomId ->
                val roomSummaryEntity = RoomSummaryEntity.where(realm, roomId).findFirst()
                if (roomSummaryEntity != null) {
                    roomSummaryEntity.isDirect = true
                    roomSummaryEntity.directUserId = userId
                    // Also update the avatar and displayname, there is a specific treatment for DMs
                    roomSummaryEntity.avatarUrl = roomAvatarResolver.resolve(realm, roomId)
                    roomSummaryEntity.displayName = roomDisplayNameResolver.resolve(realm, roomId)
                }
            }
        }

        // Handle previous direct rooms
        RoomSummaryEntity.getDirectRooms(realm, excludeRoomIds = content.values.flatten().toSet())
                .forEach {
                    it.isDirect = false
                    it.directUserId = null
                    // Also update the avatar and displayname, there was a specific treatment for DMs
                    it.avatarUrl = roomAvatarResolver.resolve(realm, it.roomId)
                    it.displayName = roomDisplayNameResolver.resolve(realm, it.roomId)
                }
    }

    private fun handleIgnoredUsers(realm: Realm, event: UserAccountDataEvent) {
        val userIds = event.content.toModel<IgnoredUsersContent>()?.ignoredUsers?.keys ?: return
        realm.where(IgnoredUserEntity::class.java)
                .findAll()
                .deleteAllFromRealm()
        // And save the new received list
        userIds.forEach { realm.createObject(IgnoredUserEntity::class.java).apply { userId = it } }
        // TODO If not initial sync, we should execute a init sync
    }

    private fun handleBreadcrumbs(realm: Realm, event: UserAccountDataEvent) {
        val recentRoomIds = event.content.toModel<BreadcrumbsContent>()?.recentRoomIds ?: return
        val entity = BreadcrumbsEntity.getOrCreate(realm)

        // And save the new received list
        entity.recentRoomIds = RealmList<String>().apply { addAll(recentRoomIds) }

        // Update the room summaries
        // Reset all the indexes...
        RoomSummaryEntity.where(realm)
                .greaterThan(RoomSummaryEntityFields.BREADCRUMBS_INDEX, RoomSummary.NOT_IN_BREADCRUMBS)
                .findAll()
                .forEach {
                    it.breadcrumbsIndex = RoomSummary.NOT_IN_BREADCRUMBS
                }

        // ...and apply new indexes
        recentRoomIds.forEachIndexed { index, roomId ->
            RoomSummaryEntity.where(realm, roomId)
                    .findFirst()
                    ?.breadcrumbsIndex = index
        }
    }

    fun handleGenericAccountData(realm: Realm, type: String, content: Content?) {
        val existing = realm.where<UserAccountDataEntity>()
                .equalTo(UserAccountDataEntityFields.TYPE, type)
                .findFirst()
        if (existing != null) {
            // Update current value
            existing.contentStr = ContentMapper.map(content)
        } else {
            realm.createObject(UserAccountDataEntity::class.java).let { accountDataEntity ->
                accountDataEntity.type = type
                accountDataEntity.contentStr = ContentMapper.map(content)
            }
        }
    }
}
