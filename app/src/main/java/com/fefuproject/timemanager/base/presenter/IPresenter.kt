package com.fefuproject.timemanager.base.presenter

import com.fefuproject.timemanager.base.interactor.IInteractor
import com.fefuproject.timemanager.base.view.IView

interface IPresenter<V : IView, I : IInteractor>  {

    fun onAttach(view: V?)

    fun onDetach()

    fun getView(): V?

}