package com.stc.printbt.fragment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stc.printbt.R
import com.stc.printbt.databinding.FragmentBluetoothBinding
import com.stc.printbt.models.Barcode
import com.stc.printbt.models.PairedData
import com.stc.printbt.utils.Shared
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBluetoothBinding

    private var myUUID: UUID? = null
    private var pairedDevices: Set<BluetoothDevice>? = null
    var threadConnectBTDevice: ThreadConnectBTDevice? = null
    var threadConnected: ThreadConnected? = null

    private var arrayList = ArrayList<Barcode>()
    private var barcode = ""
    private var chk = 0

    companion object {
        fun newInstance(): BluetoothFragment {
            return BluetoothFragment()
        }
        private const val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        threadConnected?.cancel()
        threadConnectBTDevice?.cancel()
    }

    /*override fun onDestroy() {
        super.onDestroy()
        threadConnected?.cancel()
        threadConnectBTDevice?.cancel()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (activity is MainActivity) {
            onEvent = (activity as OnEvent?)!!
        }*/

        val bundle = this.arguments
        if (bundle != null) {
            arrayList = bundle.getParcelableArrayList("arrayList")!!
            chk = arrayList.size

            /*transactionNo = bundle.getString("transactionNo") ?: ""
            partNo = bundle.getString("partNo") ?: ""
            lot = bundle.getString("lot") ?: ""*/
        }

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        initEvent()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViewModel()
    }

    private fun initEvent() {
        binding.spnPrinterAddress.inputType = InputType.TYPE_NULL
        val adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, printerList())
        binding.spnPrinterAddress.setAdapter(adapter)
        binding.spnPrinterAddress.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as PairedData
            if (selectedItem.value != null) {
                threadConnectBTDevice = ThreadConnectBTDevice(selectedItem.value!!)
                threadConnectBTDevice?.start()
            }
        }

        binding.btnDisconnect.setOnClickListener {
            threadConnected?.cancel()
            threadConnectBTDevice?.cancel()
        }

        binding.btnPrint.setOnClickListener {
            barcode = "\u001BA\n" +
                    "\u001BA3V+00000H+0000\u001BCS4\u001B#F5\u001BA1V00464H0400\n" +
                    "\u001BZ\n" +
                    "\u001BA\u001BPS\u001BWKLabel - 55x60\n" +
                    "\u001B%0\u001BH0010\u001BV00164\u001BFW04H0377\n" +
                    "\u001B%0\u001BH0010\u001BV00054\u001BFW04H0377\n" +
                    "\u001B%0\u001BH0010\u001BV00025\u001BP02\u001BRH0,SATO0.ttf,1,022,023,Android Scan Print Demo\n" +
                    "\u001B%0\u001BH0010\u001BV00076\u001BP02\u001BRH0,SATO0.ttf,0,057,063,^1$\n" +
                    "\u001B%0\u001BH0010\u001BV00174\u001BP02\u001BRH0,SATO0.ttf,1,020,020,Description:\n" +
                    "\u001B%0\u001BH0010\u001BV00198\u001BP02\u001BRH0,SATO0.ttf,0,020,020,SATO Auto-ID (Thailand) Co., Ltd.\n" +
                    "\u001B%0\u001BH0010\u001BV00217\u001BP02\u001BRH0,SATO0.ttf,0,020,020,292/1 Moo 1 Theparak Road, Tumbol\n" +
                    "\u001B%0\u001BH0010\u001BV00236\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Theparak, Amphur Muang,\n" +
                    "\u001B%0\u001BH0010\u001BV00255\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Samutprakarn 10270, Thailand\n" +
                    "\u001B%0\u001BH0148\u001BV00286\u001B2D30,L,05,1,0\u001BDN^2$,^1$\n" +
                    "\u001B%0\u001BH0008\u001BV00398\u001BP02\u001BRH0,SATO0.ttf,0,020,020,^3$\n" +
                    "\u001BQ1\n" +
                    "\u001BZ"

            arrayList.forEach {
                Handler(Looper.getMainLooper()).postDelayed({
                    sendToPrint2(it)
                },200)
            }
        }
    }

    private fun sendToPrint2(scanData: Barcode) {
        val newDetail = barcode
            .replace("^1$", scanData.barcode)
            .replace("^2$", scanData.barcode.length.toString())
            .replace("^3$", Shared.convertDate(scanData.timestamp))
        val detailBytes = newDetail.toByteArray()
        sendToPrinter(detailBytes)
    }

    private fun printerList(): java.util.ArrayList<PairedData> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevices = bluetoothAdapter.bondedDevices
        val list = java.util.ArrayList<PairedData>()
        //list.add(PairedData("Select Printer", null))
        (pairedDevices as MutableSet<BluetoothDevice>?)!!.forEach {
            list.add(PairedData(it.name, it))
        }
        return list
    }

    private fun setBTStatus(bool: Boolean) {
        if (bool) {
            binding.ivStatus.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_usb_connect)
            )
            binding.spnPrinterAddress.isEnabled = false
            binding.btnDisconnect.isEnabled = true
            binding.btnPrint.isEnabled = true
        } else {
            binding.ivStatus.setImageDrawable(
                ContextCompat.getDrawable(this@BluetoothFragment.requireContext(), R.drawable.ic_usb_disconnect)
            )
            binding.spnPrinterAddress.isEnabled = true
            binding.btnDisconnect.isEnabled = false
            binding.btnPrint.isEnabled = false
            binding.spnPrinterAddress.requestFocus()
            binding.spnPrinterAddress.setSelection(0)
        }
    }

    inner class ThreadConnectBTDevice(bluetoothDevice: BluetoothDevice) : Thread() {
        private var bluetoothSocket: BluetoothSocket? = null

        init {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(myUUID)
        }

        override fun run() {
            var success = false
            try {
                bluetoothSocket!!.connect()
                success = true
            } catch (e: IOException) {
                e.printStackTrace()
                val eMessage: String? = e.message

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@BluetoothFragment.requireContext(),
                        "Something wrong bluetoothSocket.connect():\n$eMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                    setBTStatus(false)
                }

            }

            if (success) {
                val msgConnected = "Connect successful"
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@BluetoothFragment.requireContext(), msgConnected, Toast.LENGTH_SHORT).show()
                    /*binding.tilPrinterAddress.helperText = msgConnected
                    binding.tilPrinterAddress.isErrorEnabled = true*/
                    setBTStatus(true)
                }

                startThreadConnected(bluetoothSocket!!)
            }
        }

        fun cancel() {
            try {
                bluetoothSocket!!.close()
                threadConnectBTDevice = null
                Toast.makeText(this@BluetoothFragment.requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
                /*binding.tilPrinterAddress.helperText = ""
                binding.tilPrinterAddress.isErrorEnabled = false*/
                setBTStatus(false)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Cancel socket", "${e.printStackTrace()}")
            }
        }
    }

    inner class ThreadConnected(connectedBluetoothSocket: BluetoothSocket) : Thread() {

        private var connectedInputStream: InputStream?
        private var connectedOutputStream: OutputStream?
        private var running = true

        init {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = connectedBluetoothSocket.inputStream
                out = connectedBluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ThreadConnected init", "${e.printStackTrace()}")
            }
            connectedInputStream = `in`
            connectedOutputStream = out
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (running) {
                try {
                    bytes = connectedInputStream!!.read(buffer)
                    val strReceived = String(buffer, 0, bytes)
                    val msgReceived = "$bytes bytes received:$strReceived"
                    CoroutineScope(Dispatchers.Main).launch {
                        //Toast.makeText(applicationContext, msgReceived, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    val msgConnectionLost = "Connection lost: ${e.message}".trimIndent()

                    CoroutineScope(Dispatchers.Main).launch {
                        //Toast.makeText(applicationContext, msgConnectionLost, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun write(buffer: ByteArray?) {
            try {
                connectedOutputStream?.write(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Write stream", "${e.printStackTrace()}")
            }
        }

        fun cancel() {
            try {
                running = false
                if (connectedInputStream != null) {
                    connectedInputStream?.close()
                    connectedInputStream = null
                }
                if (connectedOutputStream != null) {
                    connectedOutputStream?.close()
                    connectedOutputStream = null
                }
                threadConnected = null
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Cancel stream", "${e.printStackTrace()}")
            }
        }
    }

    private fun startThreadConnected(socket: BluetoothSocket) {
        threadConnected = ThreadConnected(socket)
        threadConnected!!.start()
    }

    private fun sendToPrinter(bytes: ByteArray) {
        if (threadConnected != null) {
            threadConnected!!.write(bytes)
            chk--
        }
    }

}