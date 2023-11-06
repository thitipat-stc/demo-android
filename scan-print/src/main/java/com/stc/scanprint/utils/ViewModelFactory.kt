package com.stc.scanprint.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stc.scanprint.MainViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(/*repository = LoginRepository(context)*/) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}