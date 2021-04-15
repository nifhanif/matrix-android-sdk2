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

package com.energeek.android.sdk.internal.session.room.membership.threepid

import com.energeek.android.sdk.api.session.identity.IdentityServiceError
import com.energeek.android.sdk.api.session.identity.ThreePid
import com.energeek.android.sdk.api.session.identity.toMedium
import com.energeek.android.sdk.internal.di.AuthenticatedIdentity
import com.energeek.android.sdk.internal.network.GlobalErrorReceiver
import com.energeek.android.sdk.internal.network.executeRequest
import com.energeek.android.sdk.internal.network.token.AccessTokenProvider
import com.energeek.android.sdk.internal.session.identity.EnsureIdentityTokenTask
import com.energeek.android.sdk.internal.session.identity.data.IdentityStore
import com.energeek.android.sdk.internal.session.identity.data.getIdentityServerUrlWithoutProtocol
import com.energeek.android.sdk.internal.session.room.RoomAPI
import com.energeek.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface InviteThreePidTask : Task<InviteThreePidTask.Params, Unit> {
    data class Params(
            val roomId: String,
            val threePid: ThreePid
    )
}

internal class DefaultInviteThreePidTask @Inject constructor(
        private val roomAPI: RoomAPI,
        private val globalErrorReceiver: GlobalErrorReceiver,
        private val identityStore: IdentityStore,
        private val ensureIdentityTokenTask: EnsureIdentityTokenTask,
        @AuthenticatedIdentity
        private val accessTokenProvider: AccessTokenProvider
) : InviteThreePidTask {

    override suspend fun execute(params: InviteThreePidTask.Params) {
        ensureIdentityTokenTask.execute(Unit)

        val identityServerUrlWithoutProtocol = identityStore.getIdentityServerUrlWithoutProtocol() ?: throw IdentityServiceError.NoIdentityServerConfigured
        val identityServerAccessToken = accessTokenProvider.getToken() ?: throw IdentityServiceError.NoIdentityServerConfigured

        return executeRequest(globalErrorReceiver) {
            val body = ThreePidInviteBody(
                    idServer = identityServerUrlWithoutProtocol,
                    idAccessToken = identityServerAccessToken,
                    medium = params.threePid.toMedium(),
                    address = params.threePid.value
            )
            roomAPI.invite3pid(params.roomId, body)
        }
    }
}
