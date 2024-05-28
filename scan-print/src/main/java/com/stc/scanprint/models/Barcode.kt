package com.stc.scanprint.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Barcode(
    val id: Int,
    val barcode: String,
    val timestamp: Date,
    var isChecked: Boolean = false
) : Parcelable {
    companion object {
        private var currentId = 0
        fun create(barcode: String,
                   timestamp: Date,
                   isChecked: Boolean = false): Barcode {
            return Barcode(currentId++, barcode, timestamp, isChecked)
        }
    }
}
