package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModel
import android.content.Context
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    fun createNewUser(context: Context, userInfo: UserInfo, onAccountCreatedCallback: OnAccountCreatedCallback) {
        Single.fromCallable { userRepository.createNewUser(context, userInfo, onAccountCreatedCallback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }
}