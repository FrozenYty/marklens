package com.example.marklens.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.parser.ParsedStudentInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

/**
 * ViewModel for the review & correction screen.
 *
 * @author Jianheng Sun
 */
class ReviewViewModel(
    private val repository: ExamRepository? = null
) : ViewModel() {

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

    /**
     * Persists the current review data to the database.
     * Parses string fields from the UI state into typed entities and saves
     * the student, exam record, and question scores atomically.
     *
     * @param imageUri URI of the original exam photo
     */
    fun save(imageUri: String) {
        val repo = repository ?: return
        if (_uiState.value.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val info = state.studentInfo

                // Get or create the student
                val student = repo.getOrCreateStudent(
                    name = info.name?.ifBlank { "Unknown" } ?: "Unknown",
                    studentId = info.studentId?.ifBlank { "0" } ?: "0",
                    className = info.className ?: ""
                )

                // Parse question scores from string fields
                val questionScores = state.scores.map { field ->
                    QuestionScore(
                        examRecordId = 0, // set by repository
                        questionNumber = field.questionNumber,
                        score = field.score.toDoubleOrNull() ?: 0.0,
                        maxScore = field.maxScore,
                        isWrong = (field.score.toDoubleOrNull() ?: 0.0) < field.maxScore
                    )
                }

                // Parse total score; fall back to sum of question scores
                val totalScore = state.totalScore.toDoubleOrNull()
                    ?: questionScores.sumOf { it.score }

                repo.saveExamWithScores(
                    student = student,
                    subject = state.subject.ifBlank { "Unknown" },
                    totalScore = totalScore,
                    imageUri = imageUri,
                    scores = questionScores
                )

                _uiState.update { it.copy(isSaving = false, saveComplete = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
