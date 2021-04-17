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

import com.squareup.moshi.Moshi
import com.nusaraya.android.sdk.api.session.room.model.message.MessageAudioContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageDefaultContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageEmoteContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageFileContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageImageContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageLocationContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageNoticeContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageOptionsContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessagePollResponseContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageTextContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageType
import com.nusaraya.android.sdk.api.session.room.model.message.MessageVerificationRequestContent
import com.nusaraya.android.sdk.api.session.room.model.message.MessageVideoContent
import com.nusaraya.android.sdk.internal.network.parsing.CipherSuiteMoshiAdapter
import com.nusaraya.android.sdk.internal.network.parsing.ForceToBooleanJsonAdapter
import com.nusaraya.android.sdk.internal.network.parsing.RuntimeJsonAdapterFactory
import com.nusaraya.android.sdk.internal.network.parsing.TlsVersionMoshiAdapter
import com.nusaraya.android.sdk.internal.network.parsing.UriMoshiAdapter
import com.nusaraya.android.sdk.internal.session.sync.parsing.DefaultLazyRoomSyncEphemeralJsonAdapter

object MoshiProvider {

    private val moshi: Moshi = Moshi.Builder()
            .add(UriMoshiAdapter())
            .add(ForceToBooleanJsonAdapter())
            .add(CipherSuiteMoshiAdapter())
            .add(TlsVersionMoshiAdapter())
            // Use addLast here so we can inject a SplitLazyRoomSyncJsonAdapter later to override the default parsing.
            .addLast(DefaultLazyRoomSyncEphemeralJsonAdapter())
            .add(RuntimeJsonAdapterFactory.of(MessageContent::class.java, "msgtype", MessageDefaultContent::class.java)
                    .registerSubtype(MessageTextContent::class.java, MessageType.MSGTYPE_TEXT)
                    .registerSubtype(MessageNoticeContent::class.java, MessageType.MSGTYPE_NOTICE)
                    .registerSubtype(MessageEmoteContent::class.java, MessageType.MSGTYPE_EMOTE)
                    .registerSubtype(MessageAudioContent::class.java, MessageType.MSGTYPE_AUDIO)
                    .registerSubtype(MessageImageContent::class.java, MessageType.MSGTYPE_IMAGE)
                    .registerSubtype(MessageVideoContent::class.java, MessageType.MSGTYPE_VIDEO)
                    .registerSubtype(MessageLocationContent::class.java, MessageType.MSGTYPE_LOCATION)
                    .registerSubtype(MessageFileContent::class.java, MessageType.MSGTYPE_FILE)
                    .registerSubtype(MessageVerificationRequestContent::class.java, MessageType.MSGTYPE_VERIFICATION_REQUEST)
                    .registerSubtype(MessageOptionsContent::class.java, MessageType.MSGTYPE_OPTIONS)
                    .registerSubtype(MessagePollResponseContent::class.java, MessageType.MSGTYPE_RESPONSE)
            )
            .add(SerializeNulls.JSON_ADAPTER_FACTORY)
            .build()

    fun providesMoshi(): Moshi {
        return moshi
    }
}
