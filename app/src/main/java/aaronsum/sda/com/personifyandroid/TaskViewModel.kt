package aaronsum.sda.com.personifyandroid

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.reactivex.Single

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    val tasks: LiveData<List<Task>> = repository.loadTasks()

    fun addTask(task: Task): Single<Unit> {
        return Single.fromCallable { repository.saveTask(task) }
    }

    fun loadTask(taskId: Int): LiveData<Task> = repository.loadTask(taskId)

    fun deleteTask(task: Task): Single<Unit> {
        return Single.fromCallable { repository.deleteTask(task) }
    }
}