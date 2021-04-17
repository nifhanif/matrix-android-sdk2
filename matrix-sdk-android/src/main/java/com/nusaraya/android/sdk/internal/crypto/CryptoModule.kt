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

package com.nusaraya.android.sdk.internal.crypto

import dagger.Binds
import dagger.Module
import dagger.Provides
import com.nusaraya.android.sdk.api.session.crypto.CryptoService
import com.nusaraya.android.sdk.api.session.crypto.crosssigning.CrossSigningService
import com.nusaraya.android.sdk.internal.crypto.api.CryptoApi
import com.nusaraya.android.sdk.internal.crypto.crosssigning.ComputeTrustTask
import com.nusaraya.android.sdk.internal.crypto.crosssigning.DefaultComputeTrustTask
import com.nusaraya.android.sdk.internal.crypto.crosssigning.DefaultCrossSigningService
import com.nusaraya.android.sdk.internal.crypto.keysbackup.api.RoomKeysApi
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.CreateKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultCreateKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultDeleteBackupTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultDeleteRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultDeleteRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultDeleteSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultGetKeysBackupLastVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultGetKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultGetRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultGetRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultGetSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultStoreRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultStoreRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultStoreSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DefaultUpdateKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DeleteBackupTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DeleteRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DeleteRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.DeleteSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.GetKeysBackupLastVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.GetKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.GetRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.GetRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.GetSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.StoreRoomSessionDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.StoreRoomSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.StoreSessionsDataTask
import com.nusaraya.android.sdk.internal.crypto.keysbackup.tasks.UpdateKeysBackupVersionTask
import com.nusaraya.android.sdk.internal.crypto.store.IMXCryptoStore
import com.nusaraya.android.sdk.internal.crypto.store.db.RealmCryptoStore
import com.nusaraya.android.sdk.internal.crypto.store.db.RealmCryptoStoreMigration
import com.nusaraya.android.sdk.internal.crypto.store.db.RealmCryptoStoreModule
import com.nusaraya.android.sdk.internal.crypto.tasks.ClaimOneTimeKeysForUsersDeviceTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultClaimOneTimeKeysForUsersDevice
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultDeleteDeviceTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultDownloadKeysForUsers
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultEncryptEventTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultGetDeviceInfoTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultGetDevicesTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultInitializeCrossSigningTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultSendEventTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultSendToDeviceTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultSendVerificationMessageTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultSetDeviceNameTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultUploadKeysTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultUploadSignaturesTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DefaultUploadSigningKeysTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DeleteDeviceTask
import com.nusaraya.android.sdk.internal.crypto.tasks.DownloadKeysForUsersTask
import com.nusaraya.android.sdk.internal.crypto.tasks.EncryptEventTask
import com.nusaraya.android.sdk.internal.crypto.tasks.GetDeviceInfoTask
import com.nusaraya.android.sdk.internal.crypto.tasks.GetDevicesTask
import com.nusaraya.android.sdk.internal.crypto.tasks.InitializeCrossSigningTask
import com.nusaraya.android.sdk.internal.crypto.tasks.SendEventTask
import com.nusaraya.android.sdk.internal.crypto.tasks.SendToDeviceTask
import com.nusaraya.android.sdk.internal.crypto.tasks.SendVerificationMessageTask
import com.nusaraya.android.sdk.internal.crypto.tasks.SetDeviceNameTask
import com.nusaraya.android.sdk.internal.crypto.tasks.UploadKeysTask
import com.nusaraya.android.sdk.internal.crypto.tasks.UploadSignaturesTask
import com.nusaraya.android.sdk.internal.crypto.tasks.UploadSigningKeysTask
import com.nusaraya.android.sdk.internal.database.RealmKeysUtils
import com.nusaraya.android.sdk.internal.di.CryptoDatabase
import com.nusaraya.android.sdk.internal.di.SessionFilesDirectory
import com.nusaraya.android.sdk.internal.di.UserMd5
import com.nusaraya.android.sdk.internal.session.SessionScope
import com.nusaraya.android.sdk.internal.session.cache.ClearCacheTask
import com.nusaraya.android.sdk.internal.session.cache.RealmClearCacheTask
import io.realm.RealmConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import java.io.File

@Module
internal abstract class CryptoModule {

    @Module
    companion object {
        internal fun getKeyAlias(userMd5: String) = "crypto_module_$userMd5"

        @JvmStatic
        @Provides
        @CryptoDatabase
        @SessionScope
        fun providesRealmConfiguration(@SessionFilesDirectory directory: File,
                                       @UserMd5 userMd5: String,
                                       realmCryptoStoreMigration: RealmCryptoStoreMigration,
                                       realmKeysUtils: RealmKeysUtils): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .directory(directory)
                    .apply {
                        realmKeysUtils.configureEncryption(this, getKeyAlias(userMd5))
                    }
                    .name("crypto_store.realm")
                    .modules(RealmCryptoStoreModule())
                    .allowWritesOnUiThread(true)
                    .schemaVersion(RealmCryptoStoreMigration.CRYPTO_STORE_SCHEMA_VERSION)
                    .migration(realmCryptoStoreMigration)
                    .build()
        }

        @JvmStatic
        @Provides
        @SessionScope
        fun providesCryptoCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob())
        }

        @JvmStatic
        @Provides
        @CryptoDatabase
        fun providesClearCacheTask(@CryptoDatabase
                                   realmConfiguration: RealmConfiguration): ClearCacheTask {
            return RealmClearCacheTask(realmConfiguration)
        }

        @JvmStatic
        @Provides
        @SessionScope
        fun providesCryptoAPI(retrofit: Retrofit): CryptoApi {
            return retrofit.create(CryptoApi::class.java)
        }

        @JvmStatic
        @Provides
        @SessionScope
        fun providesRoomKeysAPI(retrofit: Retrofit): RoomKeysApi {
            return retrofit.create(RoomKeysApi::class.java)
        }
    }

    @Binds
    abstract fun bindCryptoService(service: DefaultCryptoService): CryptoService

    @Binds
    abstract fun bindDeleteDeviceTask(task: DefaultDeleteDeviceTask): DeleteDeviceTask

    @Binds
    abstract fun bindGetDevicesTask(task: DefaultGetDevicesTask): GetDevicesTask

    @Binds
    abstract fun bindGetDeviceInfoTask(task: DefaultGetDeviceInfoTask): GetDeviceInfoTask

    @Binds
    abstract fun bindSetDeviceNameTask(task: DefaultSetDeviceNameTask): SetDeviceNameTask

    @Binds
    abstract fun bindUploadKeysTask(task: DefaultUploadKeysTask): UploadKeysTask

    @Binds
    abstract fun bindUploadSigningKeysTask(task: DefaultUploadSigningKeysTask): UploadSigningKeysTask

    @Binds
    abstract fun bindUploadSignaturesTask(task: DefaultUploadSignaturesTask): UploadSignaturesTask

    @Binds
    abstract fun bindDownloadKeysForUsersTask(task: DefaultDownloadKeysForUsers): DownloadKeysForUsersTask

    @Binds
    abstract fun bindCreateKeysBackupVersionTask(task: DefaultCreateKeysBackupVersionTask): CreateKeysBackupVersionTask

    @Binds
    abstract fun bindDeleteBackupTask(task: DefaultDeleteBackupTask): DeleteBackupTask

    @Binds
    abstract fun bindDeleteRoomSessionDataTask(task: DefaultDeleteRoomSessionDataTask): DeleteRoomSessionDataTask

    @Binds
    abstract fun bindDeleteRoomSessionsDataTask(task: DefaultDeleteRoomSessionsDataTask): DeleteRoomSessionsDataTask

    @Binds
    abstract fun bindDeleteSessionsDataTask(task: DefaultDeleteSessionsDataTask): DeleteSessionsDataTask

    @Binds
    abstract fun bindGetKeysBackupLastVersionTask(task: DefaultGetKeysBackupLastVersionTask): GetKeysBackupLastVersionTask

    @Binds
    abstract fun bindGetKeysBackupVersionTask(task: DefaultGetKeysBackupVersionTask): GetKeysBackupVersionTask

    @Binds
    abstract fun bindGetRoomSessionDataTask(task: DefaultGetRoomSessionDataTask): GetRoomSessionDataTask

    @Binds
    abstract fun bindGetRoomSessionsDataTask(task: DefaultGetRoomSessionsDataTask): GetRoomSessionsDataTask

    @Binds
    abstract fun bindGetSessionsDataTask(task: DefaultGetSessionsDataTask): GetSessionsDataTask

    @Binds
    abstract fun bindStoreRoomSessionDataTask(task: DefaultStoreRoomSessionDataTask): StoreRoomSessionDataTask

    @Binds
    abstract fun bindStoreRoomSessionsDataTask(task: DefaultStoreRoomSessionsDataTask): StoreRoomSessionsDataTask

    @Binds
    abstract fun bindStoreSessionsDataTask(task: DefaultStoreSessionsDataTask): StoreSessionsDataTask

    @Binds
    abstract fun bindUpdateKeysBackupVersionTask(task: DefaultUpdateKeysBackupVersionTask): UpdateKeysBackupVersionTask

    @Binds
    abstract fun bindSendToDeviceTask(task: DefaultSendToDeviceTask): SendToDeviceTask

    @Binds
    abstract fun bindEncryptEventTask(task: DefaultEncryptEventTask): EncryptEventTask

    @Binds
    abstract fun bindSendVerificationMessageTask(task: DefaultSendVerificationMessageTask): SendVerificationMessageTask

    @Binds
    abstract fun bindClaimOneTimeKeysForUsersDeviceTask(task: DefaultClaimOneTimeKeysForUsersDevice): ClaimOneTimeKeysForUsersDeviceTask

    @Binds
    abstract fun bindCrossSigningService(service: DefaultCrossSigningService): CrossSigningService

    @Binds
    abstract fun bindCryptoStore(store: RealmCryptoStore): IMXCryptoStore

    @Binds
    abstract fun bindComputeShieldTrustTask(task: DefaultComputeTrustTask): ComputeTrustTask

    @Binds
    abstract fun bindInitializeCrossSigningTask(task: DefaultInitializeCrossSigningTask): InitializeCrossSigningTask

    @Binds
    abstract fun bindSendEventTask(task: DefaultSendEventTask): SendEventTask
}
