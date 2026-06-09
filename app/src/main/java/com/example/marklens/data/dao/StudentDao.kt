package com.example.marklens.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.marklens.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students")
    suspend fun getAllOnce(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): Student?

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getByStudentId(studentId: String): Student?

    @Query("SELECT * FROM students WHERE className = :className ORDER BY studentId")
    fun getByClass(className: String): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY studentId")
    fun getAll(): Flow<List<Student>>
}
