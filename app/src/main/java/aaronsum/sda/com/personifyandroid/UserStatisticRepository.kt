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

class UserStatisticRepository {
    companion object {
        const val TAG = "UserStatisticRepository"
        const val COMPLETION_ON_TIME = "completionOnTime"
        const val OVERDUE = "overdue"
        const val NEW_TASK = "newTask"
    }

    private val collectionName = "userStatistics"
    private val db = FirebaseFirestore.getInstance()
    private lateinit var document: DocumentReference
    val userStatistics: MutableLiveData<UserStatistics> = MutableLiveData()

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
                    val statistic = task.result.toObject(TaskStatistic::class.java)
                    if (statistic != null) {
                        Log.i(TAG, "Statistic loaded from db")
                        userStatistics.postValue(transformData(statistic))
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

    fun incrementTaskNumber(command: String) {
        document.get()
                .addOnSuccessListener {
                    val statistic = it.toObject(TaskStatistic::class.java)
                    statistic?.let {
                        when (command) {
                            COMPLETION_ON_TIME -> {
                                Log.i(TAG, "taskCompletion on time")
                                it.taskCompletedOnTime += 1
                            }
                            OVERDUE -> {
                                Log.i(TAG, "task overdue")
                                it.taskOverdue += 1
                            }
                            NEW_TASK -> {
                                Log.i(TAG, "number of task")
                                it.numberOfTasks += 1
                            }
                        }
                        if (this::document.isInitialized) {
                            document.set(statistic as Any)
                                    .addOnSuccessListener {
                                        userStatistics.postValue(transformData(statistic))
                                    }
                        }
                    }
                }
    }
}