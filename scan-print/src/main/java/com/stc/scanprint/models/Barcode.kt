package com.stc.scanprint.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Barcode(
    val barcode: String,
    val timestamp: Date,
    var isChecked: Boolean = false
): Parcelable
