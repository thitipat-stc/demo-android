package com.stc.printbt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.util.UUID
import kotlin.Exception

class PrinterObject {

    companion object {
        const val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        private val MY_UUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
    }

    private fun isInitialized() = this::bluetoothSocket.isInitialized
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothDevice: BluetoothDevice

    fun setPrinter(bluetoothDevice: BluetoothDevice) {
        try {
            if (isInitialized())
                if (this.bluetoothDevice != bluetoothDevice)
                    disconnect()

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID)
            this.bluetoothDevice = bluetoothDevice
        } catch (e: Exception) {
            throw e
        }
    }

    fun connect() {
        try {
            if (isInitialized())
                if (!isConnected())
                    bluetoothSocket.connect()
        } catch (e: Exception) {
            throw e
        }
    }

    fun disconnect() {
        try {
            if (isInitialized())
                if (isConnected())
                    bluetoothSocket.close()
        } catch (e: Exception) {
            throw e
        }
    }

    fun isConnected() = bluetoothSocket.isConnected

}