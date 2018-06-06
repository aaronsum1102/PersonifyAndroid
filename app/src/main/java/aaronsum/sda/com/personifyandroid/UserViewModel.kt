package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()
    val currentUser: LiveData<User?> = userRepository.currentUser

    fun createNewUser(userInfo: UserInfo) = userRepository.createNewUser(userInfo)

    fun updateUserProfile(userInfo:UserInfo, user:FirebaseUser) = userRepository.updateUserProfile(userInfo, user)

    fun signInWithDetails(email: String, password: String) = userRepository.signInUser(email, password)

    fun resetPassword(email: String) = userRepository.resetPassword(email)

    fun signOut() = userRepository.signOut()

    fun deleteProfile() = userRepository.deleteProfile()

    fun authenticateUserBeforeDelete(email: String, password: String) = userRepository.authenticateUser(email, password)

    fun verifyPassword(password: String) = userRepository.verifyPassword(password)

    fun editProfile(userInfo: UserInfo) {
        userRepository.editProfile(userInfo)
    }
}