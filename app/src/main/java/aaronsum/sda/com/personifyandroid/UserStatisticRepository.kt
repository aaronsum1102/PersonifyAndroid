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
        const val TAG = "RepositoryUserStatistic"
        const val COMPLETION_ON_TIME = "completionOnTime"
        const val OVERDUE = "overdue"
        const val NEW_TASK = "newTask"
    }

    private val collectionName = "userStatistics"
    private val db = FirebaseFirestore.getInstance()
    private lateinit var document: DocumentReference
    val userStatistics: MutableLiveData<UserStatistics> = MutableLiveData()
    private var taskStatistic: TaskStatistic? = null

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initialiseCollection(userId: String) {
        document = db.collection(collectionName).document(userId)
        document.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.e(TAG, "Unable to add listener for task stat. ${it.localizedMessage}")
                return@addSnapshotListener
            }
            documentSnapshot?.let {
                taskStatistic = documentSnapshot.toObject(TaskStatistic::class.java)
                if (taskStatistic != null) {
                    userStatistics.postValue(transformData(taskStatistic!!))
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
        return UserStatistics(completionRate, overdueRate, taskStatistic.earliestCompletion, taskStatistic.longestOverdue)
    }

    fun updateStatistic(command: String) {
        if (taskStatistic == null) {
            taskStatistic = TaskStatistic()
        }
        taskStatistic?.let {
            when (command) {
                COMPLETION_ON_TIME -> {
                    it.taskCompletedOnTime += 1
                }
                OVERDUE -> {
                    it.taskOverdue += 1
                }
                NEW_TASK -> {
                    it.numberOfTasks += 1
                }
            }
            if (this::document.isInitialized) {
                document.set(taskStatistic as Any)
            }
        }
    }

    fun updateCompletionStatistic(newStatistic: UserCompletionStatistic) {
        var flagToUpdateDocument = false
        if (taskStatistic == null) {
            taskStatistic = TaskStatistic()
        }
        taskStatistic?.let {
            if (newStatistic.earliestCompletion > it.earliestCompletion) {
                flagToUpdateDocument = true
                it.earliestCompletion = newStatistic.earliestCompletion
            }
            if (newStatistic.longestOverdue < it.longestOverdue) {
                flagToUpdateDocument = true
                it.longestOverdue = newStatistic.longestOverdue
            }
            if (this::document.isInitialized && flagToUpdateDocument) {
                document.set(taskStatistic as Any)
            }
        }
    }

    fun deleteStatistic() {
        if (this::document.isInitialized) {
            document.delete()
            Log.i(TAG, "deleted user's stat.")
        }
    }

    fun clearStatistic() {
        userStatistics.postValue(UserStatistics())
    }
}

