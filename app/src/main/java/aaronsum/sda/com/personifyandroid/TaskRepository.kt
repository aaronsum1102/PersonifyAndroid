package aaronsum.sda.com.personifyandroid

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Entity
data class Task(@PrimaryKey(autoGenerate = true) val id: Int,
                val name: String,
                @ColumnInfo(name = "due_date") val dueDate: String,
                val status: String,
                val priority: String,
                val remarks: String)

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task ORDER BY name")
    fun loadAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE id = :taskId")
    fun loadTask(taskId: Int): LiveData<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTask(task: Task)

    @Delete
    fun deleteTask(task: Task)
}

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

class TaskRepository(application: Application) {
    private val taskDao: TaskDao

    init {
        val database = Room
                .databaseBuilder(application
                        , TaskDatabase::class.java
                        , "task-database")
                .build()
        taskDao = database.taskDao()
    }

    fun loadTasks(): LiveData<List<Task>> = taskDao.loadAllTasks()

    fun loadTask(taskId: Int): LiveData<Task> = taskDao.loadTask(taskId)

    fun saveTask(task: Task) {
        taskDao.saveTask(task)
    }

    fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}