package com.fefuproject.timemanager.base.presenter

import com.fefuproject.timemanager.base.interactor.IInteractor
import com.fefuproject.timemanager.base.view.IView
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : IView, I : IInteractor>
internal constructor(
    protected var interactor: I?,
/*    protected val compositeDisposable: CompositeDisposable,
    protected val schedulerProvider: SchedulerProvider*/
) : IPresenter<V, I> {

    private var view: V? = null
    private val isViewAttached: Boolean get() = view != null

    override fun onAttach(view: V?) {
        this.view = view
    }

    override fun getView(): V? = view

    override fun onDetach() {
//        compositeDisposable.dispose()
        view = null
        interactor = null
    }

}