package com.stc.onecheck.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.stc.onecheck.R
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

        fun showMessage(context: Context, alert: Alert, title: String, message: String? = null, isShowCancel: Boolean? = false) {
            val dialog: Int
            val icon: Int

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
                else -> {
                    dialog = R.style.AppTheme_AlertDialogTheme
                    icon = R.drawable.svg_sign_info_svgrepo_com
                }
            }

            val builder = AlertDialog.Builder(context, dialog)
            builder.setTitle(title)
            builder.setIcon(icon)
            if (!message.isNullOrEmpty()) builder.setMessage(message)
            builder.setCancelable(true)
            if (isShowCancel == true) {
                builder.setNegativeButton(context.getString(R.string.txt_cancel)) { dialog: DialogInterface, which: Int ->
                    dialog.cancel()
                }
            }
            builder.setPositiveButton(context.getString(R.string.txt_ok)) { dialog: DialogInterface?, which: Int ->
                dialog?.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }

        fun soundAlert(context: Context, snd: SND, isVibrate: Boolean = true) {
            val mp = when (snd) {
                SND.ERROR -> MediaPlayer.create(context, R.raw.sound)
                SND.INFO -> MediaPlayer.create(context, R.raw.success)
            }
            if(mp.isPlaying){
                mp.stop()
                Handler(Looper.getMainLooper()).postDelayed({
                    mp.start()
                },100)
            }else{
                mp.start()
            }
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isVibrate) v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                if (isVibrate) v.vibrate(500)
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

    enum class SND {
        ERROR,
        INFO
    }
}