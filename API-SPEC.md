# API Specification — MarkLens

> Contracts between modules. All collaborators MUST follow these signatures.
> Breaking changes require updating this document, tests, and all callers.
>
> **Authors**: Tianyu Yao, Jianheng Sun
> **Updated**: 2026-06-08
> **Dependencies verified** (Context7): AGP 9.2, Kotlin 2.3.21, Compose BOM 2026.05.00,
> Room 2.8.4, CameraX 1.5 + camera-compose, ML Kit 16.0.1

---

## 1. Data Entities

### 1.1 Student

```kotlin
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentId: String,      // 学号
    val className: String        // 班级
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
    )]
)
data class ExamRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subject: String,
    val totalScore: Double,
    val imageUri: String,         // 原始试卷图片路径
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
    )]
)
data class QuestionScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examRecordId: Long,
    val questionNumber: Int,
    val score: Double,
    val maxScore: Double,
    val isWrong: Boolean           // 是否做错（得分 < 满分）
)
```

### 1.4 RegionTemplate

```kotlin
@Entity(tableName = "region_templates")
data class RegionTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,              // 模板名称 e.g. "期中数学卷"
    val regionsJson: String        // JSON: List<OcrRegion>
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
    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getByStudentId(studentId: String): Student?

    @Query("SELECT * FROM students WHERE className = :className ORDER BY studentId")
    fun getByClass(className: String): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY studentId")
    fun getAll(): Flow<List<Student>>
}
```

### 2.2 ExamRecordDao

```kotlin
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
```

### 2.3 QuestionScoreDao

```kotlin
@Dao
interface QuestionScoreDao {
    @Insert
    suspend fun insertAll(scores: List<QuestionScore>)

    @Update
    suspend fun update(score: QuestionScore)

    @Query("SELECT * FROM question_scores WHERE examRecordId = :recordId ORDER BY questionNumber")
    fun getByExamRecord(recordId: Long): Flow<List<QuestionScore>>

    @Query("DELETE FROM question_scores WHERE examRecordId = :recordId")
    suspend fun deleteByExamRecord(recordId: Long)
}
```

### 2.4 RegionTemplateDao

```kotlin
@Dao
interface RegionTemplateDao {
    @Insert
    suspend fun insert(template: RegionTemplate): Long

    @Query("SELECT * FROM region_templates ORDER BY name")
    fun getAll(): Flow<List<RegionTemplate>>

    @Delete
    suspend fun delete(template: RegionTemplate)
}
```

---

## 3. Repository Contracts

### 3.1 ExamRepository

```kotlin
class ExamRepository(
    private val studentDao: StudentDao,
    private val examRecordDao: ExamRecordDao,
    private val questionScoreDao: QuestionScoreDao,
    private val regionTemplateDao: RegionTemplateDao
) {
    // --- Student ---
    suspend fun getOrCreateStudent(name: String, studentId: String, className: String): Student
    fun getStudentsByClass(className: String): Flow<List<Student>>

    // --- ExamRecord + QuestionScores (atomic) ---
    suspend fun saveExamWithScores(
        student: Student,
        subject: String,
        totalScore: Double,
        imageUri: String,
        scores: List<QuestionScore>
    ): Long  // returns examRecordId

    // --- Queries ---
    fun getRecordsBySubject(subject: String): Flow<List<ExamRecord>>
    fun getRecordsByStudent(studentId: Long): Flow<List<ExamRecord>>
    fun getAllRecords(): Flow<List<ExamRecord>>
    suspend fun getRecordWithScores(recordId: Long): Pair<ExamRecord, List<QuestionScore>>?

    // --- Deletion ---
    suspend fun deleteRecord(recordId: Long)

    // --- Region Templates ---
    suspend fun saveTemplate(name: String, regions: List<OcrRegion>): Long
    fun getAllTemplates(): Flow<List<RegionTemplate>>
    suspend fun deleteTemplate(template: RegionTemplate)
}
```

---

## 4. OCR Module Contracts

### 4.1 OcrRegion

```kotlin
data class OcrRegion(
    val id: String = UUID.randomUUID().toString(),
    val label: RegionLabel,
    val rect: RectF,              // 归一化坐标 [0,1]
    val rawText: String = "",     // OCR result (filled after recognition)
    val parsedValue: String = ""  // parsed user-facing value (filled after correction)
)

enum class RegionLabel(val displayName: String) {
    STUDENT_NAME("姓名"),
    STUDENT_ID("学号"),
    CLASS_NAME("班级"),
    SUBJECT("科目"),
    QUESTION_SCORE("题目得分"),
    TOTAL_SCORE("总分"),
    CUSTOM("自定义")
}
```

### 4.2 OcrEngine

```kotlin
class OcrEngine(private val recognizer: TextRecognizer) {
    suspend fun recognize(bitmap: Bitmap): Text
    suspend fun recognizeRegion(bitmap: Bitmap, region: RectF): String
}
```

### 4.3 RegionMapper

```kotlin
class RegionMapper {
    /**
     * Maps OCR Text blocks to OcrRegion list by spatial proximity.
     * @param ocrText Full OCR result with block-level bounding boxes
     * @param regions User-defined regions with rect coordinates
     * @return Updated regions with rawText filled
     */
    fun mapBlocksToRegions(ocrText: Text, regions: List<OcrRegion>): List<OcrRegion>
}
```

---

## 5. Parser Contracts

### 5.1 ScoreParser

```kotlin
class ScoreParser {
    /**
     * Parses a list of OcrRegion into a list of QuestionScore.
     * Extracts numeric scores from QUESTION_SCORE regions.
     * @param regions Regions with filled rawText
     * @param examRecordId The parent exam record ID
     * @return Parsed QuestionScore list
     */
    fun parseScores(regions: List<OcrRegion>, examRecordId: Long): List<QuestionScore>

    /**
     * Parses total score from TOTAL_SCORE region.
     * @return Parsed total score, or null if unparseable
     */
    fun parseTotalScore(regions: List<OcrRegion>): Double?
}
```

### 5.2 StudentInfoParser

```kotlin
class StudentInfoParser {
    /**
     * Extracts student info from OCR regions.
     * @return Triple(name, studentId, className) or null for each unparseable field
     */
    fun parse(regions: List<OcrRegion>): ParsedStudentInfo
}

data class ParsedStudentInfo(
    val name: String?,
    val studentId: String?,
    val className: String?
)
```

---

## 6. ViewModel UiState Contracts

### 6.1 CaptureScreen

```kotlin
data class CaptureUiState(
    val capturedBitmap: Bitmap? = null,
    val regions: List<OcrRegion> = emptyList(),
    val selectedRegionId: String? = null,
    val editingLabel: RegionLabel? = null,
    val templates: List<RegionTemplate> = emptyList()
)

sealed interface CaptureEvent {
    data class PhotoCaptured(val bitmap: Bitmap) : CaptureEvent
    data class PhotoSelected(val uri: Uri) : CaptureEvent
    data class RegionAdded(val region: OcrRegion) : CaptureEvent
    data class RegionMoved(val regionId: String, val newRect: RectF) : CaptureEvent
    data class RegionDeleted(val regionId: String) : CaptureEvent
    data class RegionLabelChanged(val regionId: String, val label: RegionLabel) : CaptureEvent
    data class TemplateSaved(val name: String) : CaptureEvent
    data class TemplateLoaded(val template: RegionTemplate) : CaptureEvent
    data object StartRecognition : CaptureEvent
}
```

### 6.2 ReviewScreen

```kotlin
data class ReviewUiState(
    val studentInfo: ParsedStudentInfo = ParsedStudentInfo(null, null, null),
    val subject: String = "",
    val totalScore: String = "",
    val scores: List<ScoreField> = emptyList(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

data class ScoreField(
    val questionNumber: Int,
    val score: String,
    val maxScore: Double,
    val regionImage: Bitmap?         // 裁剪后的区域图，辅助校对
)

sealed interface ReviewEvent {
    data class NameChanged(val value: String) : ReviewEvent
    data class StudentIdChanged(val value: String) : ReviewEvent
    data class ClassNameChanged(val value: String) : ReviewEvent
    data class SubjectChanged(val value: String) : ReviewEvent
    data class TotalScoreChanged(val value: String) : ReviewEvent
    data class ScoreChanged(val questionNumber: Int, val value: String) : ReviewEvent
    data object Save : ReviewEvent
}
```

### 6.3 RecordListScreen

```kotlin
data class RecordListUiState(
    val records: List<ExamRecord> = emptyList(),
    val selectedSubject: String? = null,
    val subjects: List<String> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface RecordListEvent {
    data class SubjectSelected(val subject: String?) : RecordListEvent
    data class RecordClicked(val recordId: Long) : RecordListEvent
    data class RecordDeleted(val recordId: Long) : RecordListEvent
}
```

### 6.4 StatsScreen

```kotlin
data class StatsUiState(
    val subject: String,
    val totalRecords: Int = 0,
    val averageScore: Double = 0.0,
    val maxScore: Double = 0.0,
    val minScore: Double = 0.0,
    val passRate: Double = 0.0,       // 0.0–1.0
    val scoreDistribution: Map<String, Int> = emptyMap(),   // "90-100" → count
    val perQuestionStats: List<QuestionStat> = emptyList()
)

data class QuestionStat(
    val questionNumber: Int,
    val maxScore: Double,
    val averageScore: Double,
    val errorRate: Double,            // 0.0–1.0, 做错比例
    val totalAttempts: Int
)
```

---

## 7. StatsCalculator Contract

```kotlin
class StatsCalculator {
    fun calculate(
        records: List<ExamRecord>,
        allScores: Map<Long, List<QuestionScore>>  // recordId → scores
    ): StatsUiState

    fun scoreDistribution(scores: List<Double>): Map<String, Int>
    fun perQuestionStats(allScores: Map<Long, List<QuestionScore>>): List<QuestionStat>
}
```

---

## 8. Navigation Routes

```kotlin
sealed class Route(val path: String) {
    data object Capture : Route("capture")
    data object Review : Route("review")
    data object RecordList : Route("records")
    data class RecordDetail(val recordId: Long) : Route("records/{recordId}")
    data class Stats(val subject: String) : Route("stats/{subject}")
}
```

---

## 9. Data Flow

```
CaptureScreen              ReviewScreen              RecordListScreen
    │                           │                         │
    ▼                           ▼                         ▼
CaptureViewModel           ReviewViewModel           RecordListViewModel
    │                           │                         │
    │  OcrEngine.recognize()    │  ScoreParser.parse()    │
    │  RegionMapper.map()       │  StudentInfoParser      │
    │                           │                         │
    │                           ▼                         │
    │                    ExamRepository              ExamRepository
    │                    .saveExamWithScores()       .getAllRecords()
    │                           │                         │
    └───────────────────────────┴─────────────────────────┘
                                    │
                                    ▼
                              Room Database
                        (students, exam_records,
                         question_scores, region_templates)
```

---

## 10. Breaking Change Policy

1. Entity field changes → require migration (Room `Migration`)
2. DAO signature changes → update all Repository impls + tests
3. UiState field changes → update ViewModel + UI test
4. Update this document **before** committing the implementation
5. Add `@Deprecated` annotation for one version before removing old APIs
