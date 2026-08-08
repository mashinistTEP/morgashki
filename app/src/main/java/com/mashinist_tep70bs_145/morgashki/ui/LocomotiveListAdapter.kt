package com.mashinist_tep70bs_145.morgashki.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mashinist_tep70bs_145.morgashki.R
import com.mashinist_tep70bs_145.morgashki.model.Locomotive
import com.mashinist_tep70bs_145.morgashki.model.RailwayGroup
import com.mashinist_tep70bs_145.morgashki.model.RailwayLine

/**
 * Элементы списка: заголовок группы ЖД, ряд кнопок-дорог (подгрупп) внутри
 * этой группы, либо карточка локомотива.
 */
sealed class ListRow {
    data class Header(val titleRes: Int) : ListRow()
    data class LineChipsRow(val group: RailwayGroup, val lines: List<RailwayLine>, val selectedLine: RailwayLine?) : ListRow()
    data class Item(val locomotive: Locomotive) : ListRow()
}

class LocomotiveListAdapter(
    private val rows: List<ListRow>,
    private val onClick: (Locomotive) -> Unit,
    private val onLineSelected: (RailwayGroup, RailwayLine?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_LINE_CHIPS = 2
    }

    override fun getItemViewType(position: Int): Int =
        when (rows[position]) {
            is ListRow.Header -> TYPE_HEADER
            is ListRow.Item -> TYPE_ITEM
            is ListRow.LineChipsRow -> TYPE_LINE_CHIPS
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_group_header, parent, false))
            TYPE_LINE_CHIPS -> LineChipsViewHolder(inflater.inflate(R.layout.item_line_chips_row, parent, false))
            else -> ItemViewHolder(inflater.inflate(R.layout.item_locomotive, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is ListRow.Header -> (holder as HeaderViewHolder).bind(row)
            is ListRow.Item -> (holder as ItemViewHolder).bind(row.locomotive, onClick)
            is ListRow.LineChipsRow -> (holder as LineChipsViewHolder).bind(row, onLineSelected)
        }
    }

    override fun getItemCount(): Int = rows.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(header: ListRow.Header) {
            (itemView as TextView).setText(header.titleRes)
        }
    }

    class LineChipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: LinearLayout = itemView.findViewById(R.id.lineChipsContainer)

        fun bind(row: ListRow.LineChipsRow, onLineSelected: (RailwayGroup, RailwayLine?) -> Unit) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(itemView.context)
            val density = itemView.resources.displayMetrics.density

            for (line in row.lines) {
                val chip = inflater.inflate(R.layout.item_line_chip, container, false) as CheckedTextView
                chip.setText(line.titleRes)
                val isSelected = line == row.selectedLine
                chip.isChecked = isSelected

                val lp = chip.layoutParams as LinearLayout.LayoutParams
                lp.marginEnd = (6 * density).toInt()
                chip.layoutParams = lp

                chip.setOnClickListener {
                    // повторный клик по уже выбранной дороге снимает фильтр
                    onLineSelected(row.group, if (isSelected) null else line)
                }
                container.addView(chip)
            }
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
