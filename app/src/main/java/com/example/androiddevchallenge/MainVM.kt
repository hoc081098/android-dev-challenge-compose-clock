/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainVM : ViewModel() {
    private companion object {
        const val MAX_SECONDS = 30L
    }

    private val actionChannel = Channel<MainAction>(Channel.BUFFERED)

    val stateFlow: StateFlow<MainState>

    val dispatch = { action: MainAction ->
        if (!actionChannel.isClosedForSend) {
            actionChannel.offer(action)
        }
    }

    init {
        val initial = MainState(seconds = MAX_SECONDS)
        stateFlow = MutableStateFlow(initial)

        // keep resume seconds
        var resume = MAX_SECONDS

        actionChannel
            .consumeAsFlow()
            .onEach { action ->
                when (action) {
                    MainAction.START -> Unit
                    MainAction.PAUSE -> resume = stateFlow.value.seconds
                    MainAction.RESET -> resume = MAX_SECONDS
                }
            }
            .flatMapLatest { action ->
                when (action) {
                    MainAction.START -> {
                        generateSequence(resume - 1) { it - 1 }
                            .asFlow()
                            .onEach { delay(1_000) }
                            .onStart { emit(resume) }
                            .takeWhile { it >= 0 }
                            .map {
                                MainState(
                                    seconds = it,
                                    watchState = MainState.WatchState.RUNNING,
                                )
                            }
                            .onCompletion { emit(initial) }
                    }
                    MainAction.PAUSE -> {
                        flowOf(
                            MainState(
                                MainState.WatchState.PAUSED,
                                resume
                            )
                        )
                    }
                    MainAction.RESET -> {
                        flowOf(initial)
                    }
                }
            }
            .onEach { stateFlow.value = it }
            .onEach { Log.d("###", "Main state: $it") }
            .catch { }
            .launchIn(viewModelScope)
    }
}
