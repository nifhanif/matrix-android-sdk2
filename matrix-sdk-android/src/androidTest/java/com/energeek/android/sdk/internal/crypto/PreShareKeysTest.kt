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

package com.nusaraya.android.sdk.internal.crypto

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import com.nusaraya.android.sdk.InstrumentedTest
import com.nusaraya.android.sdk.api.session.events.model.EventType
import com.nusaraya.android.sdk.api.session.events.model.toModel
import com.nusaraya.android.sdk.common.CommonTestHelper
import com.nusaraya.android.sdk.common.CryptoTestHelper
import com.nusaraya.android.sdk.internal.crypto.model.event.EncryptedEventContent
import com.nusaraya.android.sdk.internal.crypto.model.event.RoomKeyContent
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.JVM)
class PreShareKeysTest : InstrumentedTest {

    private val mTestHelper = CommonTestHelper(context())
    private val mCryptoTestHelper = CryptoTestHelper(mTestHelper)

    @Test
    fun ensure_outbound_session_happy_path() {
        val testData = mCryptoTestHelper.doE2ETestWithAliceAndBobInARoom(true)
        val e2eRoomID = testData.roomId
        val aliceSession = testData.firstSession
        val bobSession = testData.secondSession!!

        // clear any outbound session
        aliceSession.cryptoService().discardOutboundSession(e2eRoomID)

        val preShareCount = bobSession.cryptoService().getGossipingEvents().count {
            it.senderId == aliceSession.myUserId
                    && it.getClearType() == EventType.ROOM_KEY
        }

        assertEquals(0, preShareCount, "Bob should not have receive any key from alice at this point")
        Log.d("#Test", "Room Key Received from alice $preShareCount")

        // Force presharing of new outbound key
        mTestHelper.doSync<Unit> {
            aliceSession.cryptoService().prepareToEncrypt(e2eRoomID, it)
        }

        mTestHelper.waitWithLatch { latch ->
            mTestHelper.retryPeriodicallyWithLatch(latch) {
                val newGossipCount = bobSession.cryptoService().getGossipingEvents().count {
                    it.senderId == aliceSession.myUserId
                            && it.getClearType() == EventType.ROOM_KEY
                }
                newGossipCount > preShareCount
            }
        }

        val latest = bobSession.cryptoService().getGossipingEvents().lastOrNull {
            it.senderId == aliceSession.myUserId
                    && it.getClearType() == EventType.ROOM_KEY
        }

        val content = latest?.getClearContent().toModel<RoomKeyContent>()
        assertNotNull(content, "Bob should have received and decrypted a room key event from alice")
        assertEquals(e2eRoomID, content.roomId, "Wrong room")
        val megolmSessionId = content.sessionId!!

        val sharedIndex = aliceSession.cryptoService().getSharedWithInfo(e2eRoomID, megolmSessionId)
                .getObject(bobSession.myUserId, bobSession.sessionParams.deviceId)

        assertEquals(0, sharedIndex, "The session received by bob should match what alice sent")

        // Just send a real message as test
        val sentEvent = mTestHelper.sendTextMessage(aliceSession.getRoom(e2eRoomID)!!, "Allo", 1).first()

        assertEquals(megolmSessionId, sentEvent.root.content.toModel<EncryptedEventContent>()?.sessionId, "Unexpected megolm session")
        mTestHelper.waitWithLatch { latch ->
            mTestHelper.retryPeriodicallyWithLatch(latch) {
                bobSession.getRoom(e2eRoomID)?.getTimeLineEvent(sentEvent.eventId)?.root?.getClearType() == EventType.MESSAGE
            }
        }

        mTestHelper.signOutAndClose(aliceSession)
        mTestHelper.signOutAndClose(bobSession)
    }
}
