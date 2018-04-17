package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, TaskListFragment())
                    .commit()
        }

        addTaskFab.setOnClickListener {
            if (savedInstanceState == null) {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, TaskFromFragment())
                        .commit()
            }
        }
    }
}
