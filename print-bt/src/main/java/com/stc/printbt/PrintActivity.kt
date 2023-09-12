package com.stc.printbt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.stc.printbt.databinding.ActivityPrintBinding
import com.stc.printbt.models.PairedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.UUID

class PrintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrintBinding

    private var printerObject = PrinterObject()
    private var pairedDevices: Set<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPermission()
        initEvent()

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
            Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Toast.makeText(this, "${it.key} = ${it.value}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initEvent() {
        binding.spnPrinterAddress.inputType = InputType.TYPE_NULL
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, printerList())
        binding.spnPrinterAddress.setAdapter(adapter)
        binding.spnPrinterAddress.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as PairedData
            if (selectedItem.value != null) {
                try {
                    printerObject.setPrinter(selectedItem.value!!) //bluetoothDevice
                    printerObject.connect()
                } catch (e: Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDisconnect.setOnClickListener {
            printerObject.disconnect()
        }
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
}