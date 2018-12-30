package com.zakobura.together.togemp.util

import android.content.Context
import android.util.Log
import android.widget.Toast

fun logD(string: String) {
    val ste = Thread.currentThread().stackTrace[3]
    println("caller class:" + ste.className + " method:" + ste.methodName + " line:" + ste.lineNumber)
    Log.d("logTogemp : ${Throwable().stackTrace[3].className} ${ste.methodName}", string)
}

fun logE(string: String) {
    val ste = Thread.currentThread().stackTrace[3]
    println("caller class:" + ste.className + " method:" + ste.methodName + " line:" + ste.lineNumber)
    Log.e("logTogemp : ${Throwable().stackTrace[3].className} ${ste.methodName}", string)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}