package com.stc.scanprint

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.stc.scanprint.databinding.ItemBinding
import com.stc.scanprint.databinding.ItemEmptyBinding
import com.stc.scanprint.models.Barcode
import com.stc.scanprint.utils.Shared
import java.util.Date

class MainAdapter(private val context: Context, private val onEvent: OnEvent) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var arrayList = ArrayList<Barcode>()

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_EMPTY -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemEmptyBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        }

        VIEW_TYPE_ITEM -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemBinding.inflate(inflater, parent, false)
            DetailViewHolder(binding/*, context*/, onEvent)
        }

        else -> throw IllegalArgumentException("Invalid view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            when (holder) {
                is EmptyViewHolder -> {}/*holder.bind()*/
                is DetailViewHolder -> holder.bind(arrayList[position], position)
                else -> throw IllegalArgumentException("Invalid ViewHolder")
            }
        }
    }

    override fun getItemViewType(position: Int): Int = if (arrayList.isEmpty()) {
        VIEW_TYPE_EMPTY
    } else {
        VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return when (val count = arrayList.size) {
            0 -> 1
            else -> count
        }
    }

    fun submitList(newData: ArrayList<Barcode>) {
        val needNotifyDataChanged = arrayList.size == 0
        arrayList.clear()
        arrayList.addAll(newData)
        if (needNotifyDataChanged) notifyItemChanged(arrayList.size)
    }

    inner class EmptyViewHolder(binding: ItemEmptyBinding) : RecyclerView.ViewHolder(binding.root)

    inner class DetailViewHolder(private val binding: ItemBinding, onEvent: OnEvent) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            barcode: Barcode,
            position: Int
        ) {
            with(binding) {
                tvBarcode.text = barcode.barcode
                tvTime.text = Shared.convertDate(barcode.timestamp)
                checkbox.isChecked = barcode.isChecked

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gainsboro))
                        onEvent.onCheckBox(barcode, true)
                    } else {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                        onEvent.onCheckBox(barcode, false)
                    }
                }
            }
        }
    }

    interface OnEvent {
        fun onCheckBox(barcode: Barcode, isCheck: Boolean)
    }
}