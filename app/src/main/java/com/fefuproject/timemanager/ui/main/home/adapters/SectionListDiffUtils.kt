package com.fefuproject.timemanager.ui.main.home.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

class SectionListDiffUtils : DiffUtil.ItemCallback<ListItem>() {

    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        oldItem == newItem

}