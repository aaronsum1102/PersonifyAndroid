package aaronsum.sda.com.personifyandroid

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class UserRepository() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun createNewUser(context: Context, userInfo: UserInfo, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = firebaseAuth.currentUser
                        val userProfileChangeRequest = UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(userInfo.name)
                                .build()
                        user?.let { user ->
                            user.updateProfile(userProfileChangeRequest)
                                    .addOnCompleteListener {
                                        Toast.makeText(context,
                                                "Welcome, ${userInfo.name}", Toast.LENGTH_SHORT)
                                                .show()
                                        callback.onActionCompleted()
                                    }
                        }
                    } else {
                        Log.d("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context,
                                "Authentication failed. ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun signInUser(context: Context, email: String, password: String, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "signInWithEmail: success")
                        val user = firebaseAuth.currentUser
                        val displayName = user?.displayName
                        Toast.makeText(context,
                                "Welcome back, $displayName",
                                Toast.LENGTH_SHORT)
                                .show()
                        callback.onActionCompleted()

                    } else {
                        Log.d(ContentValues.TAG, "signInWithEmail: failure", task.exception)
                        Toast.makeText(context,
                                "Authentication failed. Please try again.",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    fun silentSignIn(context: Context, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context,
                                "Welcome back, ${firebaseAuth.currentUser!!.displayName}",
                                Toast.LENGTH_SHORT)
                                .show()
                        callback.onActionCompleted()
                    }
                }
    }

    fun resetPassword(context: Context, email: String, callback: OnFirebaseActionCompleteCallback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(context,
                            "An email has been sent to your email address.",
                            Toast.LENGTH_LONG)
                            .show()
                    callback.onActionCompleted()
                }
    }
}
