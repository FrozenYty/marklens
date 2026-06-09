package com.example.marklens.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.util.QuestionStat
import com.example.marklens.util.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val subject: String = "All Subjects",
    val totalRecords: Int = 0,
    val averageScore: Double = 0.0,
    val maxScore: Double = 0.0,
    val minScore: Double = 0.0,
    val passRate: Double = 0.0,
    val scoreDistribution: Map<String, Int> = emptyMap(),
    val perQuestionStats: List<QuestionStat> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for the statistics screen — loads exam data and computes
 * aggregate statistics via StatsCalculator.
 *
 * @author Jianheng Sun
 */
class StatsViewModel(
    private val repository: ExamRepository? = null,
    private val subject: String? = null
) : ViewModel() {

    private val calculator = StatsCalculator()
    private val _uiState = MutableStateFlow(
        StatsUiState(subject = subject ?: "All Subjects")
    )
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _allScores = MutableStateFlow<Map<Long, List<QuestionScore>>>(emptyMap())
    val allScores: StateFlow<Map<Long, List<QuestionScore>>> = _allScores.asStateFlow()

    init {
        val repo = repository
        if (repo != null) {
            viewModelScope.launch {
                repo.getAllRecords().collect { records ->
                val filtered = if (subject != null) records.filter { it.subject == subject }
                    else records

                if (filtered.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            totalRecords = 0,
                            isLoading = false,
                            subject = subject ?: "All Subjects"
                        )
                    }
                    return@collect
                }

                // Load scores for each record
                val scoresMap = mutableMapOf<Long, List<QuestionScore>>()
                for (record in filtered) {
                    val pair = repo.getRecordWithScores(record.id)
                    if (pair != null) {
                        scoresMap[record.id] = pair.second
                    }
                }
                _allScores.value = scoresMap

                // Compute stats
                val result = calculator.calculate(filtered, scoresMap)
                _uiState.update {
                    StatsUiState(
                        subject = subject ?: "All Subjects",
                        totalRecords = result.totalRecords,
                        averageScore = result.averageScore,
                        maxScore = result.maxScore,
                        minScore = result.minScore,
                        passRate = result.passRate,
                        scoreDistribution = result.scoreDistribution,
                        perQuestionStats = result.perQuestionStats,
                        isLoading = false
                    )
                }
            }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
