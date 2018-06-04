package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

data class User(val userId: String,
                val username: String,
                val email: String)

class UserRepository {
    companion object {
        private const val TAG = "UserRepository"
    }

    private val auth = FirebaseAuth.getInstance()
    val currentUser: MutableLiveData<User?> = MutableLiveData()

    init {
        checkUserSignInState()
    }

    fun createNewUser(userInfo: UserInfo): Task<AuthResult>? {
        return auth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
    }

    fun updateUserProfile(userInfo: UserInfo, newUser: FirebaseUser): Task<Void> {
        val userProfileChangeRequest = UserProfileChangeRequest
                .Builder()
                .setDisplayName(userInfo.name)
                .build()
        val task = newUser.updateProfile(userProfileChangeRequest)
        task.addOnSuccessListener {
            currentUser.postValue(User(newUser.uid, userInfo.name, userInfo.email))
        }
        return task
    }

    fun signInUser(email: String, password: String) = auth.signInWithEmailAndPassword(email, password)

    private fun checkUserSignInState() {
        auth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser != null) {
                currentUser.reload()
                        .addOnFailureListener {
                            Log.i(TAG, "account reload failed. ${it.localizedMessage}")
                        }
                val displayName = currentUser.displayName
                displayName?.let {
                    Log.i(TAG, "show user display name.")
                    val email = currentUser.email
                    email?.let { this.currentUser.postValue(User(currentUser.uid, displayName, email)) }
                }
            } else {
                Log.i(TAG, "not user in the session")
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
            this.currentUser.postValue(User(currentUser.uid, userInfo.name, userInfo.email))
            currentUser.updateEmail(userInfo.email)
        }
    }

    fun verifyPassword(password: String): Task<AuthResult>? {
        val email = auth.currentUser?.email
        return email?.let { auth.signInWithEmailAndPassword(email, password) }
    }

    fun deleteProfile() = auth.currentUser?.delete()
}
