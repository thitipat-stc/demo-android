package com.stc.onecheck.models

data class AnswerSheet(
    val seq: Int,
    val personalId: String,
    val firstName: String,
    val lastName: String,
    val place: String,
    val room: String,
    val date: String,
    val idCardNumber: String,
    val setOfExams: String,
    val locationCode: String,
)

data class Envelope(
    val seq: Int,
    val envelopeId: String, //รหัสซอง
    val personalId: String,
    val envelopeName: String,
)

data class Box(
    val seq: Int,
    val boxId: String, //รหัสกล่อง
    val envelopeId: String,
    val boxName: String,
)

data class Pallet(
    val seq: Int,
    val palletId: String, //รหัสพาเลท
    val boxId: String,
    val palletName: String,
)
