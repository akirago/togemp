package com.example.hashimotoakira.togemp.util

import android.util.Log

fun logD(string: String) {
    val ste = Thread.currentThread().stackTrace[3]
    println("caller class:" + ste.className + " metho:" + ste.methodName + " line:" + ste.lineNumber)
    Log.d("getTogemp : ${ste.methodName}", string)
}

fun logE(string: String) {
    val ste = Thread.currentThread().stackTrace[3]
    println("caller class:" + ste.className + " metho:" + ste.methodName + " line:" + ste.lineNumber)
    Log.e("getTogemp : ${ste.methodName}", string)
}