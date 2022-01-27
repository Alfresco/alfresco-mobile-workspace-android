package com.alfresco.content.data

import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * converting Byte to KB up tp 2 decimal point.
 */
fun String.byteToKB(): String {
    val df = DecimalFormat("#.00")
    df.roundingMode = RoundingMode.FLOOR
    return df.format(this.toDouble().div(1000)).toString()
}

/**
 * converting KB to byte up to 0 decimal point.
 */
fun String.kBToByte(): String {
    return "%.0f".format(this.toDouble().times(1000)).toString()
}
