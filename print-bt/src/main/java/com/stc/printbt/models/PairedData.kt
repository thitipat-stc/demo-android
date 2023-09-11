package com.stc.printbt.models

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PairedData(
    var display: String,
    var value: BluetoothDevice? = null
) : Parcelable {
    override fun toString(): String {
        return if (this.value == null) {
            this.display
        } else {
            "${this.display}\n${this.value}"
        }
    }
}
