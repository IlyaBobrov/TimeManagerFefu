package com.fefuproject.timemanager.ui.main.home.adapters

import androidx.recyclerview.widget.DiffUtil
import com.fefuproject.timemanager.logic.firebase.models.Items


class ItemsDiffUtils: DiffUtil.ItemCallback<Items>() {

    override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean = oldItem == newItem

}