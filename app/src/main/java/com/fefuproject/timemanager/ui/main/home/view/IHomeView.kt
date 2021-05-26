package com.fefuproject.timemanager.ui.main.home.view

import com.fefuproject.timemanager.base.view.IView
import com.fefuproject.timemanager.logic.models.NoteListModel
import com.fefuproject.timemanager.logic.models.NoteModel

interface IHomeView:IView {
    fun setListData(data: List<NoteModel>)

    fun getDb(notes: List<NoteModel>)
}