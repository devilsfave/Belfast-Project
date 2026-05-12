package com.example.medgem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun DynamicStringListInput(
    label: String,
    items: List<String>,
    onItemsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item,
                    onValueChange = { newValue ->
                        val updatedList = items.toMutableList()
                        updatedList[index] = newValue
                        onItemsChanged(updatedList)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter item") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = if (index == items.lastIndex) ImeAction.Done else ImeAction.Next
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        val updatedList = items.toMutableList()
                        updatedList.removeAt(index)
                        onItemsChanged(updatedList)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        TextButton(
            onClick = {
                val updatedList = items.toMutableList()
                updatedList.add("")
                onItemsChanged(updatedList)
            },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Item")
        }
    }
}
