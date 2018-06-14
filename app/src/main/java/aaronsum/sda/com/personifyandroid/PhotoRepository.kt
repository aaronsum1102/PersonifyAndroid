package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import com.crashlytics.android.Crashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

data class PicMetadata(val url: String = "",
                       val orientation: String = "")

class PhotoRepository {
    companion object {
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
        document.addSnapshotListener { documentSnapshot, exception ->
            exception?.let {
                Crashlytics.logException(exception)
            }
            val profilePic = documentSnapshot?.toObject(PicMetadata::class.java)
            profilePic?.let {
                profilePhotoMetadata.postValue(profilePic)
            }
        }
    }

    fun writeUserProfilePictureURL(picMetadata: PicMetadata) {
        if (this::document.isInitialized) {
            document.set(picMetadata)
        }
    }

    fun uploadProfilePhoto(file: Uri): UploadTask? {
        if (this::document.isInitialized) {
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
        }
    }

    fun clearProfilePic() {
        profilePhotoMetadata.postValue(null)
    }
}