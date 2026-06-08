package com.example.marklens.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_scores",
    foreignKeys = [ForeignKey(
        entity = ExamRecord::class,
        parentColumns = ["id"],
        childColumns = ["examRecordId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("examRecordId")]
)
data class QuestionScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examRecordId: Long,
    val questionNumber: Int,
    val score: Double,
    val maxScore: Double,
    val isWrong: Boolean
)
