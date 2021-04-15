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

package com.energeek.android.sdk.internal.task

import com.energeek.android.sdk.api.MatrixCallback
import com.energeek.android.sdk.api.util.Cancelable
import com.energeek.android.sdk.internal.extensions.foldToCallback
import com.energeek.android.sdk.internal.util.toCancelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal fun <T> CoroutineScope.launchToCallback(
        context: CoroutineContext = EmptyCoroutineContext,
        callback: MatrixCallback<T>,
        block: suspend () -> T
): Cancelable = launch(context, CoroutineStart.DEFAULT) {
    val result = runCatching {
        block()
    }
    withContext(Dispatchers.Main) {
        result.foldToCallback(callback)
    }
}.toCancelable()
