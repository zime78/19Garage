package com.zime.garage.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.window.Dialog

@Composable
fun SimpleDatePicker(
    selectedDate: MutableState<Date>,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            val calendar = Calendar.getInstance().apply { time = selectedDate.value }

            Column {
                // Year
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.YEAR, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.YEAR)}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.YEAR, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                // Month
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.MONTH, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.MONTH) + 1}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.MONTH, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                // Day
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { calendar.add(Calendar.DAY_OF_MONTH, -1); selectedDate.value = calendar.time }) {
                        Text("-")
                    }
                    Text("${calendar.get(Calendar.DAY_OF_MONTH)}", modifier = Modifier.alignByBaseline())
                    TextButton(onClick = { calendar.add(Calendar.DAY_OF_MONTH, 1); selectedDate.value = calendar.time }) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { onDismissRequest() }) {
                    Text("확인")
                }
            }
        }
    }
}
