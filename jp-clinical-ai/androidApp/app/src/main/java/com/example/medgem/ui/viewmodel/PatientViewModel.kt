package com.example.medgem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.ConversationEntity
import com.example.medgem.data.PatientEntity
import com.example.medgem.data.PatientRepository
import com.example.medgem.data.VisitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PatientViewModel
@Inject
constructor(private val repository: PatientRepository, private val chatRepository: ChatRepository) :
    ViewModel() {

    val patients: StateFlow<List<PatientEntity>> =
        repository.getAllPatients().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val visitsCache = ConcurrentHashMap<Long, StateFlow<List<VisitEntity>>>()
    private val conversationsCache = ConcurrentHashMap<Long, StateFlow<List<ConversationEntity>>>()

    fun createPatient(
        name: String,
        age: Int,
        gender: String,
        allergies: List<String> = emptyList(),
        chronicConditions: List<String> = emptyList(),
        currentMedications: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            repository.createPatient(
                name,
                age,
                gender,
                allergies,
                chronicConditions,
                currentMedications
            )
        }
    }

    fun updatePatient(patient: PatientEntity) {
        viewModelScope.launch { repository.updatePatient(patient) }
    }

    suspend fun deletePatient(patientId: Long) {
        chatRepository.deleteConversationsForPatient(patientId)
        repository.deletePatient(patientId)
    }

    fun getPatientVisits(patientId: Long): StateFlow<List<VisitEntity>> {
        return visitsCache.getOrPut(patientId) {
            repository
                .getPatientVisits(patientId)
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    fun getPatientConversations(patientId: Long): StateFlow<List<ConversationEntity>> {
        return conversationsCache.getOrPut(patientId) {
            chatRepository
                .getPatientConversations(patientId)
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        }
    }

    suspend fun deleteVisit(visitId: Long) {
        repository.deleteVisit(visitId)
    }

    suspend fun deleteConversation(conversationId: Long) {
        chatRepository.deleteConversation(conversationId)
    }

    suspend fun createVisit(patientId: Long): Long {
        return repository.createVisit(patientId)
    }

    suspend fun getPatient(id: Long): PatientEntity? {
        return repository.getPatient(id)
    }
}
