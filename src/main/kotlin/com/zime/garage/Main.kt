package com.zime.garage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zime.garage.common.LocalFileManager
import com.zime.garage.common.PreferencesManager

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    //초기화
    LocalFileManager.load()

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
//            LocalFileManager.TestJsonWrite()
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
