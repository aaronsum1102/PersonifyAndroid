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
import kotlinx.android.synthetic.main.fragment_user_login.*

class LogInFragment : Fragment(), TextWatcher {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        confirmButton.isEnabled = false
        emailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)

        confirmButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            Single.fromCallable { signInAuthentication(email, password) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d(TAG, "authentication on sub thread: success")
                    }, {
                        Log.d(TAG, "authentication on sub thread: failed", it.cause)
                    })
        }
    }

    private fun signInAuthentication(email: String, password: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail: success")
                        val user = firebaseAuth.currentUser
                        val displayName = user?.displayName
                        Toast.makeText(context,
                                "Welcome back, $displayName",
                                Toast.LENGTH_SHORT)
                                .show()
                        fragmentManager?.popBackStack("welcome",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        fragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.container, TaskListFragment())
                                ?.commit()
                    } else {
                        Log.d(TAG, "signInWithEmail: failure", task.exception)
                        Toast.makeText(context,
                                "Authentication failed. Please try again.",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    override fun afterTextChanged(s: Editable?) {
        confirmButton.isEnabled = emailText.text.contains("@") && passwordText.text.isNotEmpty()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}