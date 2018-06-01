package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

data class TaskStatistic(var taskCompletedOnTime: Int = 0,
                         var taskOverdue: Int = 0,
                         var earliestCompletion: Int = 0,
                         var longestOverdue: Int = 0,
                         var numberOfTasks: Int = 0)

data class UserStatistics(val taskCompletionRate: Int = 0,
                          val taskOverdueRate: Int = 0,
                          val earliestCompletion: Int = 0,
                          val longestOverdue: Int = 0)

data class UserCompletionStatistic(var earliestCompletion: Int,
                                   var longestOverdue: Int)

class UserStatisticRepository {
    companion object {
        const val TAG = "UserStatisticRepository"
        const val COMPLETION_ON_TIME = "completionOnTime"
        const val OVERDUE = "overdue"
        const val NEW_TASK = "newTask"
        const val UPDATE_COMPLETION_STATISTICS = "updateCompletionStatistics"
    }

    private val collectionName = "userStatistics"
    private val db = FirebaseFirestore.getInstance()
    private lateinit var document: DocumentReference
    val userStatistics: MutableLiveData<UserStatistics> = MutableLiveData()
    private var taskStatistic: TaskStatistic? = null

    init {
        db.firestoreSettings = Util.setupDBForPersistence(db)
    }

    private fun initialiseCollection(userId: String) {
        document = db.collection(collectionName).document(userId)

    }

    fun loadStatistic(userId: String) {
        initialiseCollection(userId)
        Log.i(TAG, "loadStatistic")
        if (this::document.isInitialized) {
            Log.i(TAG, "document was initialised")
            document.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    taskStatistic = task.result.toObject(TaskStatistic::class.java)
                    if (taskStatistic != null) {
                        Log.i(TAG, "Statistic loaded from db")
                        userStatistics.postValue(transformData(taskStatistic!!))
                    } else {
                        Log.i(TAG, "initialise document in db")
                        userStatistics.postValue(UserStatistics())
                    }
                } else {
                    Log.i(TAG, "${task.exception?.cause}")
                }
            }
        }
    }

    private fun transformData(taskStatistic: TaskStatistic): UserStatistics {
        val numberOfTasks = taskStatistic.numberOfTasks
        var completionRate = 0
        var overdueRate = 0
        if (numberOfTasks > 0) {
            completionRate = taskStatistic.taskCompletedOnTime * 100 / numberOfTasks
            overdueRate = taskStatistic.taskOverdue * 100 / numberOfTasks
        }
        Log.i(TAG, "transform data. completion rate : $completionRate, overdue rate : $overdueRate")
        return UserStatistics(completionRate, overdueRate, taskStatistic.earliestCompletion, taskStatistic.longestOverdue)
    }

    fun updateStatistic(command: String) {
        taskStatistic?.let {
            when (command) {
                COMPLETION_ON_TIME -> {
                    Log.i(TAG, "task completed on time")
                    it.taskCompletedOnTime += 1
                }
                OVERDUE -> {
                    Log.i(TAG, "task overdue")
                    it.taskOverdue += 1
                }
                NEW_TASK -> {
                    Log.i(TAG, "new task added")
                    it.numberOfTasks += 1
                }
            }
            if (this::document.isInitialized) {
                document.set(taskStatistic as Any)
                        .addOnSuccessListener {
                            userStatistics.postValue(transformData(taskStatistic!!))
                        }
            }
        }
    }

    fun updateCompletionStatistic(newStatistic: UserCompletionStatistic) {
        taskStatistic?.let {
            Log.i(TAG, "update completion record")
            if (newStatistic.earliestCompletion > it.earliestCompletion) {
                it.earliestCompletion = newStatistic.earliestCompletion
            }
            if (newStatistic.longestOverdue < it.longestOverdue) {
                it.longestOverdue = newStatistic.longestOverdue
            }
            if (this::document.isInitialized) {
                document.set(taskStatistic as Any)
                        .addOnSuccessListener {
                            userStatistics.postValue(transformData(taskStatistic!!))
                        }
            }
        }
    }
}

