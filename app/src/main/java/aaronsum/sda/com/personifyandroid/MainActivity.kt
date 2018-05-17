package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userViewModel = ViewModelProviders.of(this)[UserViewModel::class.java]

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
                                "Welcome back, $currentUsername",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, TaskListFragment())
                            .commit()
                }
            }
        })
    }
}