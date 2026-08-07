package com.mashinist_tep70bs_145.morgashki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mashinist_tep70bs_145.morgashki.model.LocomotiveCatalog
import com.mashinist_tep70bs_145.morgashki.model.RailwayGroup
import com.mashinist_tep70bs_145.morgashki.ui.ListRow
import com.mashinist_tep70bs_145.morgashki.ui.LocomotiveListAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val rows = buildRows()
        recyclerView.adapter = LocomotiveListAdapter(rows) { locomotive ->
            val intent = Intent(this, LocomotiveActivity::class.java)
            intent.putExtra(LocomotiveActivity.EXTRA_CODE, locomotive.code)
            startActivity(intent)
        }
    }

    /**
     * Список идёт в исходном порядке каталога, но с заголовками групп ЖД,
     * вставленными перед первым локомотивом каждой группы.
     */
    private fun buildRows(): List<ListRow> {
        val rows = mutableListOf<ListRow>()
        var currentGroup: RailwayGroup? = null

        for (loco in LocomotiveCatalog.all) {
            if (loco.group != currentGroup) {
                currentGroup = loco.group
                rows.add(ListRow.Header(currentGroup.titleRes))
            }
            rows.add(ListRow.Item(loco))
        }
        return rows
    }
}
