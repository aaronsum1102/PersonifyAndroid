package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class PhotoRepository {
    private val collectionName = "userProfilePic"
    private val dataName = "url"
    private val firebaseStore = FirebaseFirestore.getInstance()

    private val photoReference = FirebaseStorage.getInstance().getReference(collectionName)
    val profilePhoto: MutableLiveData<Uri> = MutableLiveData()

    init {
        setupDBForPersistence(firebaseStore)
    }

    private fun setupDBForPersistence(db: FirebaseFirestore) {
        val dbSetting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = dbSetting
    }

    fun writeUserProfilePictureURL(url: Uri, userId: String) {
        profilePhoto.postValue(url)
        val dataMap: Map<String, String> = mutableMapOf()
        dataMap as MutableMap
        dataMap[dataName] = url.toString()
        val documentReference = firebaseStore.collection(collectionName).document(userId)
        documentReference.set(dataMap)
    }

    fun uploadProfilePhoto(file: Uri, userId: String): UploadTask {
        val reference = photoReference.child("$userId.jpg")
        return reference.putFile(file)
    }

}