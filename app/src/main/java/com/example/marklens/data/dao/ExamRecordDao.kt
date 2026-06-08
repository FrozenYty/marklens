package com.example.marklens.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.marklens.data.entity.ExamRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamRecordDao {
    @Insert
    suspend fun insert(record: ExamRecord): Long

    @Update
    suspend fun update(record: ExamRecord)

    @Delete
    suspend fun delete(record: ExamRecord)

    @Query("SELECT * FROM exam_records WHERE id = :id")
    suspend fun getById(id: Long): ExamRecord?

    @Query("SELECT * FROM exam_records WHERE subject = :subject ORDER BY createdAt DESC")
    fun getBySubject(subject: String): Flow<List<ExamRecord>>

    @Query("SELECT * FROM exam_records WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getByStudentId(studentId: Long): Flow<List<ExamRecord>>

    @Query("SELECT * FROM exam_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ExamRecord>>
}
