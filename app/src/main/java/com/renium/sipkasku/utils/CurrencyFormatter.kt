package com.renium.sipkasku.utils

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {

    val localeID = Locale("in", "ID")

    return NumberFormat
        .getCurrencyInstance(localeID)
        .format(amount)
}
