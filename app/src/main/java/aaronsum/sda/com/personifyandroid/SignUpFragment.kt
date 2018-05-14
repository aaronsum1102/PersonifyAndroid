package aaronsum.sda.com.personifyandroid

import android.content.ContentValues.TAG
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_signup.*


data class UserInfo(val name: String, val email: String, val password: String)

class SignUpFragment : Fragment(), TextWatcher {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createAccountButton.isEnabled = false

        nameText.addTextChangedListener(this)
        emailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)
        verifyPasswordText.addTextChangedListener(this)

        createAccountButton.setOnClickListener {
            val userInfo = UserInfo(nameText.text.toString(),
                    emailText.text.toString(),
                    passwordText.text.toString())
            Single.fromCallable { createNewAccount(userInfo) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d(TAG, "create account on sub thread: success")
                    }, {
                        Log.d(TAG, "create account on sub thread: failed", it.cause)
                    })
        }
    }

    private fun createNewAccount(userInfo: UserInfo) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(userInfo.email, userInfo.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = firebaseAuth.currentUser
                        Toast.makeText(context,
                                "Welcome, ${userInfo.name}", Toast.LENGTH_SHORT)
                                .show()
                        fragmentManager?.popBackStack("welcome",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        fragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.container, TaskListFragment())
                                ?.commit()

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }

    }

    override fun afterTextChanged(s: Editable?) {
        createAccountButton.isEnabled = nameText.text.isNotEmpty()
                && emailText.text.contains("@")
                && passwordText.text.toString() == verifyPasswordText.text.toString()
                && passwordText.text.length >= 6
        if (passwordText.text.length == verifyPasswordText.text.length &&
                passwordText.text.toString() != verifyPasswordText.text.toString() &&
                passwordText.text.isNotEmpty()) {
            Toast.makeText(context, "Password doesn't match.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}