package com.stm.bledemo.extension

// Converts Byte Array to Hexadecimal String
fun ByteArray.toHexString(): String =
    joinToString (separator = " ") { String.format("%02x", it) }


fun ByteArray.toDecString(): String =
    joinToString (separator = " ") { String.format("%d", it) }


fun decToHexString(data:Int,zeroNum:Int): String {
    var formatShow= "%0"+zeroNum+"x"
    return String.format(formatShow,data)
}