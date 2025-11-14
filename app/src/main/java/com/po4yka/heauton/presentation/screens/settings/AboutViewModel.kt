package com.po4yka.heauton.presentation.screens.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.po4yka.heauton.BuildConfig
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for About Screen.
 *
 * Manages app version info and handles navigation intents.
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : MviViewModel<AboutContract.Intent, AboutContract.State, AboutContract.Effect>() {

    override fun createInitialState(): AboutContract.State {
        return AboutContract.State(
            appName = "Heauton",
            versionName = getVersionName(),
            versionCode = getVersionCode(),
            buildType = if (BuildConfig.DEBUG) "Debug" else "Release"
        )
    }

    override fun handleIntent(intent: AboutContract.Intent) {
        when (intent) {
            is AboutContract.Intent.NavigateBack -> {
                sendEffect(AboutContract.Effect.NavigateBack)
            }
            is AboutContract.Intent.OpenGitHub -> {
                sendEffect(
                    AboutContract.Effect.OpenUrl("https://github.com/po4yka/heauton-android")
                )
            }
            is AboutContract.Intent.OpenPrivacyPolicy -> {
                sendEffect(
                    AboutContract.Effect.ShowMessage("Privacy policy will be available soon")
                )
            }
            is AboutContract.Intent.OpenTermsOfService -> {
                sendEffect(
                    AboutContract.Effect.ShowMessage("Terms of service will be available soon")
                )
            }
            is AboutContract.Intent.OpenLicenses -> {
                sendEffect(
                    AboutContract.Effect.ShowMessage("Open source licenses screen coming soon")
                )
            }
        }
    }

    /**
     * Get app version name from package manager.
     */
    private fun getVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    /**
     * Get app version code from package manager.
     */
    private fun getVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }
    }
}
