package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.*


data class Task(var name: String = "",
                var dueDate: String = "",
                var status: String = "",
                var priority: String = "",
                var remarks: String = "")

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
        setupDBForPersistence(db)
    }

    private fun setupDBForPersistence(db: FirebaseFirestore) {
        val dbSetting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = dbSetting
    }

    fun loadAllTasks(): com.google.android.gms.tasks.Task<QuerySnapshot>? {
        var task: com.google.android.gms.tasks.Task<QuerySnapshot>? = null
        if (this::taskCollection.isInitialized) {
            task = taskCollection.get()
            task.addOnCompleteListener {
                if (task.isSuccessful) {
                    val tasks = mutableListOf<Pair<String, Task>>()
                    task.result.forEach { document ->
                        tasks.add(document.id to document.toObject(Task::class.java))
                    }
                    this.tasks.postValue(tasks)
                    Log.i(TAG, "task loaded from DB. Number of tasks: ${tasks.size}")
                } else {
                    Log.w(TAG, "error getting document. ${task.exception?.message}")
                }
            }
        }
        return task
    }

    fun addEventListenerToDB(userId: String) {
        initUserTaskCollectionPath(userId)
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

    private fun initUserTaskCollectionPath(userId: String) {
        taskCollection = db.collection(COLLECTION_NAME)
                .document(userId)
                .collection(SUB_COLLECTION_NAME)
    }

    private fun onDocumentAdded(change: DocumentChange) {
        val temporaryTasks = mutableListOf<Pair<String, Task>>()
        tasks.value?.let { temporaryTasks.addAll(it) }
        val document = change.document
        temporaryTasks.add(document.id to document.toObject(Task::class.java))
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
        Log.d("delete", "user id $id")
        taskCollection.document(id).delete()
    }

    fun deleteUserDocument() {
        taskCollection.get()
                .addOnSuccessListener {
                    it.documents.forEach { it.reference.delete() }
                }
    }
}