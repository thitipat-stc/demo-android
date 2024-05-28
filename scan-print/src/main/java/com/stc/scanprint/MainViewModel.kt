package com.stc.scanprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stc.scanprint.models.Barcode

class MainViewModel(/*private val repository: ReceiveRepository*/) : ViewModel() {

    private val _itemList = MutableLiveData<ArrayList<Barcode>>()
    val itemList: LiveData<ArrayList<Barcode>> = _itemList

    fun addBarcode(barcode: Barcode) {
        val list = _itemList.value ?: arrayListOf()
        list.add(barcode)
        _itemList.value = list
        //adapter.notifyItemInserted(itemList.size - 1)
    }

    fun readBarcodes(): List<Barcode>? {
        return _itemList.value?.toList()
    }

    fun updateBarcode(barcode: Barcode, isChecked: Boolean) {
        val list = _itemList.value ?: return
        val index = list.indexOfFirst { it.id == barcode.id }
        if (index != -1) {
            list[index].isChecked = isChecked
            _itemList.value = list // Update LiveData
        }
    }

    fun deleteBarcode() {
        val list = _itemList.value ?: return
        val filteredList = list.filter { !it.isChecked } as ArrayList<Barcode>
        _itemList.value?.clear()
        _itemList.value = filteredList
    }

}