package com.fefuproject.timemanager.main.home.adapters

import androidx.recyclerview.widget.DiffUtil
import com.fefuproject.timemanager.models.NoteModel

class HomeListDiffUtils: DiffUtil.ItemCallback<NoteModel>() {

    override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean = oldItem == newItem

}