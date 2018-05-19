package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userViewModel = ViewModelProviders.of(this)[UserViewModel::class.java]
        taskViewModel = ViewModelProviders.of(this)[TaskViewModel::class.java]

        supportActionBar?.hide()
        var currentUser: User?
        userViewModel.currentUser.observe(this, Observer {
            currentUser = it
            when (currentUser) {
                null -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, UserManagementFragment())
                            .commit()
                }

                else -> {
                    currentUser?.let {
                        taskViewModel.addEventListenerToDB(it.userId)
                        Toast.makeText(this@MainActivity,
                                "Welcome back, ${it.username}.",
                                Toast.LENGTH_SHORT)
                                .show()
                        initTaskList()
                    }
                }
            }
        })
    }

    private fun initTaskList() {
        taskViewModel.loadAllTask()
                ?.addOnSuccessListener {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, TaskListFragment())
                            .commit()
                }
                ?.addOnFailureListener {
                    Snackbar.make(this@MainActivity.currentFocus,
                            "Unable to load your data. ${it.message}",
                            Snackbar.LENGTH_LONG)
                            .show()
                }
    }
}