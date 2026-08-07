package com.mashinist_tep70bs_145.morgashki.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mashinist_tep70bs_145.morgashki.R
import com.mashinist_tep70bs_145.morgashki.model.Locomotive

/**
 * Элементы списка: либо заголовок группы ЖД, либо карточка локомотива.
 */
sealed class ListRow {
    data class Header(val titleRes: Int) : ListRow()
    data class Item(val locomotive: Locomotive) : ListRow()
}

class LocomotiveListAdapter(
    private val rows: List<ListRow>,
    private val onClick: (Locomotive) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (rows[position]) {
            is ListRow.Header -> TYPE_HEADER
            is ListRow.Item -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_group_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_locomotive, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is ListRow.Header -> (holder as HeaderViewHolder).bind(row)
            is ListRow.Item -> (holder as ItemViewHolder).bind(row.locomotive, onClick)
        }
    }

    override fun getItemCount(): Int = rows.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(header: ListRow.Header) {
            (itemView as TextView).setText(header.titleRes)
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvOwnName: TextView = itemView.findViewById(R.id.tvOwnName)
        private val tvOnRoute: TextView = itemView.findViewById(R.id.tvOnRoute)
        private val rowRoot: View = itemView.findViewById(R.id.rowRoot)

        fun bind(locomotive: Locomotive, onClick: (Locomotive) -> Unit) {
            tvName.text = locomotive.displayName

            if (locomotive.ownName != null) {
                tvOwnName.visibility = View.VISIBLE
                tvOwnName.text = "\u201C${locomotive.ownName}\u201D"
            } else {
                tvOwnName.visibility = View.GONE
            }

            if (locomotive.onRoute) {
                tvOnRoute.visibility = View.VISIBLE
                rowRoot.isEnabled = false
                rowRoot.alpha = 0.55f
                rowRoot.setOnClickListener(null)
                tvName.setTextColor(itemView.context.getColor(R.color.text_disabled))
            } else {
                tvOnRoute.visibility = View.GONE
                rowRoot.isEnabled = true
                rowRoot.alpha = 1f
                tvName.setTextColor(itemView.context.getColor(R.color.text_primary))
                rowRoot.setOnClickListener { onClick(locomotive) }
            }
        }
    }
}
