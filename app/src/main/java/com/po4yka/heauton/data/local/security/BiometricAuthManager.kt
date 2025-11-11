package com.po4yka.heauton.data.local.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages biometric authentication for the app.
 *
 * ## Features:
 * - Check biometric availability
 * - Show biometric prompt
 * - Support for fingerprint, face, and iris recognition
 * - Fallback to device credentials (PIN/password/pattern)
 *
 * ## Usage:
 * ```kotlin
 * biometricAuthManager.authenticate(
 *     activity = this,
 *     title = "Unlock Journal",
 *     subtitle = "Use your biometric to unlock",
 *     onSuccess = { /* Access granted */ },
 *     onError = { error -> /* Handle error */ }
 * )
 * ```
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Result of biometric authentication.
     */
    sealed class AuthResult {
        /**
         * Authentication successful.
         */
        object Success : AuthResult()

        /**
         * Authentication failed.
         */
        data class Error(val errorCode: Int, val errorMessage: String) : AuthResult()

        /**
         * User cancelled authentication.
         */
        object Cancelled : AuthResult()
    }

    /**
     * Biometric availability status.
     */
    sealed class BiometricStatus {
        /**
         * Biometric authentication is available and can be used.
         */
        object Available : BiometricStatus()

        /**
         * No biometric features are available on this device.
         */
        object NoneEnrolled : BiometricStatus()

        /**
         * Biometric hardware is not available.
         */
        object NoHardware : BiometricStatus()

        /**
         * Biometric features are currently unavailable.
         */
        object Unavailable : BiometricStatus()

        /**
         * Device does not have a secure lock screen set up.
         */
        object SecurityUpdateRequired : BiometricStatus()
    }

    /**
     * Check if biometric authentication is available.
     *
     * @return Current biometric availability status
     */
    fun checkBiometricAvailability(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)

        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricStatus.Available

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricStatus.NoneEnrolled

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricStatus.NoHardware

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricStatus.Unavailable

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricStatus.SecurityUpdateRequired

            else -> BiometricStatus.Unavailable
        }
    }

    /**
     * Check if biometric authentication is available (simple boolean check).
     */
    fun isBiometricAvailable(): Boolean {
        return checkBiometricAvailability() is BiometricStatus.Available
    }

    /**
     * Authenticate using biometric or device credentials.
     *
     * @param activity FragmentActivity context for showing the prompt
     * @param title Title shown in the prompt
     * @param subtitle Optional subtitle
     * @param description Optional description
     * @param negativeButtonText Text for the negative button (cancel)
     * @param onResult Callback with authentication result
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        description: String? = null,
        negativeButtonText: String = "Cancel",
        onResult: (AuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(AuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onResult(AuthResult.Cancelled)
                    } else {
                        onResult(AuthResult.Error(errorCode, errString.toString()))
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // This is called when biometric is recognized but not valid
                    // Don't dismiss the prompt, let user try again
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Authenticate with a simplified callback API.
     *
     * @param activity FragmentActivity context
     * @param title Title of the prompt
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails (optional)
     * @param onCancelled Called when user cancels (optional)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null,
        onCancelled: (() -> Unit)? = null
    ) {
        authenticate(
            activity = activity,
            title = title,
            subtitle = null,
            description = null,
            onResult = { result ->
                when (result) {
                    is AuthResult.Success -> onSuccess()
                    is AuthResult.Error -> onError?.invoke(result.errorMessage)
                    is AuthResult.Cancelled -> onCancelled?.invoke()
                }
            }
        )
    }

    /**
     * Get human-readable message for biometric status.
     */
    fun getStatusMessage(status: BiometricStatus): String {
        return when (status) {
            is BiometricStatus.Available ->
                "Biometric authentication is available"

            is BiometricStatus.NoneEnrolled ->
                "No biometric credentials enrolled. Please set up biometric authentication in device settings."

            is BiometricStatus.NoHardware ->
                "This device does not support biometric authentication"

            is BiometricStatus.Unavailable ->
                "Biometric authentication is currently unavailable"

            is BiometricStatus.SecurityUpdateRequired ->
                "A security update is required for biometric authentication"
        }
    }
}
