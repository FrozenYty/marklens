package com.example.marklens.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.marklens.data.entity.QuestionScore
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionScoreDao {
    @Insert
    suspend fun insertAll(scores: List<QuestionScore>)

    @Update
    suspend fun update(score: QuestionScore)

    @Query("SELECT * FROM question_scores WHERE examRecordId = :recordId ORDER BY questionNumber")
    fun getByExamRecord(recordId: Long): Flow<List<QuestionScore>>

    @Query("SELECT * FROM question_scores WHERE examRecordId = :recordId ORDER BY questionNumber")
    suspend fun getByExamRecordOnce(recordId: Long): List<QuestionScore>

    @Query("DELETE FROM question_scores WHERE examRecordId = :recordId")
    suspend fun deleteByExamRecord(recordId: Long)
}
