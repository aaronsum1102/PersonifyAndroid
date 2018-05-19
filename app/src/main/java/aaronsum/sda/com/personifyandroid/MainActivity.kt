package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userViewModel = ViewModelProviders.of(this)[UserViewModel::class.java]
        taskViewModel = ViewModelProviders.of(this)[TaskViewModel::class.java]

        supportActionBar?.hide()
        var currentUsername: String?
        userViewModel.currentUsername.observe(this, Observer {
            currentUsername = it
            when (currentUsername) {
                "" -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, UserManagementFragment())
                            .commit()
                }

                else -> {
                    currentUsername?.let {
                        Toast.makeText(this@MainActivity,
                                "Welcome back, $currentUsername.",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                    initTaskList()
                }
            }
        })
    }

    private fun initTaskList() {
        taskViewModel.loadAllTask()
                .addOnSuccessListener {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, TaskListFragment())
                            .commit()
                }
                .addOnFailureListener {
                    Snackbar.make(this@MainActivity.currentFocus,
                            "Unable to load your data. ${it.message}",
                            Snackbar.LENGTH_LONG)
                            .show()
                }
    }
}