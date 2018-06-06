package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

data class Task(var name: String = "",
                var dueDate: String = "",
                var status: String = "",
                var priority: String = "",
                var remarks: String = "",
                var daysLeft: Int = 0)

class TaskRepository {
    companion object {
        private const val TAG = "RepositoryTask"
        private const val COLLECTION_NAME = "tasks"
        private const val SUB_COLLECTION_NAME = "user tasks"
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var taskCollection: CollectionReference
    val tasks: MutableLiveData<List<Pair<String, Task>>> = MutableLiveData()
    val doneTasks: MutableLiveData<List<Pair<String, Task>>> = MutableLiveData()

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initUserTaskDocument(userId: String) {
        initUserTaskCollection(userId)
        if (this::taskCollection.isInitialized) {
            taskCollection.addSnapshotListener { documentSnapshot, exception ->
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
        val daysDifference = Util.getDaysDifference(taskFromDB.dueDate)
        if (daysDifference != taskFromDB.daysLeft && taskFromDB.status != "Done") {
            taskFromDB.daysLeft = daysDifference
        }
        if (taskFromDB.status != "Done") {
            temporaryTasks.add(document.id to taskFromDB)
        } else {
            temporaryDoneTasks.add(document.id to taskFromDB)
        }
        temporaryTasks.sortBy { it.second.daysLeft }
        temporaryDoneTasks.sortBy { it.second.daysLeft }
        tasks.value = temporaryTasks
        doneTasks.value = temporaryDoneTasks
        Log.i(TAG, "New document added. Number of tasks to be done: ${tasks.value?.size}")
        Log.i(TAG, "New document added. Number of tasks done: ${tasks.value?.size}")
    }

    private fun onDocumentModified(change: DocumentChange) {
        val taskId = change.document.id
        val temporaryTasks = mutableListOf<Pair<String, Task>>()
        tasks.value?.let { temporaryTasks.addAll(it) }
        val temporaryDoneTasks = mutableListOf<Pair<String, Task>>()
        doneTasks.value?.let { temporaryDoneTasks.addAll(it) }

        val taskToRemove = temporaryTasks.find { it.first == taskId }
        taskToRemove?.let { temporaryTasks.remove(taskToRemove) }
        val taskToRemoveIfAny = temporaryDoneTasks.find { it.first == taskId }
        taskToRemoveIfAny?.let { temporaryDoneTasks.remove(taskToRemoveIfAny) }

        val taskChanged = change.document.toObject(Task::class.java)
        if (taskChanged.status != "Done") {
            temporaryTasks.add(taskId to taskChanged)
        } else {
            temporaryDoneTasks.add(taskId to taskChanged)
        }

        temporaryTasks.sortBy { it.second.daysLeft }
        temporaryDoneTasks.sortBy { it.second.daysLeft }
        tasks.value = temporaryTasks
        doneTasks.value = temporaryDoneTasks
        Log.i(TAG, "Document has been modified. Number of tasks to be done :${tasks.value?.size}")
        Log.i(TAG, "Document has been modified. Number of tasks done:${doneTasks.value?.size}")
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
                Log.i(TAG, "Document has been deleted. Number of tasks :${tasks.value?.size}")
            }
        }

        temporaryDoneTasks?.let {
            temporaryDoneTasks as MutableList<Pair<String, Task>>
            val taskToRemove = temporaryDoneTasks.find { it.first == change.document.id }
            taskToRemove?.let {
                temporaryDoneTasks.remove(taskToRemove)
                doneTasks.value = temporaryDoneTasks
                Log.i(TAG, "Document has been deleted. Number of tasks :${doneTasks.value?.size}")
            }
        }
    }

    fun addTask(task: Task) = taskCollection.document().set(task)

    fun modifyTask(pair: Pair<String, Task>) = taskCollection.document(pair.first).set(pair.second)

    fun loadTask(id: String): MutableLiveData<Task> {
        val taskLiveData: MutableLiveData<Task> = MutableLiveData()
        taskCollection.document(id).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        taskLiveData.postValue(task.result.toObject(Task::class.java))
                    } else {
                        Log.w(TAG, "error getting specific document. ${task.exception?.message}")
                    }
                }
        return taskLiveData
    }

    fun deleteTask(id: String) {
        taskCollection.document(id).delete()
        Log.i(TAG, "specific task deleted")
    }

    fun deleteUserDocument() {
        taskCollection.get()
                .addOnSuccessListener {
                    it.documents.forEach { it.reference.delete() }
                }
        Log.i(TAG, "deleted user's tasks data")
    }

    fun clearTask() {
        tasks.postValue(null)
        doneTasks.postValue(null)
    }
}