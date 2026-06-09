package com.example.marklens.ui

import com.example.marklens.data.ExamRepository
import com.example.marklens.data.dao.ExamRecordDao
import com.example.marklens.data.dao.QuestionScoreDao
import com.example.marklens.data.dao.RegionTemplateDao
import com.example.marklens.data.dao.StudentDao
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.data.entity.Student
import com.example.marklens.parser.ParsedStudentInfo
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.review.ScoreField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var studentDao: StudentDao
    private lateinit var examRecordDao: ExamRecordDao
    private lateinit var questionScoreDao: QuestionScoreDao
    private lateinit var regionTemplateDao: RegionTemplateDao
    private lateinit var repository: ExamRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studentDao = mock()
        examRecordDao = mock()
        questionScoreDao = mock()
        regionTemplateDao = mock()
        repository = ExamRepository(studentDao, examRecordDao, questionScoreDao, regionTemplateDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_shouldHaveEmptyFields() {
        val vm = ReviewViewModel()
        val state = vm.uiState.value
        assertEquals(null, state.studentInfo.name)
        assertTrue(state.scores.isEmpty())
        assertFalse(state.isSaving)
    }

    @Test
    fun setParsedData_shouldPopulateFields() {
        val vm = ReviewViewModel()
        val info = ParsedStudentInfo("Alice", "2024001", "Class 1")
        val scores = listOf(
            ScoreField(1, "8.5", 10.0),
            ScoreField(2, "9.0", 10.0)
        )
        vm.setParsedData(info, "Math", "17.5", scores)
        val state = vm.uiState.value
        assertEquals("Alice", state.studentInfo.name)
        assertEquals("Math", state.subject)
        assertEquals("17.5", state.totalScore)
        assertEquals(2, state.scores.size)
    }

    @Test
    fun updateName_shouldChangeField() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("Old", "", ""), "", "", emptyList())
        vm.updateName("New")
        assertEquals("New", vm.uiState.value.studentInfo.name)
    }

    @Test
    fun updateScore_shouldChangeSpecificQuestion() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("", "", ""), "", "", listOf(
            ScoreField(1, "5.0", 10.0),
            ScoreField(2, "8.0", 10.0)
        ))
        vm.updateScore(1, "9.5")
        assertEquals("9.5", vm.uiState.value.scores[0].score)
        assertEquals("8.0", vm.uiState.value.scores[1].score)
    }

    @Test
    fun markSaveComplete_shouldSetFlag() {
        val vm = ReviewViewModel()
        vm.setParsedData(ParsedStudentInfo("A", "1", "C"), "S", "10", emptyList())
        vm.markSaveComplete()
        assertTrue(vm.uiState.value.saveComplete)
    }

    @Test
    fun save_withNullRepository_shouldDoNothing() = runTest {
        val vm = ReviewViewModel(repository = null)
        vm.setParsedData(
            ParsedStudentInfo("Alice", "2024001", "Class 1"),
            "Math", "17.5", listOf(ScoreField(1, "8.5", 10.0))
        )
        vm.save("fake_uri")
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.uiState.value.saveComplete)
    }

    @Test
    fun save_shouldCallRepositoryAndSetSaveComplete() = runTest {
        // Arrange: mock DAO responses
        val savedStudent = Student(id = 1, name = "Alice", studentId = "2024001", className = "Class 1")
        whenever(studentDao.getByStudentId("2024001")).thenReturn(null)
        whenever(studentDao.insert(any())).thenReturn(1L)
        whenever(examRecordDao.insert(any())).thenReturn(42L)

        val vm = ReviewViewModel(repository)
        vm.setParsedData(
            ParsedStudentInfo("Alice", "2024001", "Class 1"),
            subject = "Math",
            totalScore = "17.5",
            scores = listOf(
                ScoreField(1, "8.5", 10.0),
                ScoreField(2, "9.0", 10.0)
            )
        )

        // Act
        vm.save("test_uri")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: ViewModel state updated
        val state = vm.uiState.value
        assertTrue(state.saveComplete, "saveComplete should be true after save")
        assertFalse(state.isSaving, "isSaving should be false after save")

        // Assert: repository was called correctly
        val recordCaptor = argumentCaptor<ExamRecord>()
        verify(examRecordDao).insert(recordCaptor.capture())
        val insertedRecord = recordCaptor.firstValue
        assertEquals("Math", insertedRecord.subject)
        assertEquals(17.5, insertedRecord.totalScore, 0.01)
        assertEquals("test_uri", insertedRecord.imageUri)
        assertEquals(1L, insertedRecord.studentId)
    }
}
