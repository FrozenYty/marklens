package com.example.marklens.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.data.entity.Student
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class StudentDaoTest {

    private lateinit var db: MarkLensDatabase
    private lateinit var dao: StudentDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarkLensDatabase::class.java
        ).build()
        dao = db.studentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_shouldReturnNonZeroId() = runTest {
        val id = dao.insert(Student(name = "Alice", studentId = "2024001", className = "Class 1"))
        assertTrue(id > 0)
    }

    @Test
    fun getByStudentId_shouldReturnCorrectStudent() = runTest {
        dao.insert(Student(name = "Bob", studentId = "2024002", className = "Class 2"))
        val result = dao.getByStudentId("2024002")
        assertNotNull(result)
        assertEquals("Bob", result!!.name)
    }

    @Test
    fun getByStudentId_notFound_shouldReturnNull() = runTest {
        val result = dao.getByStudentId("nonexistent")
        assertNull(result)
    }

    @Test
    fun getByClass_shouldReturnOnlySameClass() = runTest {
        dao.insert(Student(name = "Alice", studentId = "001", className = "Class A"))
        dao.insert(Student(name = "Bob", studentId = "002", className = "Class B"))
        dao.insert(Student(name = "Charlie", studentId = "003", className = "Class A"))

        dao.getByClass("Class A").test {
            val students = awaitItem()
            assertEquals(2, students.size)
            val names = students.map { it.name }.toSet()
            assertTrue(names.containsAll(listOf("Alice", "Charlie")))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun update_shouldPersistChanges() = runTest {
        val id = dao.insert(Student(name = "Old", studentId = "001", className = "C1"))
        dao.update(Student(id = id, name = "New", studentId = "001", className = "C1"))
        val result = dao.getByStudentId("001")
        assertEquals("New", result!!.name)
    }

    @Test
    fun delete_shouldRemoveStudent() = runTest {
        val id = dao.insert(Student(name = "Temp", studentId = "999", className = "X"))
        dao.delete(Student(id = id, name = "Temp", studentId = "999", className = "X"))
        val result = dao.getByStudentId("999")
        assertNull(result)
    }

    @Test
    fun getAll_shouldEmitAllStudents() = runTest {
        dao.insert(Student(name = "A", studentId = "1", className = "C"))
        dao.insert(Student(name = "B", studentId = "2", className = "C"))

        dao.getAll().test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
