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

package com.energeek.android.sdk.internal.session.identity

import com.energeek.android.sdk.api.session.identity.IdentityServiceError
import com.energeek.android.sdk.internal.di.AuthenticatedIdentity
import com.energeek.android.sdk.internal.network.executeRequest
import com.energeek.android.sdk.internal.network.token.AccessTokenProvider
import com.energeek.android.sdk.internal.task.Task
import timber.log.Timber
import javax.inject.Inject

internal interface IdentityDisconnectTask : Task<Unit, Unit>

internal class DefaultIdentityDisconnectTask @Inject constructor(
        private val identityApiProvider: IdentityApiProvider,
        @AuthenticatedIdentity
        private val accessTokenProvider: AccessTokenProvider
) : IdentityDisconnectTask {

    override suspend fun execute(params: Unit) {
        val identityAPI = identityApiProvider.identityApi ?: throw IdentityServiceError.NoIdentityServerConfigured

        // Ensure we have a token.
        // We can have an identity server configured, but no token yet.
        if (accessTokenProvider.getToken() == null) {
            Timber.d("No token to disconnect identity server.")
            return
        }

        executeRequest(null) {
            identityAPI.logout()
        }
    }
}
