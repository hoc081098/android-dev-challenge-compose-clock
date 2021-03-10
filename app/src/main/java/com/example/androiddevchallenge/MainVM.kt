package com.example.androiddevchallenge

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

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