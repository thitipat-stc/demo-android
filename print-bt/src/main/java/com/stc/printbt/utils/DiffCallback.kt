package com.stc.printbt.utils

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.stc.printbt.models.Barcode

class DiffCallback(
    private val oldList: ArrayList<Barcode>,
    private val newList: ArrayList<Barcode>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].barcode == newList[newItemPosition].barcode
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        // Check if only the isChecked property has changed
        return if (oldItem.isChecked != newItem.isChecked) {
            Bundle().apply {
                putBoolean("isChecked", newItem.isChecked)
            }
        } else {
            null
        }
    }
}