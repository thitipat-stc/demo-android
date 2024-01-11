package com.stc.copylabel

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.stc.copylabel.databinding.ActivityMainBinding
import com.ttpkk.library.PrinterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var alertDialog: AlertDialog? = null

    private var printerService = PrinterService(this)
    private var pairedDevices: Set<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvAppVersion.text = getString(R.string.txt_demo)

        initPermission()
        initEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        printerService.disconnect()
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Toast.makeText(this, "${it.key} = ${it.value}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initEvent() {
        binding.spnPrinterAddress.inputType = InputType.TYPE_NULL

        Shared.setEdittextChange(binding.edtScan1, binding.tilScan1, binding.btnPrint)

        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, printerList())
        binding.spnPrinterAddress.setAdapter(adapter)
        binding.spnPrinterAddress.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as PairedData
            if (selectedItem.value != null) {
                setBTStatus(false)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) {
                            setLoadingDialog(true)
                        }

                        printerService.setPrinter(selectedItem.value!!) //bluetoothDevice
                        printerService.connect()

                        withContext(Dispatchers.Main) {
                            setLoadingDialog(false)
                            setBTStatus(true)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                            setLoadingDialog(false)
                        }
                    }
                }
            }
        }

        binding.btnDisconnect.setOnClickListener {
            try {
                printerService.disconnect()
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                Shared.hideKeyboard(this)
                setBTStatus(false)
            }
        }

        binding.edtScan1.setOnKeyListener { view, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent!!.action == KeyEvent.ACTION_UP) {
                Shared.hideKeyboard(this)
                val barcode = binding.edtScan1.text.toString()
                printLabel(barcode)
            }
            false
        }

        binding.btnPrint.setOnClickListener {
            Shared.hideKeyboard(this)
            val barcode = binding.edtScan1.text.toString()
            printLabel(barcode)
        }
    }

    private fun printLabel(barcode: String) {
        if (barcode.isEmpty()) {
            binding.tilScan1.error = getString(R.string.txt_please_scan_barcode)
            binding.tilScan1.isErrorEnabled = true
            return
        }

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
            .replace("^1$", barcode)
            .replace("^2$", barcode.length.toString())
            .replace("^3$", Shared.convertDate(Date()))

        val detailBytes = newDetail.toByteArray()
        CoroutineScope(Dispatchers.IO).launch {
            printerService.sendCommandToPrint(detailBytes)
            withContext(Dispatchers.Main) {
                binding.edtScan1.requestFocus()
                binding.edtScan1.selectAll()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun printerList(): java.util.ArrayList<PairedData> {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
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
            binding.ivStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_usb_connect))
            binding.spnPrinterAddress.isEnabled = false
            binding.btnDisconnect.isEnabled = true
            binding.btnPrint.isEnabled = false
            binding.edtScan1.requestFocus()
            binding.edtScan1.text?.clear()
        } else {
            binding.ivStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_usb_disconnect))
            binding.spnPrinterAddress.isEnabled = true
            binding.btnDisconnect.isEnabled = false
            binding.btnPrint.isEnabled = false
            binding.spnPrinterAddress.requestFocus()
            binding.spnPrinterAddress.setSelection(0)

            Shared.clearInputAlert(binding.edtScan1, binding.tilScan1)

            binding.spnPrinterAddress.requestFocus()
            binding.edtScan1.text?.clear()
        }
    }

    private fun setLoadingDialog(bool: Boolean, str: String? = null) {
        if (bool) {
            if (alertDialog?.isShowing == true) return
            alertDialog = Shared.setLoadingDialog(this, str ?: "")
            alertDialog?.show()
        } else {
            alertDialog?.dismiss()
            return
        }
    }
}