package com.stc.scanprint.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stc.scanprint.R
import com.stc.scanprint.databinding.FragmentWifiBinding
import com.stc.scanprint.models.Barcode
import com.stc.scanprint.utils.Shared
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class WifiFragment(private val list: List<Barcode>) : BottomSheetDialogFragment(), View.OnClickListener, View.OnKeyListener {

    private lateinit var binding: FragmentWifiBinding
    private var alertDialog: AlertDialog? = null

    private lateinit var output: PrintWriter
    private lateinit var input: BufferedReader

    private var sharedPreferences: SharedPreferences? = null
    private var address = "192.168.10.61"
    private var port = "1024"

    private var format = "\u001BA\n" +
            "\u001BA3V+00000H+0000\u001BCS6\u001B#F5\n" +
            "\u001BA1V00440H0440\n" +
            "\u001BZ\n" +
            "\u001BA\u001BPS\u001BWKLabel\n" +
            "\u001B%0\u001BH0168\u001BV00127\u001B2D30,L,05,1,0\u001BDN^2$,^1$\n" +
            "\u001B%0\u001BH0076\u001BV00055\u001BP02\u001BRH0,SATO0.ttf,0,033,034,Android Print Demo\n" +
            "\u001B%0\u001BH0105\u001BV00364\u001BP02\u001BRH0,SATO0.ttf,0,022,023,^3$\n" +
            "\u001B%0\u001BH0150\u001BV00260\u001BP02\u001BRH0,SATO0.ttf,0,022,023,^1$\n" +
            "\u001BQ1\n" +
            "\u001BZ"

    companion object {
        fun newInstance(list: List<Barcode>): WifiFragment {
            return WifiFragment(list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("MyPreferences", MODE_PRIVATE)
        address = sharedPreferences?.getString("key_address", "192.168.10.61").toString()
        port = sharedPreferences?.getString("key_port", "1024").toString()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWifiBinding.inflate(inflater, container, false)
        initEvent()

        binding.edtPrinterAddress.setText(address)
        binding.edtPrinterPort.setText(port)
        binding.edtPrinterAddress.setSelection(binding.edtPrinterAddress.length())

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {

        super.onCancel(dialog)

        val editor = sharedPreferences?.edit()
        val address = binding.edtPrinterAddress.text.toString()
        val port = binding.edtPrinterPort.text.toString()
        if (address.isNotEmpty()) editor?.putString("key_address", address)
        if (port.isNotEmpty()) editor?.putString("key_port", port)
        editor?.apply()

    }

    private fun initEvent() {
        binding.edtPrinterAddress.requestFocus()
        binding.edtPrinterAddress.setOnKeyListener(this)
        binding.edtPrinterPort.setOnKeyListener(this)
        binding.btnPrint.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            /*R.id.btn_print -> {
                val address = binding.edtPrinterAddress.text.toString()
                val port = binding.edtPrinterPort.text.toString()

                list.forEach {
                    val thread: Thread = object : Thread() {
                        override fun run() {
                            val socket: Socket
                            try {
                                socket = Socket(address, port.toInt())

                                output = PrintWriter(socket.getOutputStream())
                                input = BufferedReader(InputStreamReader(socket.getInputStream()))

                                val newFormat = format
                                    .replace("^1$", it.barcode)
                                    .replace("^2$", it.barcode.length.toString())
                                    .replace("^3$", Shared.convertDate(it.timestamp))

                                output.write(newFormat)
                                output.flush()

                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    thread.start()
                }
            }*/

            R.id.btn_print -> {

                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)

                address = binding.edtPrinterAddress.text.toString()
                port = binding.edtPrinterPort.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    val socket: Socket
                    try {
                        socket = Socket(address, port.toInt())

                        output = PrintWriter(socket.getOutputStream())
                        input = BufferedReader(InputStreamReader(socket.getInputStream()))

                        list.forEach label@{
                            val newFormat = format
                                .replace("^1$", it.barcode)
                                .replace("^2$", it.barcode.length.toString())
                                .replace("^3$", Shared.convertDate(it.timestamp))

                            output.write(newFormat)
                            output.flush()
                        }

                    } catch (e: IOException) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        when (v?.id) {
            R.id.edt_printer_address -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
                    val address = binding.edtPrinterAddress.text.toString()
                    when {
                        address.isEmpty() -> {
                            binding.tilPrinterAddress.error = "Please fill printer address"
                            binding.tilPrinterAddress.isErrorEnabled = true
                        }

                        else -> {
                            binding.edtPrinterPort.requestFocus()
                        }
                    }
                    return true
                }
            }

            R.id.edt_printer_port -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
                    val address = binding.edtPrinterAddress.text.toString()
                    val port = binding.edtPrinterPort.text.toString()
                    when {
                        address.isEmpty() -> {
                            binding.tilPrinterAddress.error = "Please fill printer address"
                            binding.tilPrinterAddress.isErrorEnabled = true
                        }

                        port.isEmpty() -> {
                            binding.tilPrinterPort.error = "Please fill printer port"
                            binding.tilPrinterPort.isErrorEnabled = true
                        }

                        else -> {
                            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view?.windowToken, 0)
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    /*private fun setLoadingDialog(bool: Boolean, str: String? = null) {
        if (bool) {
            if (alertDialog?.isShowing == true) return
            alertDialog = Shared.setLoadingDialog(requireContext(), str ?: "")
            alertDialog?.show()
        } else {
            alertDialog?.dismiss()
            return
        }
    }

    private fun setBTStatus(bool: Boolean) {
        if (bool) {
            binding.ivStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_usb_connect))
            binding.tilPrinterAddress.isEnabled = false
            binding.tilPrinterPort.isEnabled = false
            binding.btnPrint.isEnabled = true
        } else {
            binding.ivStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_usb_disconnect))
            binding.tilPrinterAddress.isEnabled = true
            binding.tilPrinterPort.isEnabled = true
            binding.btnPrint.isEnabled = false
            binding.tilPrinterAddress.requestFocus()
        }
    }*/

    /*inner class Thread1(private val address: String, private val port: Int) : Runnable {
        override fun run() {
            val socket: Socket
            try {
                socket = Socket(address, port)

                output = PrintWriter(socket.getOutputStream())
                input = BufferedReader(InputStreamReader(socket.getInputStream()))

                CoroutineScope(Dispatchers.Main).launch {
                    setBTStatus(true)
                    Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                }

                thread2 = Thread(Thread2(address, port))
                thread2.start()

            } catch (e: IOException) {
                CoroutineScope(Dispatchers.Main).launch {
                    setBTStatus(false)
                    Toast.makeText(context, "Connected failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class Thread2(private val address: String, private val port: Int) : Runnable {
        override fun run() {
            while (true) {
                try {
                    val message = input.readLine()

                    if (message != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Server: $message", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (thread1.isAlive) {
                            thread1 = Thread(Thread1(address, port))
                            thread1.start()
                            return
                        }
                    }
                } catch (e: IOException) {
                    CoroutineScope(Dispatchers.Main).launch {
                        setBTStatus(false)
                        Toast.makeText(context, "Connected failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    inner class Thread3(private val format: String) : Runnable {
        override fun run() {
            output.write(format)
            output.flush()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Send: $format", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

}