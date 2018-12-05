@file:Suppress("NOTHING_TO_INLINE")

package com.tkachenkod.ltimer.ui.base

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.base.PmController
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import me.dmdev.rxpm.widget.InputControl

abstract class BaseScreen<PM : BaseScreenPm>(bundle: Bundle? = null) :
        PmController<PM>(bundle),
        LayoutContainer {

    // Holds the view to allow the usage of android extensions right after the view is inflated.
    private var internalContainerView: View? = null

    abstract val screenLayout: Int

    abstract val pm: PM

    open fun onInitView(view: View, savedViewState: Bundle?) {}

    open fun onBindPresentationModel(view: View, pm: PM) {}

    override val containerView: View?
        get() = internalContainerView

    override fun providePresentationModel() = pm

    override fun createView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(screenLayout, container, false).also {
            internalContainerView = it
            onInitView(it, savedViewState)
        }
    }

    override fun onBindPresentationModel(pm: PM) {
        onBindPresentationModel(view!!, pm)
    }

    override fun handleBack(): Boolean {
        passTo(presentationModel.backActionConsumer)
        return true
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)

        internalContainerView = null
        clearFindViewByIdCache()
    }

    infix fun InputControl.bindTo(textInputLayout: TextInputLayout) {
        bind(textInputLayout, compositeUnbind)
    }

    internal inline fun InputControl.bind(
        textInputLayout: TextInputLayout, compositeDisposable: CompositeDisposable
    ) {

        val edit = textInputLayout.editText!!

        bind(edit, compositeDisposable)
        compositeDisposable.add(
            error.observable.subscribe { error ->
                textInputLayout.error = if (error.isEmpty()) null else error
            }
        )
    }

    internal inline fun InputControl.bind(
        editText: EditText,
        compositeDisposable: CompositeDisposable
    ) {

        var editing = false

        compositeDisposable.addAll(

            text.observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val editable = editText.text
                    if (!it!!.contentEquals(editable)) {
                        editing = true
                        if (editable is Spanned) {
                            val ss = SpannableString(it)
                            TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                            editable.replace(0, editable.length, ss)
                        } else {
                            editable.replace(0, editable.length, it)
                        }
                        editing = false
                    }
                },

            editText.textChanges()
                .skipInitialValue()
                .filter { !editing }
                .map { it.toString() }
                .subscribe(textChanges.consumer)
        )
    }
}