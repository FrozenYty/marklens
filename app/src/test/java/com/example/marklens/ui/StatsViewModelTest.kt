package com.example.marklens.ui

import com.example.marklens.data.ExamRepository
import com.example.marklens.data.dao.ExamRecordDao
import com.example.marklens.data.dao.QuestionScoreDao
import com.example.marklens.data.dao.RegionTemplateDao
import com.example.marklens.data.dao.StudentDao
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.ui.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var examRecordDao: ExamRecordDao
    private lateinit var questionScoreDao: QuestionScoreDao
    private lateinit var repository: ExamRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        examRecordDao = mock()
        questionScoreDao = mock()
        val studentDao: StudentDao = mock()
        val regionTemplateDao: RegionTemplateDao = mock()
        repository = ExamRepository(studentDao, examRecordDao, questionScoreDao, regionTemplateDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nullRepository_shouldSetLoadingFalse() {
        val vm = StatsViewModel(repository = null)
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalRecords)
    }

    @Test
    fun emptyRecords_shouldShowZeroState() = runTest {
        whenever(examRecordDao.getAll()).thenReturn(flowOf(emptyList()))

        val vm = StatsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalRecords)
    }

    @Test
    fun records_shouldComputeStats() = runTest {
        val records = listOf(
            ExamRecord(1, 1, "Math", 85.0, "uri1", 1000L),
            ExamRecord(2, 2, "Math", 65.0, "uri2", 2000L)
        )
        val scores = listOf(
            QuestionScore(1, 1, 1, 8.0, 10.0, false),
            QuestionScore(2, 1, 2, 9.0, 10.0, false),
            QuestionScore(3, 2, 1, 6.0, 10.0, true),
            QuestionScore(4, 2, 2, 7.0, 10.0, false)
        )

        whenever(examRecordDao.getAll()).thenReturn(flowOf(records))
        whenever(examRecordDao.getById(1)).thenReturn(records[0])
        whenever(examRecordDao.getById(2)).thenReturn(records[1])
        whenever(questionScoreDao.getByExamRecordOnce(1)).thenReturn(scores.filter { it.examRecordId == 1L })
        whenever(questionScoreDao.getByExamRecordOnce(2)).thenReturn(scores.filter { it.examRecordId == 2L })

        val vm = StatsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.totalRecords)
        assertEquals(75.0, state.averageScore, 0.01)
        assertEquals(85.0, state.maxScore, 0.01)
        assertEquals(65.0, state.minScore, 0.01)
        assertEquals(1.0, state.passRate, 0.01) // both >= 60

        // per question stats
        assertEquals(2, state.perQuestionStats.size)

        // allScores for heatmap
        assertEquals(2, vm.allScores.value.size)
    }

    @Test
    fun subjectFilter_shouldOnlyIncludeMatchingRecords() = runTest {
        val records = listOf(
            ExamRecord(1, 1, "Math", 85.0, "uri1", 1000L),
            ExamRecord(2, 2, "English", 92.0, "uri2", 2000L)
        )
        val mathScores = listOf(
            QuestionScore(1, 1, 1, 8.0, 10.0, false)
        )

        whenever(examRecordDao.getAll()).thenReturn(flowOf(records))
        whenever(examRecordDao.getById(1)).thenReturn(records[0])
        whenever(questionScoreDao.getByExamRecordOnce(1)).thenReturn(mathScores)

        val vm = StatsViewModel(repository, subject = "Math")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.totalRecords)
        assertEquals("Math", state.subject)
    }
}
