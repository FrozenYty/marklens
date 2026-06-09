# API Specification — MarkLens

> **Authors**: Tianyu Yao, Jianheng Sun
> **Updated**: 2026-06-10
> **Dependencies**: AGP 9.2, Kotlin 2.3.21, Compose BOM 2026.05.00,
> Room 2.8.4, CameraX 1.5, ML Kit 16.0.1, Coil 2.7.0

---

## 1. Data Entities

### 1.1 Student

```kotlin
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentId: String,
    val className: String
)
```

### 1.2 ExamRecord

```kotlin
@Entity(
    tableName = "exam_records",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("studentId")]
)
data class ExamRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subject: String,
    val totalScore: Double,
    val imageUri: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 1.3 QuestionScore

```kotlin
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
```

### 1.4 RegionTemplate

```kotlin
@Entity(tableName = "region_templates")
data class RegionTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val regionsJson: String    // JSON array of OcrRegion
)
```

### 1.5 Entity Relationships

```
Student 1 ──── N ExamRecord 1 ──── N QuestionScore
RegionTemplate (standalone)
```

---

## 2. DAO Contracts

### 2.1 StudentDao

```kotlin
@Dao
interface StudentDao {
    suspend fun insert(student: Student): Long
    suspend fun update(student: Student)
    suspend fun delete(student: Student)
    suspend fun getAllOnce(): List<Student>
    suspend fun getById(id: Long): Student?
    suspend fun getByStudentId(studentId: String): Student?
    fun getByClass(className: String): Flow<List<Student>>
    fun getAll(): Flow<List<Student>>
}
```

### 2.2 ExamRecordDao

```kotlin
@Dao
interface ExamRecordDao {
    suspend fun insert(record: ExamRecord): Long
    suspend fun update(record: ExamRecord)
    suspend fun delete(record: ExamRecord)
    suspend fun getById(id: Long): ExamRecord?
    fun getBySubject(subject: String): Flow<List<ExamRecord>>
    fun getByStudentId(studentId: Long): Flow<List<ExamRecord>>
    fun getAll(): Flow<List<ExamRecord>>
}
```

### 2.3 QuestionScoreDao

```kotlin
@Dao
interface QuestionScoreDao {
    suspend fun insertAll(scores: List<QuestionScore>)
    suspend fun update(score: QuestionScore)
    fun getByExamRecord(recordId: Long): Flow<List<QuestionScore>>
    suspend fun getByExamRecordOnce(recordId: Long): List<QuestionScore>
    suspend fun deleteByExamRecord(recordId: Long)
}
```

### 2.4 RegionTemplateDao

```kotlin
@Dao
interface RegionTemplateDao {
    suspend fun insert(template: RegionTemplate): Long
    suspend fun update(template: RegionTemplate)
    fun getAll(): Flow<List<RegionTemplate>>
    suspend fun getByName(name: String): RegionTemplate?
    suspend fun delete(template: RegionTemplate)
}
```

---

## 3. Repository

```kotlin
class ExamRepository(
    private val studentDao: StudentDao,
    private val examRecordDao: ExamRecordDao,
    private val questionScoreDao: QuestionScoreDao,
    private val regionTemplateDao: RegionTemplateDao
) {
    suspend fun getStudentNameMap(): Map<Long, String>
    suspend fun getOrCreateStudent(name, studentId, className): Student
    fun getStudentsByClass(className): Flow<List<Student>>

    suspend fun saveExamWithScores(student, subject, totalScore, imageUri, scores): Long
    fun getRecordsBySubject(subject): Flow<List<ExamRecord>>
    fun getRecordsByStudent(studentId): Flow<List<ExamRecord>>
    fun getAllRecords(): Flow<List<ExamRecord>>
    suspend fun getRecordWithScores(recordId): Pair<ExamRecord, List<QuestionScore>>?
    suspend fun deleteRecord(recordId: Long)

    suspend fun saveTemplate(name, regionsJson): Long
    suspend fun updateTemplate(id, name, regionsJson)
    suspend fun getTemplateByName(name): RegionTemplate?
    fun getAllTemplates(): Flow<List<RegionTemplate>>
    suspend fun deleteTemplate(template: RegionTemplate)
}
```

---

## 4. OCR Module

### 4.1 OcrRegion

```kotlin
data class OcrRegion(
    val id: String = UUID.randomUUID().toString(),
    val label: RegionLabel,
    val rect: RectF,              // normalized [0, 1]
    val rawText: String = "",
    val parsedValue: String = ""
)

enum class RegionLabel(val displayName: String) {
    STUDENT_NAME("Name"),
    STUDENT_ID("Student ID"),
    CLASS_NAME("Class"),
    SUBJECT("Subject"),
    QUESTION_SCORE("Q Score"),
    TOTAL_SCORE("Total Score"),
    CUSTOM("Custom")
}
```

### 4.2 OcrProvider

```kotlin
interface OcrProvider {
    suspend fun recognizeRegion(image: Bitmap, region: RectF): String
    suspend fun recognizeBlocks(image: Bitmap): List<TextBlock>
    fun close()
}

class MlKitOcrProvider : OcrProvider { ... }
```

### 4.3 OcrEngine

```kotlin
class OcrEngine {
    suspend fun recognize(bitmap: Bitmap): Text
    suspend fun recognizeBlocks(bitmap: Bitmap): List<TextBlock>
    suspend fun recognizeRegion(bitmap: Bitmap, region: RectF): String
    fun close()
}
```

### 4.4 RegionMapper

```kotlin
data class TextBlock(val text: String, val boundingBox: RectF)

class RegionMapper {
    fun mapBlocksToRegions(blocks: List<TextBlock>, regions: List<OcrRegion>): List<OcrRegion>
}
```

---

## 5. Parsers

### 5.1 ScoreParser

```kotlin
class ScoreParser {
    fun parseScores(regions: List<OcrRegion>, examRecordId: Long): List<QuestionScore>
    fun parseTotalScore(regions: List<OcrRegion>): Double?
}
```

Supports two modes:
- **Multiple QUESTION_SCORE regions** — one per question, raw text like "8" or "8/10"
- **Single QUESTION_SCORE region** — table format, column-oriented OCR text
  (e.g., "Score 15 14 15 17 15 Max 20 20 20 20 20")

### 5.2 StudentInfoParser

```kotlin
data class ParsedStudentInfo(val name: String?, val studentId: String?, val className: String?)

class StudentInfoParser {
    fun parse(regions: List<OcrRegion>): ParsedStudentInfo

    companion object {
        fun cleanLabel(text: String?): String?  // strips "Label:" prefixes
    }
}
```

---

## 6. Navigation

```kotlin
sealed class Nav {
    data object Home : Nav()
    data object Editor : Nav()
    data object Templates : Nav()
    data object Records : Nav()
    data class Review(val imageUri: String) : Nav()
    data class Stats(val subject: String?) : Nav()
}
```

Navigation is stack-based (`navStack: List<Nav>`), with `push()` and `pop()`.

---

## 7. ViewModel Contracts

### 7.1 ReviewViewModel

```kotlin
data class ReviewUiState(
    val studentInfo: ParsedStudentInfo,
    val subject: String,
    val totalScore: String,
    val scores: List<ScoreField>,
    val isSaving: Boolean,
    val saveComplete: Boolean
)

data class ScoreField(val questionNumber: Int, val score: String, val maxScore: Double)

class ReviewViewModel(private val repository: ExamRepository?) : ViewModel() {
    val uiState: StateFlow<ReviewUiState>
    fun setParsedData(info, subject, totalScore, scores)
    fun updateName/updateStudentId/updateClassName/updateSubject/updateTotalScore(value)
    fun updateScore(questionNumber, value)
    fun save(imageUri: String)
}
```

### 7.2 RecordListViewModel

```kotlin
data class RecordListUiState(
    val records: List<ExamRecord>,
    val studentNames: Map<Long, String>,
    val selectedSubject: String?,
    val subjects: List<String>,
    val isLoading: Boolean
)

sealed interface RecordListEvent {
    data class SubjectSelected(val subject: String?) : RecordListEvent
    data class RecordClicked(val recordId: Long) : RecordListEvent
    data class RecordDeleted(val recordId: Long) : RecordListEvent
}

class RecordListViewModel(private val repository: ExamRepository?) : ViewModel() {
    val uiState: StateFlow<RecordListUiState>
    fun onEvent(event: RecordListEvent)
}
```

### 7.3 StatsViewModel

```kotlin
class StatsViewModel(private val repository: ExamRepository, subject: String?) : ViewModel()
```

---

## 8. Utilities

### 8.1 CsvExporter

```kotlin
object CsvExporter {
    fun export(records: List<ExamRecord>, allScores: Map<Long, List<QuestionScore>>): String
}
```

### 8.2 StatsCalculator

```kotlin
class StatsCalculator {
    fun calculate(records: List<ExamRecord>, allScores: Map<Long, List<QuestionScore>>): StatsResult
}
```

---

## 9. Data Flow

```
Home
 ├─ Scan Paper → pick template → gallery photo
 │      ↓
 │  ML Kit recognizeBlocks(fullPage)
 │      ↓
 │  RegionMapper.mapBlocksToRegions()
 │      ↓
 │  StudentInfoParser + ScoreParser
 │      ↓
 │  ReviewScreen (verify/correct) → save()
 │      ↓
 │  ExamRepository.saveExamWithScores()
 │      ↓
 │  Room Database
 │
 ├─ Templates → Editor (draw regions, label, save)
 │
 └─ Records → list / filter / delete / CSV export
        ↓
    Statistics (charts)
```
