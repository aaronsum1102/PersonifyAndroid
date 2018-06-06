package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

data class PicMetadata(val url: String = "",
                       val orientation: String = "")

class PhotoRepository {
    companion object {
        private const val TAG = "RepositoryPhoto"
        private const val COLLECTION_NAME = "userProfilePic"
    }

    private val db = FirebaseFirestore.getInstance()
    private val photoReference = FirebaseStorage.getInstance().getReference(COLLECTION_NAME)
    private val collection = db.collection(COLLECTION_NAME)
    private lateinit var document: DocumentReference

    val profilePhotoMetadata: MutableLiveData<PicMetadata> = MutableLiveData()

    init {
        db.firestoreSettings = Util.persistenceDBSetting
    }

    fun initDocument(userId: String) {
        document = collection.document(userId)
        document.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.i(TAG, "Unable to add snapshot listener for profile photo metadata. ${firebaseFirestoreException.localizedMessage}")
            }
            val profilePic = documentSnapshot?.toObject(PicMetadata::class.java)
            profilePic?.let {
                profilePhotoMetadata.postValue(profilePic)
                Log.i(TAG, "changes in profilePic metadata posted.")
            }
        }
    }

    fun writeUserProfilePictureURL(picMetadata: PicMetadata) {
        if (this::document.isInitialized) {
            document.set(picMetadata)
            Log.i(TAG, "persist profile pic url")
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
        profilePhotoMetadata.postValue(null)
    }
}