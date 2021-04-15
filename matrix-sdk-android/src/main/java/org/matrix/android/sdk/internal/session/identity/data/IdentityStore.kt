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

package com.energeek.android.sdk.internal.session.identity.data

import com.energeek.android.sdk.api.session.identity.ThreePid
import com.energeek.android.sdk.internal.session.identity.model.IdentityHashDetailResponse

internal interface IdentityStore {

    fun getIdentityData(): IdentityData?

    fun setUrl(url: String?)

    fun setToken(token: String?)

    fun setUserConsent(consent: Boolean)

    fun setHashDetails(hashDetailResponse: IdentityHashDetailResponse)

    /**
     * Store details about a current binding
     */
    fun storePendingBinding(threePid: ThreePid, data: IdentityPendingBinding)

    fun getPendingBinding(threePid: ThreePid): IdentityPendingBinding?

    fun deletePendingBinding(threePid: ThreePid)
}

internal fun IdentityStore.getIdentityServerUrlWithoutProtocol(): String? {
    return getIdentityData()?.identityServerUrl?.substringAfter("://")
}
