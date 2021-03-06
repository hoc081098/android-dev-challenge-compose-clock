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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp() {
    val vm = viewModel<MainVM>()
    val state by vm.stateFlow.collectAsState()

    val mm = (state.seconds / 60).toString().padStart(2, '0')
    val ss = (state.seconds % 60).toString().padStart(2, '0')

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Countdown Timer")
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Buttons(state.watchState, vm.dispatch)
                }
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Text(text = "$mm:$ss", style = MaterialTheme.typography.h3)
            }
        }
    }
}

@Composable
fun Buttons(watchState: MainState.WatchState, dispatch: (MainAction) -> Unit) {
    when (watchState) {
        MainState.WatchState.RUNNING -> {
            Button(onClick = { dispatch(MainAction.START) }, enabled = false) {
                Text(text = "START")
            }
            Button(onClick = { dispatch(MainAction.PAUSE) }) {
                Text(text = "PAUSE")
            }
            Button(onClick = { dispatch(MainAction.RESET) }) {
                Text(text = "RESET")
            }
        }
        MainState.WatchState.PAUSED -> {
            Button(onClick = { dispatch(MainAction.START) }) {
                Text(text = "RESUME")
            }
            Button(onClick = { dispatch(MainAction.PAUSE) }, enabled = false) {
                Text(text = "PAUSE")
            }
            Button(onClick = { dispatch(MainAction.RESET) }) {
                Text(text = "RESET")
            }
        }
        MainState.WatchState.IDLE -> {
            Button(onClick = { dispatch(MainAction.START) }) {
                Text(text = "START")
            }
            Button(onClick = { dispatch(MainAction.PAUSE) }, enabled = false) {
                Text(text = "PAUSE")
            }
            Button(onClick = { dispatch(MainAction.RESET) }, enabled = false) {
                Text(text = "RESET")
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
