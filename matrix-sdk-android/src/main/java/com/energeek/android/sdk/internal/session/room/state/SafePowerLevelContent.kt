/*
 * Copyright (c) 2021 The Matrix.org Foundation C.I.C.
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

package com.nusaraya.android.sdk.internal.session.room.state

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.nusaraya.android.sdk.api.session.events.model.toContent
import com.nusaraya.android.sdk.api.session.events.model.toModel
import com.nusaraya.android.sdk.api.session.room.model.PowerLevelsContent
import com.nusaraya.android.sdk.api.session.room.powerlevels.Role
import com.nusaraya.android.sdk.api.util.JsonDict

@JsonClass(generateAdapter = true)
internal data class SerializablePowerLevelsContent(
        @Json(name = "ban") val ban: Int = Role.Moderator.value,
        @Json(name = "kick") val kick: Int = Role.Moderator.value,
        @Json(name = "invite") val invite: Int = Role.Moderator.value,
        @Json(name = "redact") val redact: Int = Role.Moderator.value,
        @Json(name = "events_default") val eventsDefault: Int = Role.Default.value,
        @Json(name = "events") val events: Map<String, Int> = emptyMap(),
        @Json(name = "users_default") val usersDefault: Int = Role.Default.value,
        @Json(name = "users") val users: Map<String, Int> = emptyMap(),
        @Json(name = "state_default") val stateDefault: Int = Role.Moderator.value,
        // `Int` is the diff here (instead of `Any`)
        @Json(name = "notifications") val notifications: Map<String, Int> = emptyMap()
)

internal fun JsonDict.toSafePowerLevelsContentDict(): JsonDict {
    return toModel<PowerLevelsContent>()
            ?.let { content ->
                SerializablePowerLevelsContent(
                        ban = content.ban,
                        kick = content.kick,
                        invite = content.invite,
                        redact = content.redact,
                        eventsDefault = content.eventsDefault,
                        events = content.events,
                        usersDefault = content.usersDefault,
                        users = content.users,
                        stateDefault = content.stateDefault,
                        notifications = content.notifications.mapValues { content.notificationLevel(it.key)  }
                )
            }
            ?.toContent()
            ?: emptyMap()
}
