package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class PhotoRepository {
    private val collectionName = "userProfilePic"
    private val dataName = "url"
    private val firebaseStore = FirebaseFirestore.getInstance()

    private val photoReference = FirebaseStorage.getInstance().getReference(collectionName)
    val profilePhoto: MutableLiveData<String> = MutableLiveData()
    private val collection = firebaseStore.collection(collectionName)

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
        profilePhoto.postValue(url.toString())
        val dataMap: Map<String, String> = mutableMapOf()
        dataMap as MutableMap
        dataMap[dataName] = url.toString()
        collection.document(userId).set(dataMap)
    }

    fun uploadProfilePhoto(file: Uri, userId: String): UploadTask {
        val reference = photoReference.child("$userId.jpg")
        return reference.putFile(file)
    }

    fun loadUserProfile(userId: String) {
        collection.document(userId).get()
                .addOnSuccessListener {
                    val url = it.get(dataName)
                    url?.let {
                        url as String
                        profilePhoto.postValue(url)
                    }
                }
    }

    fun deleteUserProfilePic(userId: String) {
        Log.d("TAG", "$userId")
        collection.document(userId).delete()
        photoReference.child("$userId.jpg").delete()
    }
}