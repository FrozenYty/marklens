package com.example.marklens.data

import com.example.marklens.data.dao.ExamRecordDao
import com.example.marklens.data.dao.QuestionScoreDao
import com.example.marklens.data.dao.RegionTemplateDao
import com.example.marklens.data.dao.StudentDao
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.data.entity.RegionTemplate
import com.example.marklens.data.entity.Student
import kotlinx.coroutines.flow.Flow

class ExamRepository(
    private val studentDao: StudentDao,
    private val examRecordDao: ExamRecordDao,
    private val questionScoreDao: QuestionScoreDao,
    private val regionTemplateDao: RegionTemplateDao
) {
    // ── Student ──

    suspend fun getStudentNameMap(): Map<Long, String> {
        return studentDao.getAllOnce().associate { it.id to it.name }
    }

    suspend fun getOrCreateStudent(name: String, studentId: String, className: String): Student {
        val existing = studentDao.getByStudentId(studentId)
        if (existing != null) return existing
        val id = studentDao.insert(Student(name = name, studentId = studentId, className = className))
        return Student(id = id, name = name, studentId = studentId, className = className)
    }

    fun getStudentsByClass(className: String): Flow<List<Student>> =
        studentDao.getByClass(className)

    // ── ExamRecord + QuestionScores ──

    suspend fun saveExamWithScores(
        student: Student,
        subject: String,
        totalScore: Double,
        imageUri: String,
        scores: List<QuestionScore>
    ): Long {
        val recordId = examRecordDao.insert(
            ExamRecord(
                studentId = student.id,
                subject = subject,
                totalScore = totalScore,
                imageUri = imageUri
            )
        )
        questionScoreDao.insertAll(scores.map { it.copy(id = 0, examRecordId = recordId) })
        return recordId
    }

    // ── Queries ──

    fun getRecordsBySubject(subject: String): Flow<List<ExamRecord>> =
        examRecordDao.getBySubject(subject)

    fun getRecordsByStudent(studentId: Long): Flow<List<ExamRecord>> =
        examRecordDao.getByStudentId(studentId)

    fun getAllRecords(): Flow<List<ExamRecord>> =
        examRecordDao.getAll()

    suspend fun getRecordWithScores(recordId: Long): Pair<ExamRecord, List<QuestionScore>>? {
        val record = examRecordDao.getById(recordId) ?: return null
        val scores = questionScoreDao.getByExamRecordOnce(recordId)
        return record to scores
    }

    // ── Deletion ──

    suspend fun deleteRecord(recordId: Long) {
        val record = examRecordDao.getById(recordId) ?: return
        questionScoreDao.deleteByExamRecord(recordId)
        examRecordDao.delete(record)
    }

    // ── Region Templates ──

    suspend fun saveTemplate(name: String, regionsJson: String): Long {
        val existing = regionTemplateDao.getByName(name)
        if (existing != null) throw IllegalStateException("Template \"$name\" already exists")
        return regionTemplateDao.insert(RegionTemplate(name = name, regionsJson = regionsJson))
    }

    suspend fun updateTemplate(id: Long, name: String, regionsJson: String) {
        val existing = regionTemplateDao.getByName(name)
        if (existing != null && existing.id != id)
            throw IllegalStateException("Template \"$name\" already exists")
        regionTemplateDao.update(RegionTemplate(id = id, name = name, regionsJson = regionsJson))
    }

    suspend fun getTemplateByName(name: String): RegionTemplate? =
        regionTemplateDao.getByName(name)

    fun getAllTemplates(): Flow<List<RegionTemplate>> =
        regionTemplateDao.getAll()

    suspend fun deleteTemplate(template: RegionTemplate) =
        regionTemplateDao.delete(template)
}
