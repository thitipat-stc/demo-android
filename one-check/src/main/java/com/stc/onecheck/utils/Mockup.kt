package com.stc.onecheck.utils

import com.stc.onecheck.models.Box
import kotlin.collections.ArrayList

class Mockup {
    companion object {
        fun getBox(): ArrayList<Box> {
            val array = ArrayList<Box>()
            array.add(Box(1, "12345", "IN12345", ""))
            array.add(Box(2, "67890", "IN67890", ""))
            return array
        }
    }
}