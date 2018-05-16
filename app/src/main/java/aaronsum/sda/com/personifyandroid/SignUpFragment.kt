package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_signup.*

interface OnFirebaseActionCompleteCallback {
    fun onActionCompleted()
}

data class UserInfo(val name: String, val email: String, val password: String)

class SignUpFragment : Fragment(), TextWatcher {
    lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        createAccountButton.isEnabled = false

        nameText.addTextChangedListener(this)
        emailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)
        verifyPasswordText.addTextChangedListener(this)

        createAccountButton.setOnClickListener {
            val userInfo = UserInfo(nameText.text.toString(),
                    emailText.text.toString(),
                    passwordText.text.toString())
            createNewAccount(userInfo)
        }
    }

    private fun createNewAccount(userInfo: UserInfo) {
        userViewModel.createNewUser(userInfo, object : OnFirebaseActionCompleteCallback {
            override fun onActionCompleted() {
                fragmentManager?.popBackStack("welcome",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container, TaskListFragment())
                        ?.commit()
            }
        })
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