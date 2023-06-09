package com.example.fp_v2

import android.content.Context
import android.widget.Toast
import java.sql.Timestamp
import java.util.Calendar
import java.util.Locale
import android.text.format.DateFormat

object Utils {

    const val AD_STATUS_AVALIABLE="UYGUN"
    const val AD_STATUS_SOLD="İLAN KAPANDI"

    val categories= arrayOf(
        "Tüm kategoriler",
        "Tampon",
        "Cam",
        "Kaporta",
        "Far",
        "Tekerlek",
        "Boya",
    )

    val categoryIcons= arrayOf(
        R.drawable.ic_category_all,
        R.drawable.ic_category_rear,
        R.drawable.ic_category_glass,
        R.drawable.ic_category_kaporta,
        R.drawable.ic_category_far,
        R.drawable.ic_category_wheel,
        R.drawable.ic_category_paint,
    )

    val conditions= arrayOf(
        "Sıfır",
        "ikinci El",
    )

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getTimestamp(): Long{
        return System.currentTimeMillis()
    }

    fun FormatTimestampDate(timestamp: Long):String{
        val calendar=Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis=timestamp

        return android.text.format.DateFormat.format("dd/mm/yyyy",calendar).toString()
    }

}