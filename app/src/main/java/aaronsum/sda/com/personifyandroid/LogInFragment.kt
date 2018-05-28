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
import kotlinx.android.synthetic.main.fragment_user_login.*

class LogInFragment : Fragment(), TextWatcher {
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        confirmButton.isEnabled = false
        emailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)

        confirmButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            signInAuthentication(email, password)
        }

        newPasswordButton.setOnClickListener {
            fragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack("login")
                    ?.replace(R.id.container, ResetPasswordFragment())
                    ?.commit()
        }
    }

    private fun signInAuthentication(email: String, password: String) {
        userViewModel.signInWithDetails(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.continueWith {
                            val result = it.result
                            this@LogInFragment.context?.let {
                                Toast.makeText(this@LogInFragment.context,
                                        "Welcome back, ${result.user.displayName}.",
                                        Toast.LENGTH_SHORT)
                                        .show()
                            }
                            val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
                            photoViewModel.loadUserProfilePic(result.user.uid)
                            val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
                            taskViewModel.addEventListenerToDB(result.user.uid)
                            taskViewModel.loadAllTask()
                            view?.let { Util.hideSoftKeyboard(activity, view as View) }

                            val taskListFragment = TaskListFragment()
                            fragmentManager?.popBackStack("welcome",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            fragmentManager
                                    ?.beginTransaction()
                                    ?.replace(R.id.container, taskListFragment)
                                    ?.commit()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    this@LogInFragment.context?.let {
                        Toast.makeText(this@LogInFragment.context,
                                "Authentication failed. ${exception.localizedMessage}",
                                Toast.LENGTH_LONG)
                                .show()
                    }
                }
    }

    override fun afterTextChanged(s: Editable?) {
        confirmButton.isEnabled = emailText.text.contains("@") && passwordText.text.toString().length >= 6
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}