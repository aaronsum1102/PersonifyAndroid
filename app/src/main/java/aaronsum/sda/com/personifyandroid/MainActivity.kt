package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            auth.signInAnonymously().addOnCompleteListener {
                Toast.makeText(this,
                        "Welcome back, ${currentUser.displayName}",
                        Toast.LENGTH_SHORT)
                        .show()
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, TaskListFragment())
                        .commit()
            }
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