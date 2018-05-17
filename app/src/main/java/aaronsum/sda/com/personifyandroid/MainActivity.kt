package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userViewModel = ViewModelProviders.of(this)[UserViewModel::class.java]

        supportActionBar?.hide()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userViewModel.silentSignIn(object : OnFirebaseActionCompleteCallback {
                override fun onActionFailed(message: String) {
                    Toast.makeText(this@MainActivity,
                            "Sign in failed because $message.",
                            Toast.LENGTH_LONG)
                            .show()
                }

                override fun onActionSucceed(message: String) {
                    Toast.makeText(this@MainActivity,
                            "Welcome back, $message",
                            Toast.LENGTH_SHORT)
                            .show()
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, TaskListFragment())
                            .commit()
                }
            })
        } else {
            if (savedInstanceState == null) {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, UserManagementFragment())
                        .commit()
            }
        }
    }
}