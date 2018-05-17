package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

interface OnFirebaseActionCompleteCallback {
    fun onActionSucceed(message: String)

    fun onActionFailed(message: String)
}

class UserRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    val currentUsername: MutableLiveData<String> = MutableLiveData()

    init {
        checkUserSignInState()
    }

    fun createNewUser(userInfo: UserInfo, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let { newUser ->
                            updateUserProfile(userInfo, callback, newUser)
                        }
                    } else {
                        val message = task.exception?.message
                        message?.let { callback.onActionFailed(it) }
                    }
                }
    }

    private fun updateUserProfile(userInfo: UserInfo, callback: OnFirebaseActionCompleteCallback, newUser: FirebaseUser) {
        val userProfileChangeRequest = UserProfileChangeRequest
                .Builder()
                .setDisplayName(userInfo.name)
                .build()
        newUser.updateProfile(userProfileChangeRequest)
                .addOnCompleteListener {
                    callback.onActionSucceed(userInfo.name)
                }
    }

    fun signInUser(email: String, password: String, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val displayName = user?.displayName
                        displayName?.let { callback.onActionSucceed(it) }
                    } else {
                        val message = task.exception?.message
                        message?.let { callback.onActionFailed(it) }
                    }
                }
    }

    private fun checkUserSignInState() {
        firebaseAuth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser != null) {
                currentUsername.postValue(currentUser.displayName)
            } else {
                currentUsername.postValue("")
            }
        }
    }

    fun resetPassword(email: String, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val message = "An email has been sent to your email address."
                        callback.onActionSucceed(message)
                    } else {
                        val message = task.exception?.message
                        message?.let { callback.onActionFailed(message) }
                    }
                }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
