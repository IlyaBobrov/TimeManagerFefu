package com.fefuproject.timemanager.ui.main.home.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.components.getColorCompat
import com.fefuproject.timemanager.components.getParsedDate
import com.fefuproject.timemanager.components.selectHeaderCardDate
import com.fefuproject.timemanager.ui.main.home.adapters.ListItem.Companion.TYPE_HEADER
import com.fefuproject.timemanager.ui.main.home.adapters.ListItem.Companion.TYPE_ITEM
import com.fefuproject.timemanager.ui.main.home.adapters.ListItem.Companion.TYPE_NOTE
import kotlinx.parcelize.Parcelize

class SectionHomeAdapter(
//    private val items: ArrayList<ListItem>,
    val homeSectionOnItemClickListener: HomeSectionOnItemClickListener
) : ListAdapter<ListItem, ViewHolder>(SectionListDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_HEADER ->
                VHHeader(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
                )
            TYPE_NOTE -> VHNote(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_standart, parent, false)
            )
            else -> {
                VHItem(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is VHHeader) {
            (holder as VHHeader).bindVHHeader(getItem(position) as Header)

        } else if (holder is VHItem) {
            (holder as VHItem).bindVHItem(getItem(position) as Item)
        } else if (holder is VHNote) {
            (holder as VHNote).bindVHNote(getItem(position) as Note)
        }
    }


    override fun getItemViewType(position: Int): Int = getItem(position).itemType




    inner class VHHeader(itemView: View) : ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById<View>(R.id.tv_date) as TextView

        fun bindVHHeader(header: Header) {
            tvName.text = header.name
        }
    }

    inner class VHNote(itemView: View) : ViewHolder(itemView), View.OnClickListener {

        val category = itemView.findViewById<TextView>(R.id.itemCategory)
        val date = itemView.findViewById<TextView>(R.id.itemDate)
        val message = itemView.findViewById<TextView>(R.id.itemMessage)
        val complete = itemView.findViewById<LinearLayout>(R.id.itemContainer)


        fun bindVHNote(currentItem: Note) {
            itemView.setOnClickListener(this)
            this.category.text = currentItem.category
            selectHeaderCardDate(
                getParsedDate(currentItem.date, "yyyy-MM-dd"),
                this.date
            )
            this.message.text = currentItem.message
            if (currentItem.complete) {
                complete.setBackgroundColor(itemView.context.getColorCompat(R.color.green_100))
            }
        }

        override fun onClick(v: View?) {
            homeSectionOnItemClickListener.onSectionItemClick(absoluteAdapterPosition, getItem(absoluteAdapterPosition) as Note)
        }
    }

    inner class VHItem(itemView: View) : ViewHolder(itemView) {
        var tvItem: TextView = itemView.findViewById<View>(R.id.tv_item_text) as TextView

        fun bindVHItem(item: Item) {
            tvItem.text = item.name
        }
    }

    interface HomeSectionOnItemClickListener {
        fun onSectionItemClick(pos: Int, item: Note)
    }

}


interface ListItem {
    val itemType: Int

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER = 1
        const val TYPE_NOTE = 2
    }
}


class Header(
    var name: String,
    override val itemType: Int = TYPE_HEADER
) : ListItem


class Item(
    var name: String,
    override val itemType: Int = TYPE_ITEM
) : ListItem

@Parcelize
data class Note(
    internal val id: Int,
    internal var category: String,
    internal var date: String,
    internal var message: String,
    internal var complete: Boolean,
    override val itemType: Int = TYPE_NOTE
) : Parcelable, ListItem
