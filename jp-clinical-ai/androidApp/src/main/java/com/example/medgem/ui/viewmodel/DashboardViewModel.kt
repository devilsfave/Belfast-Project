package com.example.medgem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.ConversationEntity
import com.example.medgem.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    chatRepository: ChatRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val recentConversations: StateFlow<List<ConversationEntity>> =
        chatRepository.getGlobalConversations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val userName: StateFlow<String> = userPreferencesRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "User"
        )
}
