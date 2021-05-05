package com.fefuproject.timemanager.ui.base.presenter

import com.fefuproject.timemanager.ui.base.view.IView

abstract class BasePresenter<V : IView>
internal constructor() {

    private var view: V? = null

    fun getView(): V? = view

    fun onAttach(view: V?) {
        this.view = view
    }

    fun onDetach() {
        view = null
    }
}