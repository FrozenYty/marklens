package com.example.marklens.ui

import com.example.marklens.data.ExamRepository
import com.example.marklens.data.dao.ExamRecordDao
import com.example.marklens.data.dao.QuestionScoreDao
import com.example.marklens.data.dao.RegionTemplateDao
import com.example.marklens.data.dao.StudentDao
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.ui.list.RecordListEvent
import com.example.marklens.ui.list.RecordListViewModel
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecordListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var examRecordDao: ExamRecordDao
    private lateinit var repository: ExamRepository

    private lateinit var studentDao: StudentDao

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        Dispatchers.setMain(testDispatcher)
        examRecordDao = mock()
        studentDao = mock()
        val questionScoreDao: QuestionScoreDao = mock()
        val regionTemplateDao: RegionTemplateDao = mock()
        whenever(studentDao.getAllOnce()).thenReturn(emptyList())
        repository = ExamRepository(studentDao, examRecordDao, questionScoreDao, regionTemplateDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_shouldHaveLoadingTrue() = runTest {
        whenever(examRecordDao.getAll()).thenReturn(flowOf(emptyList()))
        val vm = RecordListViewModel(repository)

        val state = vm.uiState.value
        assertTrue(state.isLoading)
    }

    @Test
    fun records_shouldFlowFromRepository() = runTest {
        val records = listOf(
            ExamRecord(1, 1, "Math", 85.0, "uri1", 1000L),
            ExamRecord(2, 2, "English", 92.0, "uri2", 2000L)
        )
        whenever(examRecordDao.getAll()).thenReturn(flowOf(records))

        val vm = RecordListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.records.size)
        assertEquals("Math", state.records[0].subject)
        assertTrue(state.subjects.contains("Math"))
        assertTrue(state.subjects.contains("English"))
        assertTrue(!state.isLoading)
    }

    @Test
    fun subjectFilter_shouldFilterRecords() = runTest {
        val records = listOf(
            ExamRecord(1, 1, "Math", 85.0, "uri1", 1000L),
            ExamRecord(2, 2, "English", 92.0, "uri2", 2000L)
        )
        whenever(examRecordDao.getAll()).thenReturn(flowOf(records))

        val vm = RecordListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Apply filter
        vm.onEvent(RecordListEvent.SubjectSelected("Math"))

        val state = vm.uiState.value
        assertEquals(1, state.records.size)
        assertEquals("Math", state.records[0].subject)
        assertEquals("Math", state.selectedSubject)
    }

    @Test
    fun delete_shouldCallRepository() = runTest {
        whenever(examRecordDao.getAll()).thenReturn(flowOf(emptyList()))
        whenever(examRecordDao.getById(1)).thenReturn(null)

        val vm = RecordListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(RecordListEvent.RecordDeleted(1))
        testDispatcher.scheduler.advanceUntilIdle()

        verify(examRecordDao).getById(1)
    }

    @Test
    fun nullRepository_shouldNotCrash() {
        val vm = RecordListViewModel(repository = null)
        val state = vm.uiState.value
        assertTrue(state.records.isEmpty())
        assertTrue(!state.isLoading)
    }
}
