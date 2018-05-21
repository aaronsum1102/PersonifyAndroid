package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var welcomeScreenFragment: WelcomeScreenFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            welcomeScreenFragment = WelcomeScreenFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, welcomeScreenFragment)
                    .commit()
        }
    }
}