package com.mashinist_tep70bs_145.morgashki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mashinist_tep70bs_145.morgashki.model.Locomotive
import com.mashinist_tep70bs_145.morgashki.model.LocomotiveCatalog
import com.mashinist_tep70bs_145.morgashki.model.RailwayGroup
import com.mashinist_tep70bs_145.morgashki.model.RailwayLine
import com.mashinist_tep70bs_145.morgashki.ui.ListRow
import com.mashinist_tep70bs_145.morgashki.ui.LocomotiveListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    // выбранная дорога (подгруппа) для каждой компании отдельно;
    // null у конкретной группы = показываются только локомотивы без line (общий состав)
    private val selectedLineByGroup = mutableMapOf<RailwayGroup, RailwayLine?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshList()
    }

    private fun refreshList() {
        val rows = buildRows()
        recyclerView.adapter = LocomotiveListAdapter(
            rows = rows,
            onClick = { locomotive ->
                val intent = Intent(this, LocomotiveActivity::class.java)
                intent.putExtra(LocomotiveActivity.EXTRA_CODE, locomotive.code)
                startActivity(intent)
            },
            onLineSelected = { group, line ->
                selectedLineByGroup[group] = line
                refreshList()
            }
        )
    }

    /**
     * Список идёт в исходном порядке каталога, с заголовками групп ЖД.
     * Если у группы есть локомотивы с конкретной дорогой (line != null),
     * сразу под заголовком показывается ряд маленьких кнопок-дорог.
     *
     * Пока в группе не выбрана дорога — показываются только локомотивы
     * без line (общий состав группы). При выборе дороги — только
     * локомотивы этой дороги (общий состав скрывается).
     */
    private fun buildRows(): List<ListRow> {
        val rows = mutableListOf<ListRow>()
        val groupsInOrder = LocomotiveCatalog.all.map { it.group }.distinct()

        for (group in groupsInOrder) {
            rows.add(ListRow.Header(group.titleRes))

            val locomotivesInGroup = LocomotiveCatalog.all.filter { it.group == group }
            val linesInGroup = locomotivesInGroup.mapNotNull { it.line }.distinct()

            val selectedLine = selectedLineByGroup[group]

            if (linesInGroup.isNotEmpty()) {
                rows.add(ListRow.LineChipsRow(group, linesInGroup, selectedLine))
            }

            val visibleLocomotives = if (selectedLine != null) {
                locomotivesInGroup.filter { it.line == selectedLine }
            } else {
                locomotivesInGroup.filter { it.line == null }
            }

            for (loco in visibleLocomotives) {
                rows.add(ListRow.Item(loco))
            }
        }
        return rows
    }
}
