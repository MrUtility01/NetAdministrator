package ir.mrutility.netadministrator.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val orderNum: Int,
    val contentHash: String,
    val itemCount: Int,
    val downloadedAt: Long
)

@Entity(
    tableName = "content_items",
    indices = [Index("chapterId"), Index("kind"), Index("seq")]
)
data class ContentEntity(
    @PrimaryKey val uid: String,
    val chapterId: String,
    val kind: String,
    val title: String,
    val summary: String,
    val content: String,
    val sourceStatus: String,
    val metadataJson: String,
    val fullJson: String,
    val seq: Int
)

@Entity(tableName = "learning_tasks", indices = [Index("dueAt"), Index("done")])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAt: Long? = null,
    val priority: Int = 2,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "review_state", indices = [Index("dueAt")])
data class ReviewStateEntity(
    @PrimaryKey val uid: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val ease: Double = 2.5,
    val dueAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long = 0,
    val lastRating: String = "new"
)

@Entity(tableName = "study_sessions", indices = [Index("startedAt")])
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationSeconds: Long,
    val chapterId: String?,
    val activity: String
)

@Entity(tableName = "app_preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)
