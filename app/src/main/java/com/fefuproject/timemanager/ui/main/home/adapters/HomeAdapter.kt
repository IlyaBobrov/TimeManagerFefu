package com.fefuproject.timemanager.ui.main.home.adapters

import android.graphics.Color
import android.graphics.Color.parseColor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.components.getColorCompat
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
        return if (position > 0 && getParsedDate(getItem(position).date!!, "yyyy-MM-dd") !=
            getParsedDate(getItem(position - 1).date!!, "yyyy-MM-dd")
        )
            TYPE_DATE
        else
            if (position == 0)
                TYPE_DATE
            else
                TYPE_STANDART
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

        val category = itemView.findViewById<TextView>(R.id.itemCategory)
        val date = itemView.findViewById<TextView>(R.id.itemDate)
        val message = itemView.findViewById<TextView>(R.id.itemMessage)
        val complete = itemView.findViewById<ConstraintLayout>(R.id.clItemContainer)

        fun bindItemHolder(currentItem: NoteModel) {
            if (dateType) {
                val dateTitle = itemView.findViewById<TextView>(R.id.itemTitleDate)
                selectHeaderCardDate(
                    getParsedDate(currentItem.date!!, "yyyy-MM-dd"),
                    dateTitle
                )
            }
            itemView.setOnClickListener(this)
            this.category.text = currentItem.category
            selectHeaderCardDate(
                getParsedDate(currentItem.date!!, "yyyy-MM-dd"),
                this.date
            )
            this.message.text = currentItem.message
            if (currentItem.complete != null && currentItem.complete) {
                complete.setBackgroundResource(R.drawable.ic_launcher_background)
            }
        }

        override fun onClick(v: View?) {
            homeOnItemClickListener.onItemClick(absoluteAdapterPosition)
        }
    }


    interface HomeOnItemClickListener {
        fun onItemClick(position: Int)
    }

}