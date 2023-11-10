package com.ttpkk.mylibrary

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ttpkk.library.R
import com.ttpkk.mylibrary.sqlserver.SettingsPref
import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture

class Utils {

    companion object {
        fun getAppVersion(context: Context): String {
            return context.getString(R.string.txt_app_version_format, context.packageManager.getPackageInfoCompat(context.packageName, 0).versionName)
        }

        private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
            }

        fun getSettingPrefs(context: Context): SettingsPref {
            val settingPrefs = context.applicationContext.getSharedPreferences("MyAppPref", Context.MODE_PRIVATE)

            val server = settingPrefs.getString(context.getString(R.string.txt_key_server), "")
            val port = settingPrefs.getString(context.getString(R.string.txt_key_port), "")
            val database = settingPrefs.getString(context.getString(R.string.txt_key_database), "")
            val user = settingPrefs.getString(context.getString(R.string.txt_key_user), "")
            val password = settingPrefs.getString(context.getString(R.string.txt_key_password), "")
            val timeout = settingPrefs.getString(context.getString(R.string.txt_key_timeout), "")

            return SettingsPref(
                server.toString(),
                port.toString(),
                database.toString(),
                user.toString(),
                password.toString(),
                timeout.toString()
            )
        }

        fun setEdittextChange(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout) {
            textInputEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNotEmpty()) {
                        textInputLayout.error = null
                        textInputLayout.isErrorEnabled = false
                        textInputLayout.helperText = null
                        textInputLayout.isHelperTextEnabled = false
                    }
                }
            })
        }

        fun clearInputAlert(textInputLayout: TextInputLayout) {
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
            textInputLayout.helperText = null
            textInputLayout.isHelperTextEnabled = false
        }

        fun getDateNow(format: String? = "yyyy-MM-dd"): String {
            val currentDate = SimpleDateFormat(format, Locale.getDefault())
            val todayDate = Date()
            return currentDate.format(todayDate)
        }

        fun getTimeNow(format: String? = "HH:mm:ss"): String {
            val currentDate = SimpleDateFormat(format, Locale.getDefault())
            val todayDate = Date()
            return currentDate.format(todayDate)
        }

        fun getDateTimeNow(format: String? = "yyyy-MM-dd HH:mm:ss"): String {
            val currentDate = SimpleDateFormat(format, Locale.getDefault())
            val todayDate = Date()
            return currentDate.format(todayDate)
        }

        fun hideKeyboard(activity: Activity) {
            val view = activity.currentFocus
            val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            assert(view != null)
            methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun showKeyboard(activity: Activity) {
            val view = activity.currentFocus
            val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            assert(view != null)
            methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun getLocalIPAddress(): String {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val networkInterface = en.nextElement()
                    val enu = networkInterface.inetAddresses
                    while (enu.hasMoreElements()) {
                        val inetAddress = enu.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress() ?: ""
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return ""
        }

        fun showMessage(
            context: Context, alert: Alert, title: String, message: String? = null,
            showCancelButton: Boolean? = false,
            showClearButton: Boolean? = false,
            setCanceledOnTouchOutside: Boolean? = false
        ): CompletableFuture<Action> {
            val dialog: Int
            val icon: Int
            val result = CompletableFuture<Action>()

            when (alert) {
                Alert.FAIL -> {
                    dialog = R.style.DialogFail
                    icon = R.drawable.svg_sign_error_svgrepo_com
                }

                Alert.WARNING -> {
                    dialog = R.style.DialogWarning
                    icon = R.drawable.svg_sign_warning_svgrepo_com
                }

                Alert.SUCCESS -> {
                    dialog = R.style.DialogSuccess
                    icon = R.drawable.svg_sign_check_svgrepo_com
                }

                Alert.CONFIRM -> {
                    dialog = R.style.DialogConfirm
                    icon = R.drawable.svg_sign_question_svgrepo_com
                }

                Alert.INFORMATION -> {
                    dialog = R.style.AppTheme_AlertDialogTheme
                    icon = R.drawable.svg_sign_info_svgrepo_com
                }
            }

            val builder = AlertDialog.Builder(context, dialog)
            builder.setTitle(title)
            builder.setIcon(icon)
            if (!message.isNullOrEmpty()) builder.setMessage(message)
            builder.setCancelable(setCanceledOnTouchOutside ?: true)
            builder.setOnCancelListener { result.complete(Action.CANCEL) } // Handle back button press
            builder.setOnDismissListener { result.complete(Action.CANCEL) } // Handle dialog dismiss
            if (showClearButton == true) {
                builder.setNegativeButton(context.getString(R.string.txt_clear)) { dialog: DialogInterface, which: Int ->
                    result.complete(Action.CLEAR)
                    dialog.cancel()
                }
            }
            if (showCancelButton == true) {
                builder.setNegativeButton(context.getString(R.string.txt_cancel)) { dialog: DialogInterface, which: Int ->
                    result.complete(Action.CANCEL)
                    dialog.cancel()
                }
            }
            builder.setPositiveButton(context.getString(R.string.txt_ok)) { dialog: DialogInterface?, which: Int ->
                result.complete(Action.OK)
                dialog?.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
            return result
        }

        fun setLoadingDialog(context: Context, str: String): AlertDialog {
            val layoutInflater = LayoutInflater.from(context)
            val alertView: View = layoutInflater.inflate(R.layout.dialog_loading, null)
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(alertView)
            val tvMessage = alertView.findViewById<View>(R.id.tv_message) as TextView
            if (str.isNotEmpty()) tvMessage.text = str
            val alertDialog = builder.create()
            alertDialog.show()
            return alertDialog
        }
    }
}

enum class Alert {
    FAIL,
    WARNING,
    SUCCESS,
    CONFIRM,
    INFORMATION
}

enum class Action {
    OK,
    CANCEL,
    CLEAR
}