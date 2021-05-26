package com.fefuproject.timemanager.ui.main.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.components.getParsedDate
import com.fefuproject.timemanager.components.selectHeaderCardDate
import com.fefuproject.timemanager.logic.models.NoteModel


class HomeAdapter(
    val homeOnItemClickListener: HomeOnItemClickListener
) : ListAdapter<NoteModel, RecyclerView.ViewHolder>(HomeListDiffUtils()) {

    companion object {
        const val TYPE_DATE = 0
        const val TYPE_STANDART = 1
    }

    private var currentPosition: Int = 0

    override fun getItemViewType(position: Int): Int {
        currentPosition = position
        /*return if (position > 0 && getParsedDate(getItem(position).dateStart!!, "yyyy-MM-dd") !=
            getParsedDate(getItem(position - 1).date!!, "yyyy-MM-dd")
        )
            TYPE_DATE
        else
            if (position == 0)
                TYPE_DATE
            else*/
        return TYPE_STANDART
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_DATE -> itemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_home_date, parent, false),
                true
            )
            else -> itemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_standart, parent, false), false
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (getItemViewType(position)) {
            TYPE_DATE -> (holder as itemViewHolder).bindItemHolder(getItem(position))
            else -> (holder as itemViewHolder).bindItemHolder(getItem(position))
        }

    inner class itemViewHolder(itemView: View, val dateType: Boolean = false) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title = itemView.findViewById<TextView>(R.id.itemTitle)
        val descrioption = itemView.findViewById<TextView>(R.id.itemDescrioption)
        val category = itemView.findViewById<TextView>(R.id.itemCategory)
        val dateStart = itemView.findViewById<TextView>(R.id.itemDateStart)
        val dateEnd = itemView.findViewById<TextView>(R.id.itemDateEnd)
        val completeContainer = itemView.findViewById<ConstraintLayout>(R.id.clItemContainer)
        val completeCheckBox = itemView.findViewById<CheckBox>(R.id.itemCheckBox)

        fun bindItemHolder(currentItem: NoteModel) {
            /*if (dateType) {
                val dateTitle = itemView.findViewById<TextView>(R.id.itemTitleDate)
                selectHeaderCardDate(
                    getParsedDate(currentItem.date!!, "yyyy-MM-dd"),
                    dateTitle
                )
            }*/
            itemView.setOnClickListener(this)
            this.category.text = currentItem.category
            selectHeaderCardDate(
                getParsedDate(currentItem.dateStart!!, "yyyy-MM-dd"),
                this.dateStart
            )
            if (currentItem.dateEnd != null) {
                dateEnd.isVisible
                selectHeaderCardDate(
                    getParsedDate(currentItem.dateEnd, "yyyy-MM-dd"),
                    this.dateEnd
                )
            } else {
                dateEnd.isGone
            }

            if (currentItem.title == null || currentItem.title == "") {
                title.isGone
            } else {
                title.isVisible
                title.text = currentItem.title
            }

            descrioption.text = currentItem.description

            if (currentItem.complete != null && currentItem.complete) {
                completeContainer.setBackgroundResource(R.drawable.bg_complite_item)
                completeCheckBox.isChecked = true
            } else {
                completeCheckBox.isChecked = false
            }
        }

        override fun onClick(v: View?) {
            homeOnItemClickListener.onItemClick(getItem(absoluteAdapterPosition))
        }
    }


    interface HomeOnItemClickListener {
        fun onItemClick(item: NoteModel)
    }

}