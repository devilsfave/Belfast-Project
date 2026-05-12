package com.example.medgem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.EmbeddingModuleProvider
import com.example.medgem.SearchState
import com.example.medgem.data.KnowledgeEntity
import com.example.medgem.data.KnowledgeEntity_
import com.example.medgem.data.ObjectBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class KnowledgeSearchViewModel @Inject constructor() : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isLoadingModel = MutableStateFlow(false)
    val isLoadingModel: StateFlow<Boolean> = _isLoadingModel.asStateFlow()

    fun updateQuery(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
    }

    fun clearSearch() {
        _searchState.value = SearchState()
        _isSearching.value = false
    }

    fun runSearch(query: String) {
        if (query.isBlank() || _isSearching.value || _isLoadingModel.value) return

        _searchState.value = _searchState.value.copy(query = query)

        viewModelScope.launch {
            try {
                // Lazy load the model if not already loaded
                if (!EmbeddingModuleProvider.isLoaded()) {
                    _isLoadingModel.value = true
                    _searchState.value =
                        _searchState.value.copy(status = "Initializing search engine...")

                    val result = EmbeddingModuleProvider.initialize()
                    if (result.isFailure) {
                        val error = result.exceptionOrNull()?.message ?: "Failed to load model"
                        _searchState.value = _searchState.value.copy(status = "Error: $error")
                        _isLoadingModel.value = false
                        return@launch
                    }
                    _isLoadingModel.value = false
                }

                val model = EmbeddingModuleProvider.getModel()
                if (model != null) {
                    _isSearching.value = true
                    _searchState.value = _searchState.value.copy(
                        query = query,
                        status = "Searching medical database...",
                        results = emptyList()
                    )
                    withContext(Dispatchers.Default) {
                        try {
                            val queryVector = model.encode(query, taskType = "Retrieval-query")
                            val box = ObjectBox.store.boxFor(KnowledgeEntity::class.java)
                            val dbQuery =
                                box.query(
                                    KnowledgeEntity_.embedding.nearestNeighbors(
                                        queryVector,
                                        10
                                    )
                                )
                                    .build()
                            try {
                                val results = dbQuery.findWithScores()

                                withContext(Dispatchers.Main) {
                                    // Convert distance (score) to similarity (1 - distance)
                                    val newResults =
                                        results.map { result ->
                                            Pair(
                                                result.get(),
                                                1.0 - result.score
                                            )
                                        }
                                    _searchState.value = _searchState.value.copy(
                                        query = query,
                                        results = newResults,
                                        status =
                                            if (newResults.isEmpty()) "No results found."
                                            else ""
                                    )
                                }
                            } finally {
                                dbQuery.close()
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                _searchState.value =
                                    _searchState.value.copy(status = "Error: ${e.message}")
                                e.printStackTrace()
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                _isSearching.value = false
                            }
                        }
                    }
                } else {
                    _searchState.value =
                        _searchState.value.copy(status = "Embedding Model not loaded.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(status = "Error: ${e.message}")
                _isLoadingModel.value = false
            }
        }
    }
}
