package com.stc.scanprint.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.stc.scanprint.R
import java.text.SimpleDateFormat
import java.util.*

class Shared {

    companion object {
        fun getAppVersion(context: Context): String {
            return context.getString(R.string.txt_app_version_format, context.packageManager.getPackageInfo(context.packageName, 0).versionName)
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

        fun getDateNow(): String {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayDate = Date()
            return currentDate.format(todayDate)
        }

        fun convertDate(date: Date): String {
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return currentDate.format(date)
        }

        fun getTimeNow(): String {
            val currentDate = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
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

        fun getLocalIpAddress(context: Context): String {
            try {
                val wifiManager: WifiManager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                return ipToString(wifiManager.connectionInfo.ipAddress)
            } catch (ex: Exception) {
                Log.e("IP Address", ex.toString())
            }
            return ""
        }

        private fun ipToString(i: Int): String {
            return (i and 0xFF).toString() + "." +
                    (i shr 8 and 0xFF) + "." +
                    (i shr 16 and 0xFF) + "." +
                    (i shr 24 and 0xFF)
        }

        fun setLoadingDialog(context: Context, str: String): AlertDialog {
            val layoutInflater = LayoutInflater.from(context)
            val alertView: View = layoutInflater.inflate(R.layout.dialog_loading, null)
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(true)
            builder.setView(alertView)
            val tvMessage = alertView.findViewById<View>(R.id.tv_message) as TextView
            if (str.isNotEmpty()) tvMessage.text = str
            val alertDialog = builder.create()
            alertDialog.show()
            return alertDialog
        }

    }

    enum class Alert {
        FAIL,
        WARNING,
        SUCCESS,
        CONFIRM,
        INFORMATION
    }
}