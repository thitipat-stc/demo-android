package com.stc.onecheck.utils

import com.stc.onecheck.models.Box
import kotlin.collections.ArrayList

class Mockup {
    companion object {
        fun getBox(): ArrayList<Box> {
            val array = ArrayList<Box>()
            array.add(Box(1, "66020001", "66010001", "กล่องที่ 1"))
            array.add(Box(2, "66020001", "66010002", "กล่องที่ 1"))
            array.add(Box(3, "66020001", "66010003", "กล่องที่ 1"))
            array.add(Box(4, "66020001", "66010004", "กล่องที่ 1"))
            array.add(Box(5, "66020001", "66010005", "กล่องที่ 1"))
            array.add(Box(6, "66020002", "66010006", "กล่องที่ 2"))
            array.add(Box(7, "66020002", "66010007", "กล่องที่ 2"))
            array.add(Box(8, "66020002", "66010008", "กล่องที่ 2"))
            array.add(Box(9, "66020002", "66010009", "กล่องที่ 2"))
            array.add(Box(10, "66020002", "66010010", "กล่องที่ 2"))
            return array
        }
    }
}