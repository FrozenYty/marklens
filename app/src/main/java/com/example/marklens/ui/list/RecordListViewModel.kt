package com.example.marklens.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.entity.ExamRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordListUiState(
    val records: List<ExamRecord> = emptyList(),
    val studentNames: Map<Long, String> = emptyMap(),
    val selectedSubject: String? = null,
    val subjects: List<String> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface RecordListEvent {
    data class SubjectSelected(val subject: String?) : RecordListEvent
    data class RecordClicked(val recordId: Long) : RecordListEvent
    data class RecordDeleted(val recordId: Long) : RecordListEvent
}

/**
 * ViewModel for the record list screen — shows all saved exam records
 * with optional subject filtering.
 *
 * @author Jianheng Sun
 */
class RecordListViewModel(
    private val repository: ExamRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordListUiState())
    val uiState: StateFlow<RecordListUiState> = _uiState.asStateFlow()

    private var allRecords: List<ExamRecord> = emptyList()

    init {
        val repo = repository
        if (repo != null) {
            viewModelScope.launch {
                val names = repo.getStudentNameMap()
                repo.getAllRecords().collect { records ->
                allRecords = records
                val subjects = records.map { it.subject }.distinct().sorted()
                val selected = _uiState.value.selectedSubject
                val filtered = if (selected == null) records
                    else records.filter { it.subject == selected }
                _uiState.update {
                    it.copy(
                        records = filtered,
                        studentNames = names,
                        subjects = subjects,
                        isLoading = false
                    )
                }
            }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onEvent(event: RecordListEvent) {
        when (event) {
            is RecordListEvent.SubjectSelected -> {
                val selected = event.subject
                val filtered = if (selected == null) allRecords
                    else allRecords.filter { it.subject == selected }
                _uiState.update {
                    it.copy(
                        records = filtered,
                        selectedSubject = selected
                    )
                }
            }
            is RecordListEvent.RecordClicked -> {
                // Navigation handled by screen callback
            }
            is RecordListEvent.RecordDeleted -> {
                val repo = repository ?: return
                viewModelScope.launch {
                    repo.deleteRecord(event.recordId)
                }
            }
        }
    }
}
