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

package com.nusaraya.android.sdk.internal.session.room

import com.nusaraya.android.sdk.api.session.room.RoomDirectoryService
import com.nusaraya.android.sdk.api.session.room.model.RoomDirectoryVisibility
import com.nusaraya.android.sdk.api.session.room.model.roomdirectory.PublicRoomsParams
import com.nusaraya.android.sdk.api.session.room.model.roomdirectory.PublicRoomsResponse
import com.nusaraya.android.sdk.internal.session.room.directory.GetPublicRoomTask
import com.nusaraya.android.sdk.internal.session.room.directory.GetRoomDirectoryVisibilityTask
import com.nusaraya.android.sdk.internal.session.room.directory.SetRoomDirectoryVisibilityTask
import javax.inject.Inject

internal class DefaultRoomDirectoryService @Inject constructor(
        private val getPublicRoomTask: GetPublicRoomTask,
        private val getRoomDirectoryVisibilityTask: GetRoomDirectoryVisibilityTask,
        private val setRoomDirectoryVisibilityTask: SetRoomDirectoryVisibilityTask
) : RoomDirectoryService {

    override suspend fun getPublicRooms(server: String?,
                                        publicRoomsParams: PublicRoomsParams): PublicRoomsResponse {
        return getPublicRoomTask.execute(GetPublicRoomTask.Params(server, publicRoomsParams))
    }

    override suspend fun getRoomDirectoryVisibility(roomId: String): RoomDirectoryVisibility {
        return getRoomDirectoryVisibilityTask.execute(GetRoomDirectoryVisibilityTask.Params(roomId))
    }

    override suspend fun setRoomDirectoryVisibility(roomId: String, roomDirectoryVisibility: RoomDirectoryVisibility) {
        setRoomDirectoryVisibilityTask.execute(SetRoomDirectoryVisibilityTask.Params(roomId, roomDirectoryVisibility))
    }
}
