package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.MobileAds
import io.fabric.sdk.android.Fabric

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this, applicationContext.packageName)
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build()
        Fabric.with(this, crashlytics)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, getString(R.string.adMob_app_id))
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, WelcomeScreenFragment())
                    .commit()
        }
    }
}