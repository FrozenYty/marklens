package com.example.marklens.data.dao

import androidx.room.Room
import app.cash.turbine.test
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.data.entity.ExamRecord
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
class ExamRecordDaoTest {

    private lateinit var db: MarkLensDatabase
    private lateinit var dao: ExamRecordDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            MarkLensDatabase::class.java
        ).build()
        dao = db.examRecordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_shouldReturnNonZeroId() = runTest {
        val id = dao.insert(createRecord(1, "Math", 95.0))
        assertTrue(id > 0)
    }

    @Test
    fun getById_shouldReturnCorrectRecord() = runTest {
        val id = dao.insert(createRecord(1, "Physics", 88.5))
        val result = dao.getById(id)
        assertNotNull(result)
        assertEquals("Physics", result!!.subject)
    }

    @Test
    fun getById_notFound_shouldReturnNull() = runTest {
        assertNull(dao.getById(999))
    }

    @Test
    fun getBySubject_shouldFilterCorrectly() = runTest {
        dao.insert(createRecord(1, "Math", 90.0))
        dao.insert(createRecord(2, "English", 85.0))
        dao.insert(createRecord(3, "Math", 78.0))

        dao.getBySubject("Math").test {
            val records = awaitItem()
            assertEquals(2, records.size)
            assertTrue(records.all { it.subject == "Math" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByStudentId_shouldReturnOnlyThatStudent() = runTest {
        dao.insert(createRecord(1, "Math", 90.0))
        dao.insert(createRecord(2, "Math", 85.0))
        dao.insert(createRecord(1, "English", 88.0))

        dao.getByStudentId(1).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun update_shouldPersistChanges() = runTest {
        val id = dao.insert(createRecord(1, "Math", 90.0))
        dao.update(ExamRecord(id = id, studentId = 1, subject = "Math", totalScore = 95.0, imageUri = ""))
        assertEquals(95.0, dao.getById(id)!!.totalScore, 0.01)
    }

    @Test
    fun delete_shouldRemoveRecord() = runTest {
        val id = dao.insert(createRecord(1, "Math", 90.0))
        dao.delete(ExamRecord(id = id, studentId = 1, subject = "Math", totalScore = 90.0, imageUri = ""))
        assertNull(dao.getById(id))
    }

    @Test
    fun getAll_shouldEmitAllRecords() = runTest {
        dao.insert(createRecord(1, "A", 100.0))
        dao.insert(createRecord(2, "B", 90.0))

        dao.getAll().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createRecord(studentId: Long, subject: String, totalScore: Double) =
        ExamRecord(studentId = studentId, subject = subject, totalScore = totalScore, imageUri = "/test.jpg")
}
