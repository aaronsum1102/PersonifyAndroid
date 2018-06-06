package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class Task(var name: String = "",
                var dueDate: String = "",
                var status: String = "",
                var priority: String = "",
                var remarks: String = "",
                var daysLeft: Int = 0,
                var datesMarkedAsDone: String = "")

class TaskRepository {
    companion object {
        private const val TAG = "RepositoryTask"
        private const val COLLECTION_NAME = "tasks"
        private const val SUB_COLLECTION_NAME = "user tasks"
        private const val TASK_IS_DONE = "Done"
        private const val REMOVE_AFTER_NUMBER_OF_DAYS = -7
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var taskCollection: CollectionReference
    val tasks: MutableLiveData<List<Pair<String, Task>>> = MutableLiveData()
    val doneTasks: MutableLiveData<List<Pair<String, Task>>> = MutableLiveData()
    private lateinit var snapshotListener : ListenerRegistration

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initUserTaskDocument(userId: String) {
        initUserTaskCollection(userId)
        if (this::taskCollection.isInitialized) {
            snapshotListener = taskCollection.addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    Log.w(TAG, "Failed to add event listener to tasks collection. ${exception.message}")
                    return@addSnapshotListener
                }
                val source = if (documentSnapshot != null &&
                        documentSnapshot.metadata.hasPendingWrites()) "Local" else "Server"
                documentSnapshot?.documentChanges?.forEach { change ->
                    Log.i(TAG, "event source: $source")
                    when (change.type) {
                        DocumentChange.Type.ADDED -> onDocumentAdded(change)
                        DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                        DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                    }
                }
            }
        }
    }

    private fun initUserTaskCollection(userId: String) {
        taskCollection = db.collection(COLLECTION_NAME)
                .document(userId)
                .collection(SUB_COLLECTION_NAME)
    }

    private fun onDocumentAdded(change: DocumentChange) {
        val temporaryTasks = mutableListOf<Pair<String, Task>>()
        tasks.value?.let { temporaryTasks.addAll(it) }
        val temporaryDoneTasks = mutableListOf<Pair<String, Task>>()
        doneTasks.value?.let { temporaryDoneTasks.addAll(it) }

        val document = change.document
        val taskFromDB = document.toObject(Task::class.java)

        removedTaskAfterMarkedAsDone(taskFromDB, document.id)

        taskFromDB.daysLeft = Util.getDaysDifference(taskFromDB.dueDate)

        if (taskFromDB.status != TASK_IS_DONE) {
            temporaryTasks.add(document.id to taskFromDB)
        } else {
            temporaryDoneTasks.add(document.id to taskFromDB)
        }
        temporaryTasks.sortBy { it.second.daysLeft }
        temporaryDoneTasks.sortBy { it.second.daysLeft }
        tasks.value = temporaryTasks
        doneTasks.value = temporaryDoneTasks
    }

    private fun removedTaskAfterMarkedAsDone(task: Task, id: String) {
        if (task.status == TASK_IS_DONE) {
            if (task.datesMarkedAsDone.isEmpty()) {
                task.datesMarkedAsDone = Util.getCurrentDate()
            }
            val daysSinceMarkedAsDone = Util.getDaysDifference(task.datesMarkedAsDone)
            if (daysSinceMarkedAsDone <= REMOVE_AFTER_NUMBER_OF_DAYS && this::taskCollection.isInitialized) {
                taskCollection.document(id).delete()
                        .addOnFailureListener {
                            Log.e(TAG, "Failed to remove done task after days limit. ${it.localizedMessage}")
                        }
            }
        }
    }

    private fun onDocumentModified(change: DocumentChange) {
        val temporaryTasks = mutableListOf<Pair<String, Task>>()
        tasks.value?.let { temporaryTasks.addAll(it) }
        val temporaryDoneTasks = mutableListOf<Pair<String, Task>>()
        doneTasks.value?.let { temporaryDoneTasks.addAll(it) }

        val taskId = change.document.id
        val taskToRemove = temporaryTasks.find { it.first == taskId }
        taskToRemove?.let { temporaryTasks.remove(taskToRemove) }
        val taskToRemoveIfAny = temporaryDoneTasks.find { it.first == taskId }
        taskToRemoveIfAny?.let { temporaryDoneTasks.remove(taskToRemoveIfAny) }

        val taskChanged = change.document.toObject(Task::class.java)
        if (taskChanged.status != TASK_IS_DONE) {
            temporaryTasks.add(taskId to taskChanged)
        } else {
            temporaryDoneTasks.add(taskId to taskChanged)
        }

        temporaryTasks.sortBy { it.second.daysLeft }
        temporaryDoneTasks.sortBy { it.second.daysLeft }
        tasks.value = temporaryTasks
        doneTasks.value = temporaryDoneTasks
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        val temporaryTasks = tasks.value
        val temporaryDoneTasks = doneTasks.value

        temporaryTasks?.let {
            temporaryTasks as MutableList<Pair<String, Task>>
            val taskToRemove = temporaryTasks.find { it.first == change.document.id }
            taskToRemove?.let {
                temporaryTasks.remove(taskToRemove)
                tasks.value = temporaryTasks
            }
        }

        temporaryDoneTasks?.let {
            temporaryDoneTasks as MutableList<Pair<String, Task>>
            val taskToRemove = temporaryDoneTasks.find { it.first == change.document.id }
            taskToRemove?.let {
                temporaryDoneTasks.remove(taskToRemove)
                doneTasks.value = temporaryDoneTasks
            }
        }
    }

    fun addTask(task: Task) = taskCollection.document().set(task)

    fun modifyTask(pair: Pair<String, Task>) = taskCollection.document(pair.first).set(pair.second)

    fun deleteTask(id: String) {
        taskCollection.document(id).delete()
    }

    fun deleteUserDocument() {
        clearTask()
        taskCollection.get()
                .addOnSuccessListener {
                    it.documents.forEach { it.reference.delete() }
                }
        Log.i(TAG, "deleted user's tasks data")
    }

    fun clearTask() {
        if (this::snapshotListener.isInitialized) {
            snapshotListener.remove()
        }
        tasks.postValue(null)
        doneTasks.postValue(null)
    }
}