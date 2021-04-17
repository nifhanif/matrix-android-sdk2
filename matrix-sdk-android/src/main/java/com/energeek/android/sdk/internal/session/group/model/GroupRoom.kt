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

package com.nusaraya.android.sdk.internal.session.group.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GroupRoom(

        @Json(name = "aliases") val aliases: List<String> = emptyList(),
        @Json(name = "canonical_alias") val canonicalAlias: String? = null,
        @Json(name = "name") val name: String? = null,
        @Json(name = "num_joined_members") val numJoinedMembers: Int = 0,
        @Json(name = "room_id") val roomId: String,
        @Json(name = "topic") val topic: String? = null,
        @Json(name = "world_readable") val worldReadable: Boolean = false,
        @Json(name = "guest_can_join") val guestCanJoin: Boolean = false,
        @Json(name = "avatar_url") val avatarUrl: String? = null

)
