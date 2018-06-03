package aaronsum.sda.com.personifyandroid

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository()
    val tasks: LiveData<List<Pair<String, Task>>> = repository.tasks

    fun initUserTaskDocument(userId: String) {
        repository.initUserTaskDocument(userId)
    }

    fun addTask(task: Task) = repository.addTask(task)

    fun modifyTask(pair: Pair<String, Task>) = repository.modifyTask(pair)

    fun loadTask(taskId: String): LiveData<Task> = repository.loadTask(taskId)

    fun deleteTask(taskId: String) = repository.deleteTask(taskId)

    fun deleteUserDocument() {
        repository.deleteUserDocument()
        clearTaskWhenNoUserInSession()
    }

    fun clearTaskWhenNoUserInSession() = repository.clearTask()
}