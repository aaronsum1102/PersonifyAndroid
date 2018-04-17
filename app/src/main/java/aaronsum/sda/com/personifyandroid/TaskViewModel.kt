package aaronsum.sda.com.personifyandroid

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    val repository = TaskRepository()
    val tasks: LiveData<List<Task>> = repository.tasks

    fun addTask(task: Task) {
        repository.addTask(task)
    }

    fun removeTask(task: Task) {
        repository.removeTask(task)
    }
}