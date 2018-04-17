package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData

data class Task(val name: String,
                val dueDate: String,
                val status: String,
                val priority: String,
                val remarks: String)

class TaskRepository {
    val tasks: MutableLiveData<List<Task>> = MutableLiveData()

    init {
        tasks.postValue(mutableListOf())
    }

    fun addTask(task: Task) {
        val taskList = tasks.value
        taskList as MutableList
        taskList.add(task)
    }

    fun removeTask(task: Task) {
        val taskList = tasks.value
        taskList as MutableList
        val index = taskList.indexOf(task)
        taskList.removeAt(index)
    }
}