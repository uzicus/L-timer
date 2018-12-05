package com.tkachenkod.ltimer.extension

import android.content.Context
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes

fun View.showKeyboard() {

    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (hasWindowFocus()) {
        if (requestFocus()) {
            imm.showSoftInput(this@showKeyboard, 0)
        }
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(object :
            ViewTreeObserver.OnWindowFocusChangeListener {
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                post {
                    if (requestFocus()) {
                        imm.showSoftInput(this@showKeyboard, 0)
                    }
                }
                viewTreeObserver.removeOnWindowFocusChangeListener(this)
            }
        })
    }
}

fun View.hideKeyboard() {
    clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}