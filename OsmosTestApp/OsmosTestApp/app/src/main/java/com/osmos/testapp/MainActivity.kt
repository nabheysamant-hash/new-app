package com.osmos.testapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ai.osmos.core.OsmosSDK
import com.ai.osmos.ads.views.managers.BannerAdSettings
import com.ai.osmos.ads.views.interfaces.BannerAdViewInterface
import com.ai.osmos.models.ads.ContextTargeting
import com.ai.osmos.utils.error.ErrorCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ─────────────────────────────────────────
    // 🔧 CONFIGURATION — Change these to test!
    // ─────────────────────────────────────────

    // ✅ SCENARIO 1: WORKING CONFIG (real values)
    private val WORKING_CLIENT_ID   = "10131833"
    private val WORKING_CLI_UBID    = "user_123"
    private val WORKING_PAGE_TYPE   = "test-page"
    private val WORKING_AD_UNIT     = "test-inventory"

    // ❌ SCENARIO 2: FAILING CONFIG (client's mistake — wrong page_type & ad_unit)
    private val FAILING_CLI_UBID    = "user_123"
    private val FAILING_PAGE_TYPE   = "demo_page"      // ❌ Not registered in portal
    private val FAILING_AD_UNIT     = "banner_ads"     // ❌ Not configured in portal

    // Toggle this to switch between scenarios:
    // true  = show working ad
    // false = replicate client's failure
    private val USE_WORKING_CONFIG = true

    // ─────────────────────────────────────────

    private lateinit var tvStatusLog: TextView
    private lateinit var tvAdPlaceholder: TextView
    private lateinit var bannerAdContainer: LinearLayout

    private val logLines = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusLog      = findViewById(R.id.tvStatusLog)
        tvAdPlaceholder  = findViewById(R.id.tvAdPlaceholder)
        bannerAdContainer = findViewById(R.id.bannerAdContainer)

        // Show which scenario we're running
        if (USE_WORKING_CONFIG) {
            addLog("🟢 Running: WORKING config")
            addLog("   page_type = $WORKING_PAGE_TYPE")
            addLog("   ad_unit   = $WORKING_AD_UNIT")
        } else {
            addLog("🔴 Running: FAILING config (replicating client bug)")
            addLog("   page_type = $FAILING_PAGE_TYPE  ← Not in portal!")
            addLog("   ad_unit   = $FAILING_AD_UNIT  ← Not in portal!")
        }

        initializeSdkAndLoadAd()
    }

    private fun initializeSdkAndLoadAd() {
        addLog("⏳ Initializing OSMOS SDK...")

        try {
            // Step 1: Initialize SDK
            val osmosSDK = OsmosSDK
                .clientId(WORKING_CLIENT_ID)
                .debug(true)
                .build()

            addLog("✅ SDK initialized with client_id: $WORKING_CLIENT_ID")

            // Step 2: Get AdRenderer
            val adRenderer = osmosSDK.adRenderer()
            val bannerAdViewManager = osmosSDK.bannerAdView()

            addLog("✅ AdRenderer and BannerAdViewManager ready")
            addLog("📡 Making ad request...")

            // Step 3: Fetch Ad — uses working or failing config based on toggle
            lifecycleScope.launch {
                try {
                    val cliUbid  = if (USE_WORKING_CONFIG) WORKING_CLI_UBID  else FAILING_CLI_UBID
                    val pageType = if (USE_WORKING_CONFIG) WORKING_PAGE_TYPE else FAILING_PAGE_TYPE
                    val adUnit   = if (USE_WORKING_CONFIG) WORKING_AD_UNIT   else FAILING_AD_UNIT

                    val targetingParams = listOf(
                        ContextTargeting.keyword("test product")
                    )

                    val adData = adRenderer.fetchBannerAdsWithAu(
                        cliUbid = cliUbid,
                        pageType = pageType,
                        adUnit = adUnit,
                        targetingParams = targetingParams,
                        errorCallback = object : ErrorCallback {
                            override fun onError(
                                errorCode: String,
                                errorMessage: String,
                                throwable: Throwable?
                            ) {
                                addLog("❌ Fetch error [$errorCode]: $errorMessage")
                            }
                        }
                    )

                    // Step 4: Handle response
                    if (adData == null) {
                        addLog("⚠️ adData is null — server returned no ads")
                        addLog("   This means page_type or ad_unit is not")
                        addLog("   configured in the OSMOS portal!")
                        updatePlaceholder("❌ No ad returned (empty response)")
                        return@launch
                    }

                    addLog("✅ Ad data received!")
                    addLog("📐 Creating BannerAdSettings (300x250)...")

                    // Step 5: Configure ad settings
                    val adSettings = BannerAdSettings(
                        width = 300,
                        height = 250,
                        errorCallback = object : ErrorCallback {
                            override fun onError(
                                errorCode: String,
                                errorMessage: String,
                                throwable: Throwable?
                            ) {
                                addLog("❌ View error [$errorCode]: $errorMessage")
                            }
                        }
                    )

                    // Step 6: Render the ad
                    addLog("🎨 Rendering banner ad...")
                    val bannerAdView = bannerAdViewManager.showAd(
                        this@MainActivity,
                        ad = adData,
                        adViewSettings = adSettings
                    )

                    // Step 7: Add listeners and inject into layout
                    (bannerAdView as? BannerAdViewInterface)?.apply {
                        setAdClickListener { _ ->
                            addLog("👆 Ad was clicked!")
                            Log.d("OSMOS_TEST", "Ad clicked")
                        }
                        setViewLoadListener { _, _ ->
                            addLog("✅ Ad view loaded successfully!")
                            updatePlaceholder(null) // hide placeholder
                            Log.d("OSMOS_TEST", "Ad view loaded")
                        }
                    }

                    // Step 8: Add to container
                    runOnUiThread {
                        tvAdPlaceholder.visibility = View.GONE
                        bannerAdContainer.addView(bannerAdView)
                        addLog("✅ Banner ad added to screen!")
                    }

                } catch (e: Exception) {
                    addLog("💥 CRASH: ${e.javaClass.simpleName}")
                    addLog("   ${e.message}")
                    Log.e("OSMOS_TEST", "Exception", e)
                }
            }

        } catch (e: Exception) {
            addLog("💥 SDK Init failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────
    // Helper: append a line to the on-screen log
    // ─────────────────────────────────────────
    private fun addLog(line: String) {
        Log.d("OSMOS_TEST", line)
        runOnUiThread {
            logLines.add(line)
            tvStatusLog.text = logLines.joinToString("\n")
        }
    }

    private fun updatePlaceholder(message: String?) {
        runOnUiThread {
            if (message != null) {
                tvAdPlaceholder.text = message
                tvAdPlaceholder.visibility = View.VISIBLE
            } else {
                tvAdPlaceholder.visibility = View.GONE
            }
        }
    }
}
