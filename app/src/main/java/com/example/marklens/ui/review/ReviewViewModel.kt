package com.example.marklens.ui.review

import androidx.lifecycle.ViewModel
import com.example.marklens.parser.ParsedStudentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReviewUiState(
    val studentInfo: ParsedStudentInfo = ParsedStudentInfo(null, null, null),
    val subject: String = "",
    val totalScore: String = "",
    val scores: List<ScoreField> = emptyList(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

data class ScoreField(
    val questionNumber: Int,
    val score: String,
    val maxScore: Double
)

class ReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun setParsedData(
        info: ParsedStudentInfo,
        subject: String,
        totalScore: String,
        scores: List<ScoreField>
    ) {
        _uiState.update {
            it.copy(
                studentInfo = info,
                subject = subject,
                totalScore = totalScore,
                scores = scores,
                saveComplete = false
            )
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(studentInfo = it.studentInfo.copy(name = value)) }
    }

    fun updateStudentId(value: String) {
        _uiState.update { it.copy(studentInfo = it.studentInfo.copy(studentId = value)) }
    }

    fun updateClassName(value: String) {
        _uiState.update { it.copy(studentInfo = it.studentInfo.copy(className = value)) }
    }

    fun updateSubject(value: String) {
        _uiState.update { it.copy(subject = value) }
    }

    fun updateTotalScore(value: String) {
        _uiState.update { it.copy(totalScore = value) }
    }

    fun updateScore(questionNumber: Int, value: String) {
        _uiState.update { state ->
            state.copy(scores = state.scores.map { f ->
                if (f.questionNumber == questionNumber) f.copy(score = value) else f
            })
        }
    }

    fun markSaveComplete() {
        _uiState.update { it.copy(saveComplete = true) }
    }
}
