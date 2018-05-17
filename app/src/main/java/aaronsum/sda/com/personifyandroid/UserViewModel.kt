package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()
    val currentUsername: LiveData<String> = userRepository.currentUsername

    fun createNewUser(userInfo: UserInfo, onFirebaseActionCompleteCallback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.createNewUser(userInfo, onFirebaseActionCompleteCallback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun signInWithDetails(email: String, password: String, callback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.signInUser(email, password, callback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun resetPassword(email: String, callback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.resetPassword(email, callback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun signOut() {
        Single.fromCallable { userRepository.signOut() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }
}