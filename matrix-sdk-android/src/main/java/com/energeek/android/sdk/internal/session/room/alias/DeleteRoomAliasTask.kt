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

package com.nusaraya.android.sdk.internal.session.room.alias

import com.nusaraya.android.sdk.internal.network.GlobalErrorReceiver
import com.nusaraya.android.sdk.internal.network.executeRequest
import com.nusaraya.android.sdk.internal.session.directory.DirectoryAPI
import com.nusaraya.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface DeleteRoomAliasTask : Task<DeleteRoomAliasTask.Params, Unit> {
    data class Params(
            val roomAlias: String
    )
}

internal class DefaultDeleteRoomAliasTask @Inject constructor(
        private val directoryAPI: DirectoryAPI,
        private val globalErrorReceiver: GlobalErrorReceiver
) : DeleteRoomAliasTask {

    override suspend fun execute(params: DeleteRoomAliasTask.Params) {
        executeRequest(globalErrorReceiver) {
            directoryAPI.deleteRoomAlias(
                    roomAlias = params.roomAlias
            )
        }
    }
}
