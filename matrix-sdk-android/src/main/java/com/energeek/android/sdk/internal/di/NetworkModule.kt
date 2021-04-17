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

package com.nusaraya.android.sdk.internal.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import com.nusaraya.android.sdk.BuildConfig
import com.nusaraya.android.sdk.api.MatrixConfiguration
import com.nusaraya.android.sdk.internal.network.TimeOutInterceptor
import com.nusaraya.android.sdk.internal.network.UserAgentInterceptor
import com.nusaraya.android.sdk.internal.network.interceptors.CurlLoggingInterceptor
import com.nusaraya.android.sdk.internal.network.interceptors.FormattedJsonHttpLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.nusaraya.android.sdk.internal.network.ApiInterceptor
import java.util.concurrent.TimeUnit

@Module
internal object NetworkModule {

    @Provides
    @JvmStatic
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = FormattedJsonHttpLogger()
        val interceptor = HttpLoggingInterceptor(logger)
        interceptor.level = BuildConfig.OKHTTP_LOGGING_LEVEL
        return interceptor
    }

    @Provides
    @JvmStatic
    fun providesStethoInterceptor(): StethoInterceptor {
        return StethoInterceptor()
    }

    @Provides
    @JvmStatic
    fun providesCurlLoggingInterceptor(): CurlLoggingInterceptor {
        return CurlLoggingInterceptor()
    }

    @MatrixScope
    @Provides
    @JvmStatic
    @Unauthenticated
    fun providesOkHttpClient(matrixConfiguration: MatrixConfiguration,
                             stethoInterceptor: StethoInterceptor,
                             timeoutInterceptor: TimeOutInterceptor,
                             userAgentInterceptor: UserAgentInterceptor,
                             httpLoggingInterceptor: HttpLoggingInterceptor,
                             curlLoggingInterceptor: CurlLoggingInterceptor,
                             apiInterceptor: ApiInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .apply {
                    if (BuildConfig.DEBUG) {
                        addNetworkInterceptor(stethoInterceptor)
                    }
                }
                .addInterceptor(timeoutInterceptor)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(apiInterceptor)
                .apply {
                    if (BuildConfig.LOG_PRIVATE_DATA) {
                        addInterceptor(curlLoggingInterceptor)
                    }
                    matrixConfiguration.proxy?.let {
                        proxy(it)
                    }
                }
                .build()
    }

    @Provides
    @JvmStatic
    fun providesMoshi(): Moshi {
        return MoshiProvider.providesMoshi()
    }
}
