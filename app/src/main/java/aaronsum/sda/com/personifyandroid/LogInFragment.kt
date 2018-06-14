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
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_user_login.*

class LogInFragment : Fragment(), TextWatcher {
    companion object {
        const val BACK_STACK = "login"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            val analytics = FirebaseAnalytics.getInstance(it)
            analytics.setCurrentScreen(it, "LogIn", null)
        }
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
                    ?.addToBackStack(BACK_STACK)
                    ?.replace(R.id.container, ResetPasswordFragment())
                    ?.commit()
        }
    }

    private fun signInAuthentication(email: String, password: String) {
        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        userViewModel.signInWithDetails(email, password)
                .addOnSuccessListener { result ->
                    initCollections(result.user.uid)
                    view?.let { Util.hideSoftKeyboard(activity, view as View) }
                    result.user.displayName?.let { initTaskList(it) }
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

    private fun initCollections(userId: String) {
        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]

        photoViewModel.initProfilePhotoDocument(userId)
        taskViewModel.initUserTaskDocument(userId)
        userStatisticViewModel.initUserStatistic(userId)
    }

    private fun initTaskList(userName: String) {
        this@LogInFragment.context?.let {
            Toast.makeText(this@LogInFragment.context,
                    getString(R.string.welcome_user, userName),
                    Toast.LENGTH_SHORT)
                    .show()
        }
        fragmentManager?.popBackStack(WelcomeScreenFragment.STACK_NAME,
                FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, TaskListFragment())
                ?.commit()
    }

    override fun afterTextChanged(s: Editable?) {
        confirmButton.isEnabled = emailText.text.contains("@") && passwordText.text.toString().length >= 6
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}