package com.example.marklens.data.dao

import androidx.room.Room
import app.cash.turbine.test
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.data.entity.Student
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@org.junit.runner.RunWith(RobolectricTestRunner::class)
class QuestionScoreDaoTest {

    private lateinit var db: MarkLensDatabase
    private lateinit var dao: QuestionScoreDao
    private var recordId: Long = 0

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            MarkLensDatabase::class.java
        ).build()
        dao = db.questionScoreDao()
        val sid = db.studentDao().insert(
            Student(name = "T", studentId = "S0", className = "C0")
        )
        recordId = db.examRecordDao().insert(
            ExamRecord(studentId = sid, subject = "Math", totalScore = 100.0, imageUri = "")
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAll_shouldPersistScores() = runTest {
        dao.insertAll(listOf(
            QuestionScore(examRecordId = recordId, questionNumber = 1, score = 8.0, maxScore = 10.0, isWrong = false),
            QuestionScore(examRecordId = recordId, questionNumber = 2, score = 5.0, maxScore = 10.0, isWrong = true)
        ))

        dao.getByExamRecord(1).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByExamRecord_shouldReturnOrderedByQuestionNumber() = runTest {
        dao.insertAll(listOf(
            QuestionScore(examRecordId = recordId, questionNumber = 3, score = 7.0, maxScore = 10.0, isWrong = false),
            QuestionScore(examRecordId = recordId, questionNumber = 1, score = 9.0, maxScore = 10.0, isWrong = false)
        ))

        dao.getByExamRecord(1).test {
            val items = awaitItem()
            assertEquals(1, items[0].questionNumber)
            assertEquals(3, items[1].questionNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByExamRecordOnce_shouldReturnList() = runTest {
        dao.insertAll(listOf(
            QuestionScore(examRecordId = recordId, questionNumber = 1, score = 10.0, maxScore = 10.0, isWrong = false)
        ))
        val scores = dao.getByExamRecordOnce(2)
        assertEquals(1, scores.size)
        assertEquals(10.0, scores[0].score, 0.01)
    }

    @Test
    fun update_shouldPersistChanges() = runTest {
        dao.insertAll(listOf(
            QuestionScore(id = 0, examRecordId = recordId, questionNumber = 1, score = 0.0, maxScore = 10.0, isWrong = true)
        ))

        val scores = dao.getByExamRecordOnce(1)
        dao.update(scores[0].copy(score = 8.5, isWrong = false))

        val updated = dao.getByExamRecordOnce(1)
        assertEquals(8.5, updated[0].score, 0.01)
        assertFalse(updated[0].isWrong)
    }

    @Test
    fun deleteByExamRecord_shouldRemoveAllScores() = runTest {
        dao.insertAll(listOf(
            QuestionScore(examRecordId = recordId, questionNumber = 1, score = 8.0, maxScore = 10.0, isWrong = false)
        ))
        dao.deleteByExamRecord(1)

        val result = dao.getByExamRecordOnce(1)
        assertTrue(result.isEmpty())
    }
}
