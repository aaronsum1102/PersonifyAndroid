package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, WelcomeScreenFragment())
                    .commit()
        }

        MobileAds.initialize(this, getString(R.string.ad_unit_id))
    }
}