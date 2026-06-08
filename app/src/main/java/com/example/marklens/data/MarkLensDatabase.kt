package com.example.marklens.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.marklens.data.dao.ExamRecordDao
import com.example.marklens.data.dao.QuestionScoreDao
import com.example.marklens.data.dao.RegionTemplateDao
import com.example.marklens.data.dao.StudentDao
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.data.entity.RegionTemplate
import com.example.marklens.data.entity.Student

@Database(
    entities = [Student::class, ExamRecord::class, QuestionScore::class, RegionTemplate::class],
    version = 1,
    exportSchema = false
)
abstract class MarkLensDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun examRecordDao(): ExamRecordDao
    abstract fun questionScoreDao(): QuestionScoreDao
    abstract fun regionTemplateDao(): RegionTemplateDao
}
