package com.thitipat.printerservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class PrinterObject {

    companion object {
        private const val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        private val MY_UUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)

        const val STATE_NONE = 0 // we're doing nothing
        const val STATE_LISTEN = 1 // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3 // now connected to a remote device

        var mState: Int = STATE_NONE
    }

    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread

    private fun isInitialized() = this::bluetoothSocket.isInitialized
    private fun isConnected() = bluetoothSocket.isConnected
    fun isStateConnected(): Boolean {
        return mState == STATE_CONNECTED
    }

    @SuppressLint("MissingPermission")
    fun setPrinter(bluetoothDevice: BluetoothDevice) {
        try {
            if (isInitialized()) disconnect()
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID)
            mState = STATE_CONNECTING
        } catch (e: IOException) {
            throw e
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        if (isInitialized()) {
            try {
                if (this::connectedThread.isInitialized && connectedThread.isAlive) connectedThread.cancel()

                if (!isConnected()) bluetoothSocket.connect()
            } catch (e: IOException) {
                connectionFailed()
                throw e
            } finally {
                connectedThread = ConnectedThread(bluetoothSocket)
                connectedThread.start()
            }
        }
    }

    private fun connectionFailed() {
        //Unable to connect device
        mState = STATE_NONE
    }

    fun disconnect() {
        try {
            if (isInitialized()) {
                if (this::connectedThread.isInitialized && connectedThread.isAlive) connectedThread.cancel()

                if (isConnected())
                    bluetoothSocket.close()
            }
        } catch (e: IOException) {
            throw e
        }
    }

    fun sendCommandToPrint(byteArray: ByteArray) {
        if (isInitialized()) if (isConnected()) if (connectedThread.isAlive) connectedThread.write(byteArray)
    }

    class ConnectedThread(bluetoothSocket: BluetoothSocket) : Thread() {
        private var TAG = ConnectedThread::class.simpleName
        private var bluetoothSocket: BluetoothSocket
        private var connectedInputStream: InputStream
        private var connectedOutputStream: OutputStream

        init {
            this.bluetoothSocket = bluetoothSocket
            connectedInputStream = bluetoothSocket.inputStream
            connectedOutputStream = bluetoothSocket.outputStream
            mState = STATE_CONNECTED
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (mState == STATE_CONNECTED) {
                try {
                    bytes = connectedInputStream.read(buffer)
                    val strReceived = String(buffer, 0, bytes)
                    Log.i(TAG, "$bytes bytes received:$strReceived")
                } catch (e: IOException) {
                    connectionLost()
                    Log.e(TAG, "disconnected", e)
                }
            }

            Log.i(PrinterService.TAG, Thread.currentThread().name)
        }

        private fun connectionLost() {
            //Device connection was lost
            mState = STATE_NONE
        }

        fun write(buffer: ByteArray?) {
            try {
                connectedOutputStream.write(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                this.bluetoothSocket.close()
                mState = STATE_NONE
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }
}


