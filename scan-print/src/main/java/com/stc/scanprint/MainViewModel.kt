package com.stc.scanprint

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stc.scanprint.fragment.BluetoothFragment
import com.stc.scanprint.models.Barcode

class MainViewModel(/*private val repository: ReceiveRepository*/) : ViewModel() {

    private var arrayList = ArrayList<Barcode>()
    var response = MutableLiveData<ArrayList<Barcode>>()

    fun insert(barcode: Barcode) {
        arrayList.add(barcode)
        response.postValue(arrayList)
    }

    fun update(position: Int, barcode: Barcode) {
        try {
            arrayList[position] = barcode
            response.postValue(arrayList)
        }catch (e: Exception){

        }
    }

    fun delete() {
        arrayList.clear()
        response.postValue(arrayList)
    }

    fun delete(scanBarcode: ArrayList<Barcode>) {
        scanBarcode.forEach {
            arrayList.remove(it)
        }
        response.postValue(arrayList)
    }
}