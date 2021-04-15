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

package com.energeek.android.sdk.internal.crypto.keysbackup.tasks

import com.energeek.android.sdk.internal.crypto.keysbackup.api.RoomKeysApi
import com.energeek.android.sdk.internal.network.GlobalErrorReceiver
import com.energeek.android.sdk.internal.network.executeRequest
import com.energeek.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface DeleteRoomSessionDataTask : Task<DeleteRoomSessionDataTask.Params, Unit> {
    data class Params(
            val roomId: String,
            val sessionId: String,
            val version: String
    )
}

internal class DefaultDeleteRoomSessionDataTask @Inject constructor(
        private val roomKeysApi: RoomKeysApi,
        private val globalErrorReceiver: GlobalErrorReceiver
) : DeleteRoomSessionDataTask {

    override suspend fun execute(params: DeleteRoomSessionDataTask.Params) {
        return executeRequest(globalErrorReceiver) {
            roomKeysApi.deleteRoomSessionData(
                    params.roomId,
                    params.sessionId,
                    params.version)
        }
    }
}
