package com.tkachenkod.ltimer.extension

import android.content.Context
import android.content.res.Resources
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.regex.Pattern

@ColorInt
fun Resources.color(@ColorRes colorId: Int) = ResourcesCompat.getColor(this, colorId, null)

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

fun Snackbar.onDismissed(onDismissed: () -> Unit): Snackbar {
    return addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            onDismissed.invoke()
        }
    })
}

fun CharSequence.format(vararg args: Any) = format(Locale.getDefault(), *args)

/**
 * Provides {@link String#format} style functions that work with {@link Spanned} strings and preserve formatting.
 * @see <a href="https://github.com/george-steel/android-utils/blob/master/src/org/oshkimaadziig/george/androidutils/SpanFormatter.java">Github link</a>
 */
fun CharSequence.format(locale: Locale, vararg args: Any): SpannedString {
    val sequencePattern = Pattern.compile("%([0-9]+\\$|<?)([^a-zA-z%]*)([[a-zA-Z%]&&[^tT]]|[tT][a-zA-Z])")

    val out = SpannableStringBuilder(this)

    var i = 0
    var argAt = -1

    while (i < out.length) {
        val m = sequencePattern.matcher(out)
        if (!m.find(i)) break
        i = m.start()
        val exprEnd = m.end()

        val argTerm = m.group(1)
        val modTerm = m.group(2)
        val typeTerm = m.group(3)

        val cookedArg: CharSequence

        when (typeTerm) {
            "%" -> cookedArg = "%"
            "n" -> cookedArg = "\n"
            else -> {
                var argIdx = 0
                if (argTerm == "") ++argAt
                else if (argTerm == "<")
                else argIdx = Integer.parseInt(argTerm.substring(0, argTerm.length - 1)) - 1

                val argItem = args[argIdx]

                cookedArg = if (typeTerm == "s" && argItem is Spanned) {
                    argItem
                } else {
                    String.format(locale, "%$modTerm$typeTerm", argItem)
                }
            }
        }

        out.replace(i, exprEnd, cookedArg)
        i += cookedArg.length
    }

    return SpannedString(out)
}

fun String.spannable() = SpannableString(this)

fun SpannableString.applyColor(color: Int) = apply {
    setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}