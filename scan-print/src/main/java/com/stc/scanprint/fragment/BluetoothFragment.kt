package com.stc.scanprint.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stc.scanprint.R
import com.stc.scanprint.databinding.FragmentBluetoothBinding
import com.stc.scanprint.models.Barcode
import com.stc.scanprint.models.PairedData
import com.stc.scanprint.utils.Shared
import com.ttpkk.library.PrinterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBluetoothBinding

    private var alertDialog: AlertDialog? = null

    private var printerService: PrinterService? = null
    private var pairedDevices: Set<BluetoothDevice>? = null

    private var arrayList = ArrayList<Barcode>()
    private lateinit var onEvent: OnEvent

    companion object {
        fun newInstance(): BluetoothFragment {
            return BluetoothFragment()
        }

        var printedList = ArrayList<Barcode>()
    }

    fun setEvent(onEvent: OnEvent) {
        this.onEvent = onEvent
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        try {
            printerService?.disconnect()
        } catch (e: Exception) {
        }
        onEvent.clickItem(true)
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
            arrayList = bundle.parcelableArrayList("arrayList")!!

            /*transactionNo = bundle.getString("transactionNo") ?: ""
            partNo = bundle.getString("partNo") ?: ""
            lot = bundle.getString("lot") ?: ""*/
        }

        initPermission()
        printerService = PrinterService(requireContext())
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Toast.makeText(requireContext(), "Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Toast.makeText(requireContext(), "${it.key} = ${it.value}", Toast.LENGTH_SHORT).show()
        }
    }

    private inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        initEvent()
        return binding.root
    }

    private fun initEvent() {
        binding.spnPrinterAddress.inputType = InputType.TYPE_NULL

        val adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, printerList())
        binding.spnPrinterAddress.setAdapter(adapter)
        binding.spnPrinterAddress.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as PairedData
            if (selectedItem.value != null) {
                setBTStatus(false)
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            setLoadingDialog(true)
                        }
                        try {
                            printerService?.setPrinter(selectedItem.value!!) //bluetoothDevice
                            printerService?.connect()

                            withContext(Dispatchers.Main) {
                                setLoadingDialog(false)
                                setBTStatus(true)
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                                setBTStatus(false)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDisconnect.setOnClickListener {
            try {
                printerService?.disconnect()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setBTStatus(false)
            }
        }

        binding.btnPrint.setOnClickListener {
            arrayList.forEach {
                Handler(Looper.getMainLooper()).postDelayed({
                    sendToPrint2(it)
                }, 200)
            }
        }
    }

    private fun sendToPrint2(scanData: Barcode) {
        try {
            val format = "\u001BA\n" +
                    "\u001BA3V+00000H+0000\u001BCS4\u001B#F5\n" +
                    "\u001BA1V00480H0440\n" +
                    "\u001BZ\n" +
                    "\u001BA\u001BPS\u001BWKLabel - 53x58\n" +
                    "\u001B%0\u001BH0099\u001BV00054\u001BP02\u001BRH0,SATO0.ttf,1,024,028,Android Print Demo\n" +
                    "\u001B%0\u001BH0156\u001BV00130\u001B2D30,L,05,1,0\u001BDN^2$,^1$\n" +
                    "\u001B%0\u001BH0085\u001BV00395\u001BP02\u001BRH0,SATO0.ttf,0,022,023,^3$\n" +
                    "\u001BQ1\n" +
                    "\u001BZ"

            val newDetail = format
                .replace("^1$", scanData.barcode)
                .replace("^2$", scanData.barcode.length.toString())
                .replace("^3$", Shared.convertDate(scanData.timestamp))

            val detailBytes = newDetail.toByteArray()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    printerService?.sendCommandToPrint(detailBytes)
                    printedList.add(scanData)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                        setBTStatus(false)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
            setBTStatus(false)
        }
    }

    @SuppressLint("MissingPermission")
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

    private fun setLoadingDialog(bool: Boolean, str: String? = null) {
        if (bool) {
            if (alertDialog?.isShowing == true) return
            alertDialog = Shared.setLoadingDialog(requireContext(), str ?: "")
            alertDialog?.show()
        } else {
            alertDialog?.dismiss()
            return
        }
    }

    interface OnEvent {
        fun clickItem(bool: Boolean)
    }
}