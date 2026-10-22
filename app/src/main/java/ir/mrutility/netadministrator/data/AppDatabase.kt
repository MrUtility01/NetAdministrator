package ir.mrutility.netadministrator.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {
    @Query("SELECT * FROM chapters ORDER BY orderNum ASC, title ASC")
    fun observeChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY orderNum ASC, title ASC")
    suspend fun allChapters(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun chapter(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapter(chapter: ChapterEntity)

    @Query("SELECT * FROM content_items WHERE chapterId = :chapterId ORDER BY seq ASC, title ASC")
    fun observeChapterContent(chapterId: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE chapterId = :chapterId ORDER BY seq ASC, title ASC")
    suspend fun chapterContent(chapterId: String): List<ContentEntity>

    @Query("SELECT * FROM content_items WHERE kind = :kind ORDER BY chapterId, seq, title")
    fun observeContentKind(kind: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE kind = :kind ORDER BY chapterId, seq, title")
    suspend fun contentKind(kind: String): List<ContentEntity>

    @Query("SELECT * FROM content_items ORDER BY chapterId, kind, seq, title")
    suspend fun allContent(): List<ContentEntity>

    @Query("SELECT * FROM learning_tasks ORDER BY id")
    suspend fun allTasks(): List<TaskEntity>

    @Query("SELECT * FROM review_state ORDER BY uid")
    suspend fun allReviewStates(): List<ReviewStateEntity>

    @Query("SELECT * FROM study_sessions ORDER BY startedAt")
    suspend fun allStudySessions(): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC")
    fun observeStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM app_preferences ORDER BY `key`")
    suspend fun allPreferences(): List<PreferenceEntity>

    @Query("DELETE FROM learning_tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM review_state")
    suspend fun clearReviewStates()

    @Query("DELETE FROM study_sessions")
    suspend fun clearStudySessions()

    @Query("DELETE FROM app_preferences")
    suspend fun clearPreferences()

    @Query("DELETE FROM content_items WHERE chapterId = :chapterId")
    suspend fun deleteChapterItems(chapterId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ContentEntity>)

    @Transaction
    suspend fun replaceChapterContent(chapter: ChapterEntity, items: List<ContentEntity>) {
        upsertChapter(chapter)
        deleteChapterItems(chapter.id)
        upsertItems(items)
    }

    @Query("SELECT COUNT(*) FROM chapters")
    fun observeChapterCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM content_items")
    fun observeContentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Query("SELECT * FROM learning_tasks ORDER BY done ASC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, priority ASC, createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE learning_tasks SET done = :done WHERE id = :id")
    suspend fun setTaskDone(id: Long, done: Boolean)

    @Query("DELETE FROM learning_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("SELECT COUNT(*) FROM learning_tasks WHERE done = 0")
    fun observeOpenTaskCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(state: ReviewStateEntity)

    @Query("SELECT * FROM review_state WHERE uid = :uid LIMIT 1")
    suspend fun reviewState(uid: String): ReviewStateEntity?

    @Query("SELECT * FROM review_state")
    fun observeReviewStates(): Flow<List<ReviewStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStudySession(session: StudySessionEntity)

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM study_sessions WHERE startedAt >= :startOfDay")
    fun observeTodayStudySeconds(startOfDay: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: PreferenceEntity)

    @Query("SELECT value FROM app_preferences WHERE `key` = :key LIMIT 1")
    suspend fun preference(key: String): String?
}

@Database(
    entities = [ChapterEntity::class, ContentEntity::class, TaskEntity::class, ReviewStateEntity::class, StudySessionEntity::class, PreferenceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun learningDao(): LearningDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "net_administrator_android.db")
                .fallbackToDestructiveMigrationOnDowngrade()
                .build().also { INSTANCE = it }
        }
    }
}
