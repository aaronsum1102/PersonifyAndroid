package aaronsum.sda.com.personifyandroid

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

interface OnFirebaseActionCompleteCallback {
    fun onActionSucceed(message: String)

    fun onActionFailed(message: String)
}

class UserRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun createNewUser(userInfo: UserInfo, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val userProfileChangeRequest = UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(userInfo.name)
                                .build()
                        user?.let { newUser ->
                            newUser.updateProfile(userProfileChangeRequest)
                                    .addOnCompleteListener {

                                        callback.onActionSucceed(userInfo.name)
                                    }
                        }
                    } else {
                        val message = task.exception?.message
                        message?.let { callback.onActionFailed(it) }
                    }
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

    fun silentSignIn(callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displayName = firebaseAuth.currentUser?.displayName
                        displayName?.let { callback.onActionSucceed(displayName) }
                    } else {
                        val message = task.exception?.message
                        message?.let { callback.onActionFailed(it) }
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
}
