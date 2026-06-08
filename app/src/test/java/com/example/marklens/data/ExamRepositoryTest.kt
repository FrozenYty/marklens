package com.example.marklens.data

import androidx.room.Room
import app.cash.turbine.test
import com.example.marklens.data.entity.QuestionScore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@org.junit.runner.RunWith(RobolectricTestRunner::class)
class ExamRepositoryTest {

    private lateinit var db: MarkLensDatabase
    private lateinit var repo: ExamRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            MarkLensDatabase::class.java
        ).build()
        repo = ExamRepository(db.studentDao(), db.examRecordDao(), db.questionScoreDao(), db.regionTemplateDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getOrCreateStudent_newStudent_shouldInsertAndReturn() = runTest {
        val student = repo.getOrCreateStudent("Alice", "2024001", "Class 1")
        assertTrue(student.id > 0)
        assertEquals("Alice", student.name)
    }

    @Test
    fun getOrCreateStudent_existingStudent_shouldReturnExisting() = runTest {
        val first = repo.getOrCreateStudent("Bob", "2024002", "Class 2")
        val second = repo.getOrCreateStudent("Bobby", "2024002", "Class 2")
        assertEquals(first.id, second.id)
        assertEquals("Bob", second.name)
    }

    @Test
    fun saveExamWithScores_shouldInsertRecordAndScores() = runTest {
        val student = repo.getOrCreateStudent("Alice", "001", "C1")
        val scores = listOf(
            QuestionScore(examRecordId = 0, questionNumber = 1, score = 8.0, maxScore = 10.0, isWrong = false),
            QuestionScore(examRecordId = 0, questionNumber = 2, score = 5.0, maxScore = 10.0, isWrong = true)
        )
        val recordId = repo.saveExamWithScores(student, "Math", 13.0, "/img.jpg", scores)

        val (record, savedScores) = repo.getRecordWithScores(recordId)!!
        assertEquals("Math", record.subject)
        assertEquals(13.0, record.totalScore, 0.01)
        assertEquals(2, savedScores.size)
        assertEquals(recordId, savedScores[0].examRecordId)
    }

    @Test
    fun deleteRecord_shouldCascadeScores() = runTest {
        val student = repo.getOrCreateStudent("C", "002", "C2")
        val scores = listOf(QuestionScore(examRecordId = 0, questionNumber = 1, score = 5.0, maxScore = 5.0, isWrong = false))
        val recordId = repo.saveExamWithScores(student, "Physics", 5.0, "/img.jpg", scores)

        repo.deleteRecord(recordId)
        assertNull(repo.getRecordWithScores(recordId))
    }

    @Test
    fun getRecordsBySubject_shouldFilterCorrectly() = runTest {
        val s = repo.getOrCreateStudent("D", "003", "C3")
        repo.saveExamWithScores(s, "Math", 90.0, "/a.jpg", emptyList())
        repo.saveExamWithScores(s, "English", 85.0, "/b.jpg", emptyList())

        repo.getRecordsBySubject("Math").test {
            val records = awaitItem()
            assertEquals(1, records.size)
            assertEquals("Math", records[0].subject)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveTemplate_shouldReturnId() = runTest {
        val id = repo.saveTemplate("Midterm", "[{}]")
        assertTrue(id > 0)
    }

    @Test
    fun getAllTemplates_shouldEmitAll() = runTest {
        repo.saveTemplate("A", "[]")
        repo.saveTemplate("B", "[]")

        repo.getAllTemplates().test {
            val templates = awaitItem()
            assertEquals(2, templates.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecords_shouldEmitAll() = runTest {
        val s = repo.getOrCreateStudent("E", "004", "C4")
        repo.saveExamWithScores(s, "Math", 100.0, "/a.jpg", emptyList())
        repo.saveExamWithScores(s, "Physics", 95.0, "/b.jpg", emptyList())

        repo.getAllRecords().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecordWithScores_notFound_shouldReturnNull() = runTest {
        assertNull(repo.getRecordWithScores(999))
    }
}
