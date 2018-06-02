package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class PhotoRepository {
    companion object {
        private const val TAG = "PhotoRepository"
        private const val COLLECTION_NAME = "userProfilePic"
        private const val URL = "url"
    }

    private val db = FirebaseFirestore.getInstance()
    private val photoReference = FirebaseStorage.getInstance().getReference(COLLECTION_NAME)
    private val collection = db.collection(COLLECTION_NAME)
    private lateinit var document: DocumentReference

    val profilePhoto: MutableLiveData<String> = MutableLiveData()

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initDocument(userId: String) {
        document = collection.document(userId)
        document.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.i(TAG, "Unable to add snapshot listener. ${firebaseFirestoreException.localizedMessage}")
            }
            val url = documentSnapshot?.get(URL) as String?
            url?.let { profilePhoto.postValue(url) }
        }
    }

    fun writeUserProfilePictureURL(url: Uri) {
        profilePhoto.postValue(url.toString())
        val dataMap: Map<String, String> = mutableMapOf()
        dataMap as MutableMap
        dataMap[URL] = url.toString()
        if (this::document.isInitialized) {
            document.set(dataMap)
            Log.i(TAG, "record profile pic url")
        }
    }

    fun uploadProfilePhoto(file: Uri): UploadTask? {
        if (this::document.isInitialized) {
            Log.i(TAG, "upload profile pic")
            val fileName = "${document.id}.jpg"
            val reference = photoReference.child(fileName)
            return reference.putFile(file)
        }
        return null
    }

    fun deleteUserProfilePic() {
        if (this::document.isInitialized) {
            document.delete()
            photoReference.child("${document.id}.jpg").delete()
            Log.i(TAG, "delete user profile pic when deleting account")
        }
    }

    fun clearProfilePic() {
        Log.i(TAG, "clear profile pic after log out")
        profilePhoto.postValue(null)
    }
}