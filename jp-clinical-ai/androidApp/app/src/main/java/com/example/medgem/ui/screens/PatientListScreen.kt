package com.example.medgem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.data.PatientEntity
import com.example.medgem.ui.components.DynamicStringListInput
import com.example.medgem.ui.components.PatientFormDialog
import com.example.medgem.ui.components.MedGemTopBar
import com.example.medgem.ui.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    onBack: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit
) {
    val viewModel: PatientViewModel = hiltViewModel()
    val patients by viewModel.patients.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MedGemTopBar(
                title = "Patients",
                onBack = onBack,
                centerTitle = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (patients.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(64.dp))
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text("No patients yet", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(patients) { patient ->
                        PatientCard(
                            patient = patient,
                            onClick = { onNavigateToPatientDetail(patient.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            PatientFormDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, age, gender, allergies, conditions, medications ->
                    viewModel.createPatient(name, age, gender, allergies, conditions, medications)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun PatientCard(patient: PatientEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = patient.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${patient.gender}, ${patient.age} years old",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
