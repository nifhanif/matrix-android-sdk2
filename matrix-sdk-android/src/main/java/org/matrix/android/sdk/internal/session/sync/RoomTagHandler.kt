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

import com.energeek.android.sdk.api.session.room.model.tag.RoomTagContent
import com.energeek.android.sdk.internal.database.model.RoomSummaryEntity
import com.energeek.android.sdk.internal.database.model.RoomTagEntity
import io.realm.Realm
import com.energeek.android.sdk.internal.database.query.getOrCreate
import javax.inject.Inject

internal class RoomTagHandler @Inject constructor() {

    fun handle(realm: Realm, roomId: String, content: RoomTagContent?) {
        if (content == null) {
            return
        }
        val tags = content.tags.entries.map { (tagName, params) ->
            RoomTagEntity(tagName, params["order"] as? Double)
            Pair(tagName, params["order"] as? Double)
        }
        RoomSummaryEntity.getOrCreate(realm, roomId).updateTags(tags)
    }
}
