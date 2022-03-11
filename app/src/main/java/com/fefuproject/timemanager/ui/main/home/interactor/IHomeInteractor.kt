package com.fefuproject.timemanager.ui.main.home.interactor

import com.fefuproject.timemanager.base.interactor.IInteractor

interface IHomeInteractor:IInteractor {
    suspend fun doGetDb()

}