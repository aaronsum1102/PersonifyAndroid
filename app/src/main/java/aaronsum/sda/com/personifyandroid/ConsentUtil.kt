package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.net.URL

object ConsentUtil {
    fun displayAdd(fragment: Fragment, @LayoutRes layoutWithAd: Int, @LayoutRes layoutWithoutAd: Int) {
        if (!(BuildConfig.DEBUG)) {
            verifyConsentStatusBeforeAction(fragment, layoutWithAd, layoutWithoutAd)
        }
    }

    private fun verifyConsentStatusBeforeAction(fragment: Fragment, @LayoutRes layoutWithAd: Int, @LayoutRes layoutWithoutAd: Int) {
        val context = fragment.context
        val view = fragment.view
        context?.let {
            view?.let {
                val consentInformation = ConsentInformation.getInstance(context)
                consentInformation.requestConsentInfoUpdate(arrayOf(context.getString(R.string.publisher_id)),
                        object : ConsentInfoUpdateListener {
                            override fun onFailedToUpdateConsentInfo(reason: String?) {
                                setLayout(fragment, layoutWithAd)
                            }

                            override fun onConsentInfoUpdated(consentStatus: ConsentStatus?) {
                                when (consentStatus) {
                                    ConsentStatus.UNKNOWN -> {
                                        showConsentForm(fragment, layoutWithAd, layoutWithoutAd)
                                    }
                                    else -> {
                                        if (consentInformation.isRequestLocationInEeaOrUnknown) {
                                            if (consentStatus == ConsentStatus.PERSONALIZED) {
                                                loadAd(fragment, layoutWithAd, adRequest(null, null))
                                            } else {
                                                loadAd(fragment, layoutWithAd, adRequest("npa", "1"))
                                            }
                                        } else {
                                            loadAd(fragment, layoutWithAd, adRequest(null, null))
                                        }
                                    }
                                }
                            }
                        })
            }
        }
    }

    private fun showConsentForm(fragment: Fragment, @LayoutRes layoutId: Int, @LayoutRes layoutWithoutAd: Int) {
        val context = fragment.context
        var form: ConsentForm? = null
        context?.let {
            form = ConsentForm.Builder(context, URL(context.getString(R.string.privacy_policy)))
                    .withListener(object : ConsentFormListener() {
                        override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                            super.onConsentFormClosed(consentStatus, userPrefersAdFree)
                            userPrefersAdFree?.let {
                                if (userPrefersAdFree) {
                                    fragment.activity?.finish()
                                } else {
                                    verifyConsentStatusBeforeAction(fragment, layoutId, layoutWithoutAd)
                                }
                            }
                        }

                        override fun onConsentFormLoaded() {
                            form?.show()
                        }
                    })
                    .withPersonalizedAdsOption()
                    .withNonPersonalizedAdsOption()
                    .build()
            form?.load()
        }
    }

    private fun loadAd(fragment: Fragment, @LayoutRes layoutId: Int, adRequest: AdRequest) {
        val view = fragment.view
        view?.let {
            val adView = view.findViewById<AdView>(R.id.adView)
            adView?.loadAd(adRequest)
            adView?.adListener = object : AdListener() {
                override fun onAdLoaded() {}

                override fun onAdFailedToLoad(errorCode: Int) {
                    setLayout(fragment, layoutId)
                }

                override fun onAdOpened() {}

                override fun onAdLeftApplication() {}

                override fun onAdClosed() {}
            }
        }
    }

    private fun adRequest(key: String?, value: String?): AdRequest {
        val extras = Bundle()
        extras.putString("max_ad_content_rating", "G")
        if (key != null && value != null) {
            extras.putString(key, value)
        }
        return AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
    }

    private fun setLayout(fragment: Fragment, @LayoutRes layoutId: Int) {
        val context = fragment.context
        val view = fragment.activity
        val root = view?.findViewById<ConstraintLayout>(R.id.root)
        val set = ConstraintSet()
        context?.let {
            root?.let {
                set.run {
                    clone(context, layoutId)
                    applyTo(root)
                }
            }
        }
    }
}