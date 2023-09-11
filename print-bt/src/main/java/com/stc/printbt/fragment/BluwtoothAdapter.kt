package com.stc.printbt.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/*
class ScanAdapter(private val context: Context, private val onEvent: OnEvent) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var arrayList = ArrayList<ScanList>()

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_EMPTY -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScanEmptyBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        }

        VIEW_TYPE_ITEM -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScanBinding.inflate(inflater, parent, false)
            DetailViewHolder(binding*/
/*, context*//*
, onEvent)
        }

        else -> throw IllegalArgumentException("Invalid view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            when (holder) {
                is EmptyViewHolder -> {}*/
/*holder.bind()*//*

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

    fun clearData() {
        val size: Int = arrayList.size
        arrayList.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun submitList(newData: ArrayList<ScanList>) {
        val needNotifyDataChanged = arrayList.size == 0
        arrayList.clear()
        arrayList.addAll(newData)
        if (needNotifyDataChanged) notifyItemChanged(arrayList.size)
    }

    inner class EmptyViewHolder(binding: ItemScanEmptyBinding) : RecyclerView.ViewHolder(binding.root)

    inner class DetailViewHolder(private val binding: ItemScanBinding, onEvent: OnEvent) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            scanList: ScanList,
            position: Int
        ) {
            with(binding) {
                val index = context.getString(R.string.txt_no_format, position + 1)
                val serial = scanList.serial
                val location = scanList.location
                tvSerial.text = serial
                tvLocation.text = location

                btnDelete.setOnClickListener{
                    onEvent.clickItem(position, serial)
                }
            }
        }
    }

    interface OnEvent {
        fun clickItem(position: Int, partNo: String)
    }
}

class DiffCallback(
    private val oldList: ArrayList<ScanList>,
    private val newList: ArrayList<ScanList>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].serial == newList[newItemPosition].serial
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}*/
/*
class ScanAdapter(private val context: Context, private val onEvent: OnEvent) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var arrayList = ArrayList<ScanList>()

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_EMPTY -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScanEmptyBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        }

        VIEW_TYPE_ITEM -> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScanBinding.inflate(inflater, parent, false)
            DetailViewHolder(binding*/
/*, context*//*
, onEvent)
        }

        else -> throw IllegalArgumentException("Invalid view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            when (holder) {
                is EmptyViewHolder -> {}*/
/*holder.bind()*//*

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

    fun clearData() {
        val size: Int = arrayList.size
        arrayList.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun submitList(newData: ArrayList<ScanList>) {
        val needNotifyDataChanged = arrayList.size == 0
        arrayList.clear()
        arrayList.addAll(newData)
        if (needNotifyDataChanged) notifyItemChanged(arrayList.size)
    }

    inner class EmptyViewHolder(binding: ItemScanEmptyBinding) : RecyclerView.ViewHolder(binding.root)

    inner class DetailViewHolder(private val binding: ItemScanBinding, onEvent: OnEvent) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            scanList: ScanList,
            position: Int
        ) {
            with(binding) {
                val index = context.getString(R.string.txt_no_format, position + 1)
                val serial = scanList.serial
                val location = scanList.location
                tvSerial.text = serial
                tvLocation.text = location

                btnDelete.setOnClickListener{
                    onEvent.clickItem(position, serial)
                }
            }
        }
    }

    interface OnEvent {
        fun clickItem(position: Int, partNo: String)
    }
}

class DiffCallback(
    private val oldList: ArrayList<ScanList>,
    private val newList: ArrayList<ScanList>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].serial == newList[newItemPosition].serial
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}*/
