package com.offshore.platform.mobile.ui.view.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.KnowledgeCaseDao
import com.offshore.platform.mobile.data.local.entity.LocalKnowledgeCaseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val dao: KnowledgeCaseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeState())
    val uiState: StateFlow<KnowledgeState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            dao.observeAll().collect { cases ->
                _uiState.value = _uiState.value.copy(cases = cases, isLoading = false)
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = dao.search(query)
            _uiState.value = _uiState.value.copy(searchResults = results, searchQuery = query)
        }
    }
}

data class KnowledgeState(
    val isLoading: Boolean = true,
    val cases: List<LocalKnowledgeCaseEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<LocalKnowledgeCaseEntity> = emptyList()
)
