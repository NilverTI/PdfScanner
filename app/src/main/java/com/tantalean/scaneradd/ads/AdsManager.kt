package com.tantalean.scaneradd.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdsManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    // ✅ TU ID REAL
    private val realAdUnitId = "ca-app-pub-3940256099942544/5224354917"

    // ✅ ID TEST (usa este si estás en emulador para comprobar)
    private val testAdUnitId = "ca-app-pub-3940256099942544/5224354917"

    // 🔁 Cambia a true si quieres probar con anuncios test
    private val USE_TEST_ADS = false

    private val adUnitId: String
        get() = if (USE_TEST_ADS) testAdUnitId else realAdUnitId

    fun init() {
        MobileAds.initialize(context) {}
        loadRewarded()
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    fun loadRewarded() {
        if (isLoading) return
        isLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d("AdsManager", "Rewarded loaded ✅")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e("AdsManager", "Failed to load ❌: ${error.message}")
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        onRewarded: (RewardItem) -> Unit,
        onNotReady: () -> Unit,
        onClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            // No está listo: recarga y avisa que aún no está
            loadRewarded()
            onNotReady()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadRewarded()
                Log.e("AdsManager", "Failed to show ❌: ${adError.message}")
                onClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("AdsManager", "Ad showed ✅")
            }
        }

        ad.show(activity) { rewardItem ->
            // ✅ SOLO si el usuario ganó la recompensa
            onRewarded(rewardItem)
        }
    }
}