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
        private const val TAG = "TaskRepository"
        private const val COLLECTION_NAME = "tasks"
        private const val SUB_COLLECTION_NAME = "user tasks"
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var taskCollection: CollectionReference
    val tasks: MutableLiveData<List<Pair<String, Task>>> = MutableLiveData()

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initUserTaskDocument(userId: String) {
        initUserTaskCollection(userId)
        if (this::taskCollection.isInitialized) {
            taskCollection.addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    Log.w(TAG, "Failed to add event listener to DB. ${exception.message}")
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
        val document = change.document
        val taskFromDB = document.toObject(Task::class.java)
        val daysDifference = Util.getDaysDifference(taskFromDB.dueDate)
        if (daysDifference != taskFromDB.daysLeft && taskFromDB.status != "Done") {
            taskFromDB.daysLeft = daysDifference
        }
        temporaryTasks.add(document.id to taskFromDB)
        temporaryTasks.sortBy { it.second.daysLeft }
        tasks.value = temporaryTasks
        Log.i(TAG, "New document added. Number of tasks: ${tasks.value?.size}")
    }

    private fun onDocumentModified(change: DocumentChange) {
        val taskId = change.document.id
        val temporaryTasks = mutableListOf<Pair<String, Task>>()
        tasks.value?.let { taskList ->
            temporaryTasks.addAll(taskList)
            val taskPair = temporaryTasks.find { it.first == taskId }
            temporaryTasks.remove(taskPair)
            temporaryTasks.add(taskId to change.document.toObject(Task::class.java))
            temporaryTasks.sortBy { it.second.daysLeft }
            tasks.value = temporaryTasks
            Log.i(TAG, "Document has been modified. Number of tasks :${tasks.value?.size}")
        }
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        val temporaryTasks = tasks.value
        temporaryTasks?.let {
            temporaryTasks as MutableList<Pair<String, Task>>
            val taskToRemove = temporaryTasks.find { it.first == change.document.id }
            taskToRemove?.let {
                temporaryTasks.remove(taskToRemove)
                tasks.value = temporaryTasks
                Log.i(TAG, "Document has been deleted. Number of tasks :${tasks.value?.size}")
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
    }

    fun deleteUserDocument() {
        taskCollection.get()
                .addOnSuccessListener {
                    it.documents.forEach { it.reference.delete() }
                }
    }
}