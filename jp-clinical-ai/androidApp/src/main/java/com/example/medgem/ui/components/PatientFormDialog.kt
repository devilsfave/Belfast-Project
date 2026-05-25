package com.example.medgem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medgem.data.PatientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormDialog(
    initialPatient: PatientEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, List<String>, List<String>, List<String>) -> Unit
) {
    var name by remember { mutableStateOf(initialPatient?.name ?: "") }
    var age by remember { mutableStateOf(initialPatient?.age?.toString() ?: "") }
    val genderOptions = listOf("Male", "Female", "Other")
    var genderIndex by remember {
        val index = if (initialPatient != null) {
            genderOptions.indexOfFirst { it.equals(initialPatient.gender, ignoreCase = true) }
        } else 0
        mutableStateOf(if (index >= 0) index else 0)
    }
    var allergies by remember { mutableStateOf(initialPatient?.allergies ?: listOf()) }
    var conditions by remember { mutableStateOf(initialPatient?.chronicConditions ?: listOf()) }
    var medications by remember { mutableStateOf(initialPatient?.currentMedications ?: listOf()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialPatient == null) "Add Patient" else "Edit Patient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = age,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 3) age = it
                        },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    genderOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = genderOptions.size
                            ),
                            onClick = { genderIndex = index },
                            selected = index == genderIndex
                        ) {
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DynamicStringListInput(
                    label = "Allergies",
                    items = allergies,
                    onItemsChanged = { allergies = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                DynamicStringListInput(
                    label = "Chronic Conditions",
                    items = conditions,
                    onItemsChanged = { conditions = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                DynamicStringListInput(
                    label = "Medications",
                    items = medications,
                    onItemsChanged = { medications = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        age.toIntOrNull() ?: 0,
                        genderOptions[genderIndex],
                        allergies.filter { it.isNotBlank() },
                        conditions.filter { it.isNotBlank() },
                        medications.filter { it.isNotBlank() }
                    )
                },
                enabled = name.isNotBlank() && age.isNotBlank()
            ) {
                Text(if (initialPatient == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
