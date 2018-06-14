package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics

class WelcomeScreenFragment : Fragment() {
    companion object {
        const val USER_ID = "userId"
        const val STACK_NAME = "welcome"
    }

    private lateinit var analytics: FirebaseAnalytics
    private val welcomeScreenTime: Long = 1500

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            analytics = FirebaseAnalytics.getInstance(it)
            analytics.setCurrentScreen(it, "WelcomeScreen", null)
        }

        signInStateCheckForAction()
    }

    private fun signInStateCheckForAction() {
        val viewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        viewModel.currentUser.observe(this, Observer {
            when (it) {
                null -> noUserInSession()
                else -> userExistInSession(it)
            }
        })
    }

    private fun noUserInSession() {
        Handler().postDelayed({
            fragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.container, UserManagementFragment())
                    ?.commit()
        }, welcomeScreenTime)
    }

    private fun userExistInSession(currentUser: User?) {
        currentUser?.let {
            val userId = it.userId
            loadProfilePhoto(userId)
            loadUserTaskStatistic(userId)
            initUserTasks(userId, it.username)
        }
    }

    private fun loadProfilePhoto(userId: String) {
        val viewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        viewModel.initProfilePhotoDocument(userId)
    }

    private fun loadUserTaskStatistic(userId: String) {
        val viewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]
        viewModel.initUserStatistic(userId)
    }

    private fun initUserTasks(userId: String, userName: String) {
        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        viewModel.initUserTaskDocument(userId)
        Handler().postDelayed({
            showWelcomeMessage(userName)
            initTaskListFragment(userId)
        }, welcomeScreenTime)
    }

    private fun showWelcomeMessage(userName: String) {
        this@WelcomeScreenFragment.context?.let {
            Toast.makeText(this@WelcomeScreenFragment.context,
                    getString(R.string.welcome_user, userName),
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun initTaskListFragment(userId: String) {
        val taskListFragment = TaskListFragment()
        val bundle = Bundle()
        bundle.putString(USER_ID, userId)
        taskListFragment.arguments = bundle
        if (this::analytics.isInitialized) {
            analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        }
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, taskListFragment)
                ?.commit()

    }
}