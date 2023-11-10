package com.ttpkk.mylibrary.print

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class PrinterService {

    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var connectedInputStream: InputStream
    private lateinit var connectedOutputStream: OutputStream
    private lateinit var job: Job

    companion object {
        val TAG: String = PrinterService::class.java.simpleName
        private const val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        private val MY_UUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)
        var mState = PrinterStatus.STATE_NONE
    }

    private fun isSocketInitialized() = this::bluetoothSocket.isInitialized
    private fun isSocketConnected() = bluetoothSocket.isConnected
    private fun isJopInitialized() = this::job.isInitialized
    fun isStatus(): PrinterStatus = mState

    @SuppressLint("MissingPermission")
    fun setPrinter(bluetoothDevice: BluetoothDevice) {
        try {
            if (isSocketInitialized()) disconnect()
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID)
            mState = PrinterStatus.STATE_CONNECTING
        } catch (e: IOException) {
            lostConnection()
            throw e
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        if (isSocketInitialized()) {
            try {
                if (!isSocketConnected()) {
                    bluetoothSocket.connect()
                    connectedThread(bluetoothSocket)
                }
            } catch (e: IOException) {
                lostConnection()
                throw e
            }
        }
    }

    fun disconnect() {
        try {
            if (isJopInitialized())
                if (job.isActive)
                    job.cancel()

            if (isSocketInitialized())
                if (isSocketConnected())
                    bluetoothSocket.close()
        } catch (e: IOException) {
            throw e
        } finally {
            lostConnection()
        }
    }

    fun sendCommandToPrint(byteArray: ByteArray) {
        if (isSocketInitialized())
            if (isSocketConnected())
                write(byteArray)
    }

    private fun connectedThread(bluetoothSocket: BluetoothSocket) {
        job = CoroutineScope(Dispatchers.Default).launch {
            connectedInputStream = bluetoothSocket.inputStream
            connectedOutputStream = bluetoothSocket.outputStream
            mState = PrinterStatus.STATE_CONNECTED
            while (mState == PrinterStatus.STATE_CONNECTED) {
                val buffer = ByteArray(1024)
                var bytes: Int
                try {
                    bytes = withContext(Dispatchers.IO) {
                        connectedInputStream.read(buffer)
                    }
                    val strReceived = String(buffer, 0, bytes)
                    Log.i(TAG, "$bytes bytes received:$strReceived")
                } catch (e: IOException) {
                    lostConnection()
                }
            }
        }
    }

    private fun write(buffer: ByteArray?) {
        try {
            connectedOutputStream.write(buffer)
        } catch (e: IOException) {
            throw e
        }
    }

    private fun lostConnection() {
        mState = PrinterStatus.STATE_NONE
    }

    enum class PrinterStatus {
        STATE_NONE, // we're doing nothing
        STATE_CONNECTING, // now initiating an outgoing connection
        STATE_CONNECTED, // now connected to a remote device
        //STATE_LISTEN, // now listening for incoming connections
    }
}