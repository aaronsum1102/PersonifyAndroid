package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
                override fun onActionCompleted() {
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