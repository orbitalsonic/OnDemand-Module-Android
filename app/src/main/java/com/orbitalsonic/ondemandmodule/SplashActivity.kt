package com.orbitalsonic.ondemandmodule

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.orbitalsonic.ondemandmodule.databinding.ActivitySplashBinding
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var manager: SplitInstallManager
    private val moduleAssets by lazy { getString(R.string.module_assets) }

    /** Listener used to handle changes in state for install requests. */
    private val listener = SplitInstallStateUpdatedListener { state ->
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                //  In order to see this, the application has to be uploaded to the Play Store.
                displayLoadingState(state)
                Log.i("OnDemandModule","DOWNLOADING")
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                /*
                  This may occur when attempting to download a sufficiently large module.

                  In order to see this, the application has to be uploaded to the Play Store.
                  Then features can be requested until the confirmation path is triggered.
                 */
                Log.i("OnDemandModule","REQUIRES_USER_CONFIRMATION")
                startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                displayAssets()
                Log.i("OnDemandModule","INSTALLED")
            }

            SplitInstallSessionStatus.INSTALLING -> {
                binding.subTitleDownloading.text = "Please wait feature is installing..."
                Log.i("OnDemandModule","INSTALLING")
                displayLoadingState(state)
            }
            SplitInstallSessionStatus.FAILED -> {
                binding.subTitleDownloading.text = "Oh no something happened! Please restart the app"
                Log.i(
                    "OnDemandModule",
                    "Error: ${state.errorCode()} for module ${state.moduleNames()}"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        manager = SplitInstallManagerFactory.create(this)
        loadAndLaunchModule()

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

    }


    private fun loadHandler(timer: Long = 4000) {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()

        }, timer)
    }

    private fun loadAndLaunchModule() {
        // Skip loading if the module already is installed. Perform success action directly.
        if (manager.installedModules.contains(moduleAssets)) {
            setInitialViews(true)
            return
        }

        setInitialViews(false)
        // Create request to install a feature module by name.
        val request = SplitInstallRequest.newBuilder()
            .addModule(moduleAssets)
            .build()

        // Load and install the requested feature module.
        manager.startInstall(request)
    }

    /** Display assets loaded from the assets feature module. */
    private fun displayAssets() {
        binding.btnNext.visibility = View.VISIBLE
        binding.subTitleDownloading.text = "Installation is completed, click button to start"
        binding.progressDownloading.progress = 100
        binding.tvDownloadingCounter.text = "100%"
    }

    /** Display a loading state to the user. */
    private fun displayLoadingState(state: SplitInstallSessionState) {
        val bytesDownloaded = state.bytesDownloaded()
        val totalBytesToDownload = state.totalBytesToDownload()
        val mDiv:Float = bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()
        val downloadPercentage:Float = mDiv * 100f
        binding.progressDownloading.progress = downloadPercentage.toInt()
        binding.tvDownloadingCounter.text = "${downloadPercentage.toInt()}%"
        binding.subTitleDownloading.text = "${getFileSize(bytesDownloaded)}/${getFileSize(totalBytesToDownload)}"
        Log.i("OnDemandModule","bytesDownloaded: $bytesDownloaded")
        Log.i("OnDemandModule","totalBytesToDownload: $totalBytesToDownload")
        Log.i("OnDemandModule","mDiv: $mDiv")
        Log.i("OnDemandModule","downloadPercentage: $downloadPercentage")
    }

    private fun setInitialViews(isModuleInstalled:Boolean){
        if (isModuleInstalled){
            binding.loadingContainer.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
            loadHandler()
        }else{
            binding.progressDownloading.max = 100
            binding.progressDownloading.progress = 0
            binding.tvDownloadingCounter.text = "0%"

            binding.loadingContainer.visibility = View.VISIBLE
            binding.btnNext.visibility = View.GONE
        }
    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        manager.unregisterListener(listener)
        super.onPause()
    }

    override fun onBackPressed() {
    }

    private fun getFileSize(size: Long): String {
        if (size <= 0) return "0B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }


}