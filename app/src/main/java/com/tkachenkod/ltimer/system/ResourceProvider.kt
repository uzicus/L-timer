package com.tkachenkod.ltimer.system

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.tkachenkod.ltimer.extension.color

class ResourceProvider(private val context: Context) {

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.resources.getString(resId, *formatArgs)
    }

    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return context.resources.getQuantityString(resId, quantity, *formatArgs)
    }

    fun getStringArray(@ArrayRes resId: Int): Array<String> {
        return context.resources.getStringArray(resId)
    }

    fun getIntArray(@ArrayRes resId: Int): IntArray {
        return context.resources.getIntArray(resId)
    }

    fun getBooleanArray(@ArrayRes resId: Int): BooleanArray {
        val stringArray = getStringArray(resId)
        val boolArray = BooleanArray(stringArray.size)
        for (i in stringArray.indices) {
            boolArray[i] = java.lang.Boolean.parseBoolean(stringArray[i])
        }
        return boolArray
    }

    fun getColor(@ColorRes resId: Int): Int {
        return context.resources.color(resId)
    }
}
