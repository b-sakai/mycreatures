package com.websarva.wings.android.mycreatures

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.suspendCoroutine

class RecentItemView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recent_item_view)

        val recentMenu = findViewById<RecyclerView>(R.id.recentMenu)
        val layout = LinearLayoutManager(this@RecentItemView)

        lifecycleScope.launch {
            recentMenu.layoutManager = layout
            val recentList = createRecentList()
            val adapter = RecyclerListAdapter(recentList)
            recentMenu.adapter = adapter
            // 区切り線
            val decorator = DividerItemDecoration(this@RecentItemView, layout.orientation)
            recentMenu.addItemDecoration(decorator)
        }
    }

    private suspend fun createRecentList(): MutableList<MutableMap<String, Any>> {
        val menuList: MutableList<MutableMap<String, Any>> = mutableListOf()
        val list = getCurrentItemFromDatabase()
        for (item in list) {
            val date = Date(item.createdAt)
            val formatter = SimpleDateFormat("yyyy.MM.dd 'at' HH::mm:ss")
            val createdTime = formatter.format(date)
            var menu = mutableMapOf<String, Any>(
                "speciesName" to item.name,
                "parentName" to item.parentName,
                "createDate" to createdTime
            )
            menuList.add(menu)
        }
        Log.i("MyplantpediA", menuList.toString())
        return menuList
    }

    private suspend fun getCurrentItemFromDatabase(): List<SpeciesEntity> {
        val db = SpeciesRoomDatabase.getDatabase(this@RecentItemView)
        val speciesDao = db.speciesDao()
        val nowTime = System.currentTimeMillis()
        //val thresholdTime = nowTime - 1000 * 60 * 60 * 24 * 30 // 現在からちょうど30日前のms時刻
        val thresholdTime = 0L
        return speciesDao.getRecentItems(thresholdTime)
    }

    private inner class RecyclerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var _speciesNameRow: TextView
        var _parentNameRow: TextView
        var _createDateRow: TextView
        init {
            _speciesNameRow = itemView.findViewById(R.id.speciesName)
            _parentNameRow = itemView.findViewById(R.id.parentName)
            _createDateRow = itemView.findViewById(R.id.createTime)
        }
    }

    private inner class RecyclerListAdapter(private val _listData: MutableList<MutableMap<String, Any>>): RecyclerView.Adapter<RecyclerListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerListViewHolder {
            val inflater = LayoutInflater.from(this@RecentItemView)
            val view = inflater.inflate(R.layout.row, parent, false)
            view.setOnClickListener(ItemClickListener())
            val holder = RecyclerListViewHolder(view)
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerListViewHolder, position: Int) {
            val item = _listData[position]
            val speciesName = item["speciesName"] as String
            val parentName = item["parentName"] as String
            val createDate = item["createDate"] as String
            holder._speciesNameRow.text = speciesName
            holder._parentNameRow.text = parentName
            holder._createDateRow.text = createDate
        }

        override fun getItemCount(): Int {
            return _listData.size
        }
    }

    private inner class ItemClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val tvSpeciesName = view.findViewById<TextView>(R.id.speciesName)
            val tvParentName = view.findViewById<TextView>(R.id.parentName)
            val speciesName = tvSpeciesName.text.toString()
            val parentName = tvParentName.text.toString()
            val msg = speciesName
            Toast.makeText(this@RecentItemView, msg, Toast.LENGTH_SHORT).show()

            val intent2PhylogeneticTree = Intent(this@RecentItemView, PhylogeneticTree::class.java).apply {
                putExtra("currentName", speciesName)
                putExtra("parentTree", arrayListOf<String>(parentName))
            }
            startActivity(intent2PhylogeneticTree)
        }

    }


}