package com.stc.printbt.print

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.content.ContextCompat
import com.stc.printbt.R
import com.stc.printbt.databinding.ActivityPrintBinding
import com.stc.printbt.models.PairedData
import com.thitipat.printerservice.PrinterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class PrintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrintBinding

    private var printerService = PrinterService()
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
                setBTStatus(false)
                try {
                    printerService.setPrinter(selectedItem.value!!) //bluetoothDevice
                    printerService.connect()
                    setBTStatus(true)
                } catch (e: Exception) {
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDisconnect.setOnClickListener {
            try {
                printerService.disconnect()
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setBTStatus(false)
            }
        }

        binding.btnPrint.setOnClickListener {
            val barcode = "\u001BA\n" +
                    "\u001BA3V+00000H+0000\u001BCS4\u001B#F5\n" +
                    "\u001BA1V00599H0440\n" +
                    "\u001BZ\n" +
                    "\u001BA\u001BPS\u001BWKw55h75.lbl\n" +
                    "\u001B%0\u001BH0024\u001BV00024\u001BFW0404V0525H0397\n" +
                    "\u001B%0\u001BH0268\u001BV00411\u001B2D30,M,04,1,0\u001BDN0036,ABC-9721-8DSF,SATO CL4NX,A01-24,5743\n" +
                    "\u001B%0\u001BH0116\u001BV00116\u001BBG01054>GABCD0123456ABCD\n" +
                    "\u001B%0\u001BH0176\u001BV00171\u001BP02\u001BRDB@0,010,010,ABCD0123456ABCD\n" +
                    "\u001B%0\u001BH0082\u001BV00070\u001BP02\u001BRH0,SATO0.ttf,1,033,034,ZAN-NSK89-S89Z\n" +
                    "\u001B%0\u001BH0038\u001BV00477\u001BP02\u001BRH0,SATO0.ttf,0,020,021,Timestamp : \u001B%0\u001BH0039\u001BV00411\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Balance : \u001B%0\u001BH0037\u001BV00338\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Location no. : \u001B%0\u001BH0040\u001BV00260\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Name : \u001B%0\u001BH0150\u001BV00189\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Print Kanban\n" +
                    "\u001B%0\u001BH0239\u001BV00382\u001BFW04V00164\n" +
                    "\u001B%0\u001BH0029\u001BV00457\u001BFW04H0212\n" +
                    "\u001B%0\u001BH0029\u001BV00381\u001BFW04H0380\n" +
                    "\u001B%0\u001BH0029\u001BV00305\u001BFW04H0380\n" +
                    "\u001B%0\u001BH0029\u001BV00230\u001BFW04H0380\n" +
                    "\u001B%0\u001BH0122\u001BV00260\u001BP02\u001BRH0,SATO0.ttf,0,020,020,Airbag Module\n" +
                    "\u001B%0\u001BH0144\u001BV00411\u001BP02\u001BRH0,SATO0.ttf,0,020,020,225\n" +
                    "\u001B%0\u001BH0026\u001BV00557\u001BP02\u001BRH0,SATO0.ttf,0,020,020,SATO AUTO-ID (THAILAND) CO. LTD\n" +
                    "\u001B%0\u001BH0194\u001BV00338\u001BP02\u001BRH0,SATO0.ttf,0,020,020,A02-827\n" +
                    "\u001B%0\u001BH0037\u001BV00511\u001BP02\u001BRH0,SATO0.ttf,0,020,020,1:25:00 PM\n" +
                    "\u001BQ1\n" +
                    "\u001BZ"
            val detailBytes = barcode.toByteArray()
            printerService.sendCommandToPrint(detailBytes)
        }
    }

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
            binding.btnPrint.isEnabled = true
        } else {
            binding.ivStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_usb_disconnect))
            binding.spnPrinterAddress.isEnabled = true
            binding.btnDisconnect.isEnabled = false
            binding.btnPrint.isEnabled = false
            binding.spnPrinterAddress.requestFocus()
            binding.spnPrinterAddress.setSelection(0)
        }
    }
}