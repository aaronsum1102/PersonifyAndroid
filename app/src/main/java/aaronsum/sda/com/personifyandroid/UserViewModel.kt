package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()
    val currentUser: LiveData<User?> = userRepository.currentUser

    fun createNewUser(userInfo: UserInfo) = userRepository.createNewUser(userInfo)

    fun signInWithDetails(email: String, password: String) = userRepository.signInUser(email, password)

    fun resetPassword(email: String) = userRepository.resetPassword(email)

    fun signOut() = userRepository.signOut()
}