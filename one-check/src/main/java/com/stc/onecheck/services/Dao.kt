package com.stc.onecheck.services

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ControlResponse(
    @SerializedName("message")
    @Expose var message: String? = null,
)