package aaronsum.sda.com.personifyandroid

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()

    fun createNewUser(userInfo: UserInfo, onFirebaseActionCompleteCallback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.createNewUser(getApplication() as Context, userInfo, onFirebaseActionCompleteCallback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun signInWithDetails(email: String, password: String, callback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.signInUser(getApplication() as Context, email, password, callback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun silentSignIn(callback: OnFirebaseActionCompleteCallback) {
        Single.fromCallable { userRepository.silentSignIn(getApplication() as Context, callback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }


}