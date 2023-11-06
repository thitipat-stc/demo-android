package com.stc.scanprint

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.stc.scanprint.databinding.ActivityMainBinding
import com.stc.scanprint.fragment.BluetoothFragment
import com.stc.scanprint.models.Barcode
import com.stc.scanprint.utils.DiffCallback
import com.stc.scanprint.utils.Shared
import com.stc.scanprint.utils.ViewModelFactory
import java.util.Date

class MainActivity : AppCompatActivity(), View.OnKeyListener, View.OnClickListener, MainAdapter.OnEvent, BluetoothFragment.OnEvent {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvAppVersion.text = getString(R.string.txt_demo)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        initEvent()
        initViewModel()
        initAdapter()
    }

    private fun initEvent() {
        binding.edtScan1.requestFocus()
        binding.edtScan1.setOnKeyListener(this)
        binding.btnSettings.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)
        binding.btnPrint.setOnClickListener(this)
        Shared.setEdittextChange(binding.edtScan1, binding.tilScan1)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, ViewModelFactory(this)).get(MainViewModel::class.java)

        viewModel.response.observe(this, Observer {
            binding.edtScan1.selectAll()
            if (it != null) {
                val diffResult = DiffUtil.calculateDiff(DiffCallback(adapter.arrayList, it))
                diffResult.dispatchUpdatesTo(adapter)
                adapter.submitList(it)
            } else {
                clearActivity()
            }
        })
    }

    private fun initAdapter() {
        adapter = MainAdapter(this, this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = adapter
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        when (v?.id) {
            R.id.edtScan1 -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
                    val edtScan1 = binding.edtScan1.text.toString()
                    when {
                        edtScan1.isEmpty() -> {
                            binding.edtScan1.requestFocus()
                            binding.tilScan1.error = getString(R.string.txt_please_scan_barcode)
                            binding.tilScan1.isErrorEnabled = true
                        }
                        /*edtScan1.length > 12 -> {
                            binding.edtScan1.requestFocus()
                            binding.tilScan1.error = "Digit more than 12!!"
                            binding.tilScan1.isErrorEnabled = true
                        }*/

                        else -> {
                            insertItem(edtScan1)
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnSettings -> {
                /*val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)*/
            }

            R.id.btnClear -> {
                if (adapter.arrayList.size > 0) viewModel.delete()
            }

            R.id.btn_print -> {
                Shared.hideKeyboard(this)

                if (adapter.arrayList.size <= 0) {
                    Toast.makeText(this, "Please scan item", Toast.LENGTH_SHORT).show()
                    return
                }

                val printArr = ArrayList<Barcode>()

                adapter.arrayList.forEach {
                    if (it.isChecked) {
                        printArr.add(it)
                    }
                }

                if (printArr.size <= 0) {
                    Toast.makeText(this, "Please select item", Toast.LENGTH_SHORT).show()
                    return
                }

                val fm = supportFragmentManager.findFragmentByTag("fragment") as BluetoothFragment?
                if (fm != null && fm.isVisible) {
                    return
                }
                val bundle = Bundle()
                /*bundle.putString("transactionNo", viewModel.transactionNo.value)
                bundle.putString("partNo", partNo)
                bundle.putString("lot", lot)*/
                bundle.putParcelableArrayList("arrayList", printArr)
                val fragment = BluetoothFragment.newInstance()
                fragment.setEvent(this)
                fragment.arguments = bundle
                fragment.show(supportFragmentManager, "fragment")
            }
        }
    }

    private fun insertItem(barcode: String) {
        viewModel.insert((Barcode(barcode, Date())))
    }

    override fun onCheckBox(position: Int, barcode: String, timestamp: Date, isCheck: Boolean) {
        viewModel.update(position, Barcode(barcode, timestamp, isCheck))
    }

    private fun clearActivity() {
        binding.apply {
            tilScan1.error = null
            tilScan1.isErrorEnabled = false
            edtScan1.text?.clear()
            edtScan1.requestFocus()
        }
    }

    override fun clickItem(bool: Boolean) {
        if (bool) {
            if (adapter.arrayList.size > 0)
                if (BluetoothFragment.printedList.size > 0) {
                    viewModel.delete(BluetoothFragment.printedList)
                }
        }
    }
}