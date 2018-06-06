package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

data class User(val userId: String = "",
                val username: String = "",
                val email: String = "")

class UserRepository {
    companion object {
        private const val TAG = "RepositoryUser"
    }

    private val auth = FirebaseAuth.getInstance()
    val currentUser: MutableLiveData<User?> = MutableLiveData()
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("userMetadata")

    init {
        checkUserSignInState()
        db.firestoreSettings = Util.persistenceDBSetting
    }

    private fun addDocumentChangeListener(userId: String) {
        collection.document(userId).addSnapshotListener { documentSnapshot, exception ->
            Log.d(TAG, "error adding listener to user metadata doc. ${exception?.localizedMessage}")
            documentSnapshot?.let {
                val user = it.toObject(User::class.java)
                currentUser.postValue(user)
            }
        }
    }

    fun createNewUser(userInfo: UserInfo): Task<AuthResult>? {
        return auth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
    }

    fun updateUserProfile(userInfo: UserInfo, newUser: FirebaseUser): Task<Void> {
        writeUserMetadataToDB(userInfo, newUser.uid)
        val userProfileChangeRequest = UserProfileChangeRequest
                .Builder()
                .setDisplayName(userInfo.name)
                .build()
        val task = newUser.updateProfile(userProfileChangeRequest)
        task.addOnSuccessListener {
            addDocumentChangeListener(newUser.uid)
        }
        return task
    }

    private fun writeUserMetadataToDB(userInfo: UserInfo, userId: String) {
        collection.document(userId).set(User(userId, userInfo.name, userInfo.email))
    }

    fun signInUser(email: String, password: String) = auth.signInWithEmailAndPassword(email, password)

    private fun checkUserSignInState() {
        auth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser != null) {
                currentUser.reload()
                        .addOnFailureListener {
                            Log.e(TAG, "account reload failed. ${it.localizedMessage}")
                        }
                addDocumentChangeListener(currentUser.uid)
            } else {
                this.currentUser.postValue(null)
            }
        }
    }

    fun resetPassword(email: String) = auth.sendPasswordResetEmail(email)

    fun signOut() = auth.signOut()

    fun editProfile(userInfo: UserInfo) {
        val currentUser = auth.currentUser
        currentUser?.let {
            updateUserProfile(userInfo, currentUser)
        }
    }

    fun verifyPassword(password: String): Task<AuthResult>? {
        val email = auth.currentUser?.email
        return email?.let { auth.signInWithEmailAndPassword(email, password) }
    }

    fun deleteProfile(): Task<Void>? {
        Log.i(TAG, "current user before delete account, ${auth.currentUser}")
        auth.currentUser?.uid?.let {
            collection.document(it).delete()
        }
        currentUser.postValue(null)
        return auth.currentUser?.delete()
    }

    fun authenticateUser(email: String, password: String) = auth.signInWithEmailAndPassword(email, password)
}
