package com.leonardo.drinkslab.util

import android.util.Base64
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.leonardo.drinkslab.data.model.QRCode
import java.nio.charset.StandardCharsets

object QRDecode {
    private val TAG: String = "AppDebug"

    fun decode(qrCode: String): QRCode {
        val data: ByteArray = Base64.decode(qrCode, Base64.DEFAULT)
        val qrCodeDecoded = String(data, StandardCharsets.US_ASCII)

        Log.d(TAG, "QRDecode, QR decoded: $qrCodeDecoded")

        val mapper = jacksonObjectMapper()
        var qrCode: QRCode = mapper.readValue<QRCode>("""$qrCodeDecoded""", QRCode::class.java)

        //Log.d(TAG, "VisitsFragment, DataState, Infovisita: $infoVisita")
        return qrCode
    }
}