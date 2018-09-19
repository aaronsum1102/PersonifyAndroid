package aaronsum.sda.com.personifyandroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository()
    val tasks: LiveData<List<Pair<String, Task>>> = repository.tasks
    val doneTasks: LiveData<List<Pair<String, Task>>> = repository.doneTasks

    fun initUserTaskDocument(userId: String) {
        repository.initUserTaskDocument(userId)
    }

    fun addTask(task: Task) = repository.addTask(task)

    fun modifyTask(pair: Pair<String, Task>) = repository.modifyTask(pair)

    fun deleteTask(taskId: String) = repository.deleteTask(taskId)

    fun deleteUserDocument() {
        repository.deleteUserDocument()
    }

    fun clearTaskWhenNoUserInSession() = repository.clearTask()
}