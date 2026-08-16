package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlaybackErrorLogRepository
import com.example.data.db.PlaybackErrorLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaybackDiagnosticsViewModel(
    private val repository: PlaybackErrorLogRepository
) : ViewModel() {

    val errorLogs: StateFlow<List<PlaybackErrorLogEntity>> = repository.observeRecentErrors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteError(errorId: Long) {
        viewModelScope.launch {
            repository.deleteError(errorId)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }
}
