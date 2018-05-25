package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

class WelcomeScreenFragment : Fragment() {
    private val welcomeScreenTime: Long = 1500

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInStateCheckForAction()
    }

    private fun signInStateCheckForAction() {
        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        var currentUser: User?
        userViewModel.currentUser.observe(this, Observer {
            currentUser = it
            when (currentUser) {
                null -> {
                    Handler().postDelayed({
                        fragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.container, UserManagementFragment())
                                ?.commit()
                    }, welcomeScreenTime)
                }

                else -> {
                    currentUser?.let {
                        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
                        taskViewModel.addEventListenerToDB(it.userId)
                        val message = "Welcome back, ${it.username}."
                        initTaskList(taskViewModel, message, it.userId)
                    }
                }
            }
        })
    }

    private fun initTaskList(taskViewModel: TaskViewModel, message: String, userId: String) {
        taskViewModel.loadAllTask()
                ?.addOnSuccessListener {
                    Handler().postDelayed({
                        this@WelcomeScreenFragment.context?.let {
                            Toast.makeText(this@WelcomeScreenFragment.context,
                                    message,
                                    Toast.LENGTH_SHORT)
                                    .show()
                        }
                        fragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.container, TaskListFragment())
                                ?.commit()
                    }, welcomeScreenTime)
                }
                ?.addOnFailureListener { exception ->
                    val view = this@WelcomeScreenFragment.view
                    view?.let {
                        Snackbar.make(view,
                                "Unable to load your data. ${exception.message}",
                                Snackbar.LENGTH_LONG)
                                .show()
                    }
                }
    }
}