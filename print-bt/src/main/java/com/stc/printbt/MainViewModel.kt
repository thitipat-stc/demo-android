package com.stc.printbt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stc.printbt.models.Barcode

class MainViewModel(/*private val repository: ReceiveRepository*/) : ViewModel() {

    var arrayList = ArrayList<Barcode>()
    var response = MutableLiveData<ArrayList<Barcode>>()

    fun insert(barcode: Barcode) {
        arrayList.add(barcode)
        response.postValue(arrayList)
    }

    fun update(position: Int, barcode: Barcode) {
        arrayList[position] = barcode
        response.postValue(arrayList)
    }

    fun delete() {
        arrayList.clear()
        response.postValue(arrayList)
    }
}