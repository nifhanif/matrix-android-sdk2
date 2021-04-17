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

package com.nusaraya.android.sdk.internal.auth.login

import dagger.Lazy
import com.nusaraya.android.sdk.api.auth.data.HomeServerConnectionConfig
import com.nusaraya.android.sdk.api.failure.Failure
import com.nusaraya.android.sdk.api.session.Session
import com.nusaraya.android.sdk.internal.auth.AuthAPI
import com.nusaraya.android.sdk.internal.auth.SessionCreator
import com.nusaraya.android.sdk.internal.auth.data.PasswordLoginParams
import com.nusaraya.android.sdk.internal.di.Unauthenticated
import com.nusaraya.android.sdk.internal.network.RetrofitFactory
import com.nusaraya.android.sdk.internal.network.executeRequest
import com.nusaraya.android.sdk.internal.network.httpclient.addSocketFactory
import com.nusaraya.android.sdk.internal.network.ssl.UnrecognizedCertificateException
import com.nusaraya.android.sdk.internal.task.Task
import okhttp3.OkHttpClient
import javax.inject.Inject

internal interface DirectLoginTask : Task<DirectLoginTask.Params, Session> {
    data class Params(
            val homeServerConnectionConfig: HomeServerConnectionConfig,
            val userId: String,
            val password: String,
            val deviceName: String
    )
}

internal class DefaultDirectLoginTask @Inject constructor(
        @Unauthenticated
        private val okHttpClient: Lazy<OkHttpClient>,
        private val retrofitFactory: RetrofitFactory,
        private val sessionCreator: SessionCreator
) : DirectLoginTask {

    override suspend fun execute(params: DirectLoginTask.Params): Session {
        val client = buildClient(params.homeServerConnectionConfig)
        val homeServerUrl = params.homeServerConnectionConfig.homeServerUri.toString()

        val authAPI = retrofitFactory.create(client, homeServerUrl)
                .create(AuthAPI::class.java)

        val loginParams = PasswordLoginParams.userIdentifier(params.userId, params.password, params.deviceName)

        val credentials = try {
            executeRequest(null) {
                authAPI.login(loginParams)
            }
        } catch (throwable: Throwable) {
            throw when (throwable) {
                is UnrecognizedCertificateException -> Failure.UnrecognizedCertificateFailure(
                        homeServerUrl,
                        throwable.fingerprint
                )
                else                                -> throwable
            }
        }

        return sessionCreator.createSession(credentials, params.homeServerConnectionConfig)
    }

    private fun buildClient(homeServerConnectionConfig: HomeServerConnectionConfig): OkHttpClient {
        return okHttpClient.get()
                .newBuilder()
                .addSocketFactory(homeServerConnectionConfig)
                .build()
    }
}
