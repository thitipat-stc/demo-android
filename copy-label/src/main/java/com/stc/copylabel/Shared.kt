package com.stc.copylabel

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class Shared {

    companion object {
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

        fun hideKeyboard(activity: Activity) {
            val view = activity.currentFocus
            val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            assert(view != null)
            methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        fun convertDate(date: Date): String {
            val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return currentDate.format(date)
        }

        fun clearInputAlert(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout) {
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
            textInputLayout.helperText = null
            textInputLayout.isHelperTextEnabled = false
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
    }
}