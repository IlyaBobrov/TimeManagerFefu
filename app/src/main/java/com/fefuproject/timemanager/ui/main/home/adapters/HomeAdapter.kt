package com.fefuproject.timemanager.ui.main.home.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.logic.firebase.models.Items


class HomeAdapter(
    val homeOnItemClickListener: HomeOnItemClickListener,
) : ListAdapter<Items/*NoteModel*/, RecyclerView.ViewHolder>(ItemsDiffUtils()/*HomeListDiffUtils()*/)/*,
    ItemTouchHelperAdapter*/ {

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

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            TYPE_DATE -> itemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_home_date, parent, false),
                true
            )
            else -> itemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_standart, parent, false), false
            )
        }
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

        fun bindItemHolder(currentItem: Items) {
            Log.d("TAG", "bindItemHolder: $currentItem")
            itemView.setOnClickListener(this)
            completeCheckBox.setOnClickListener(this)

            if (currentItem.category == "-") {
                this.category.visibility = View.GONE
            } else {
                this.category.visibility = View.VISIBLE
                this.category.text = currentItem.category
            }
            dateStart.text = currentItem.dateToDo
            if (currentItem.deadline != null) {
                dateEnd.isVisible
                this.dateEnd.text = currentItem.deadline
            } else {
                dateEnd.isGone
            }

            if (currentItem.title == null || currentItem.title.toString() == "") {
                this.title.visibility = View.GONE
            } else {
                this.title.visibility = View.VISIBLE
                title.text = currentItem.title
            }

            descrioption.text = currentItem.text

            if (currentItem.isComplited!!) {
                completeCheckBox.isChecked = true
                completeContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_300
                    )
                )

            } else {
                completeContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                completeCheckBox.isChecked = false
            }
        }

        /*fun bindItemHolder(currentItem: NoteModel) {
            Log.d("TAG", "bindItemHolder: $currentItem")
            itemView.setOnClickListener(this)
            completeCheckBox.setOnClickListener(this)

            if (currentItem.category?.title == "Все") {
                this.category.visibility = View.GONE
            } else {
                this.category.visibility = View.VISIBLE
                this.category.text = currentItem.category?.title
            }
            dateStart.text = currentItem.dateStart
            if (currentItem.dateEnd != null) {
                dateEnd.isVisible
                this.dateEnd.text = currentItem.dateEnd
            } else {
                dateEnd.isGone
            }

            if (currentItem.title == null || currentItem.title.toString() == "") {
                this.title.visibility = View.GONE
            } else {
                this.title.visibility = View.VISIBLE
                title.text = currentItem.title
            }

            descrioption.text = currentItem.description

            if (currentItem.complete!!) {
                completeCheckBox.isChecked = true
                completeContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_300
                    )
                )

            } else {
                completeContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                completeCheckBox.isChecked = false
            }
        }*/

        override fun onClick(v: View) {
            homeOnItemClickListener.onItemClick(
                v,
                getItem(absoluteAdapterPosition),
                absoluteAdapterPosition
            )
        }
    }

    interface HomeOnItemClickListener {
        fun onItemClick(v: View, item: Items, position: Int)
    }

    /*interface HomeOnItemClickListener {
        fun onItemClick(v: View, item: NoteModel, position: Int)
    }*/


    /*override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mItems, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mItems, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }*/

}

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position: Int)

}