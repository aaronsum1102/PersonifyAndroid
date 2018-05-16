package aaronsum.sda.com.personifyandroid

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class UserRepository() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun createNewUser(context: Context, userInfo: UserInfo, onAccountCreatedCallback: OnAccountCreatedCallback) {
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
                                        onAccountCreatedCallback.onAccountCreated()
                                    }
                        }
                    } else {
                        Log.d( "TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context,
                                "Authentication failed. ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}