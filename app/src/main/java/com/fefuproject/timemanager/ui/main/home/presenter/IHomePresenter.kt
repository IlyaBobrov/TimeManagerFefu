package com.fefuproject.timemanager.ui.main.home.presenter

import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.ui.main.home.interactor.IHomeInteractor
import com.fefuproject.timemanager.ui.main.home.view.IHomeView

interface IHomePresenter<V: IHomeView, I: IHomeInteractor>{

    fun getNotes()

    fun insertNote()
}