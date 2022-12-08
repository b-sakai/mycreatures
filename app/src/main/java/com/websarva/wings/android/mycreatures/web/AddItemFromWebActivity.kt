package com.websarva.wings.android.mycreatures.web

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.mycreatures.R
import com.websarva.wings.android.mycreatures.database.SpeciesEntity
import com.websarva.wings.android.mycreatures.database.SpeciesRoomDatabase
import kotlinx.coroutines.launch


class AddItemFromWebActivity : AppCompatActivity() {
    private var newItemName : String = ""
    private var bitmap: Bitmap? = null
    private var apgKey = arrayListOf<String>()
    private var apgValue = arrayListOf<String>()
    private var apgExplanation: String = ""

    private var parentName: String = ""
    private var parentId: Int = 0
    private var newItem: SpeciesEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_from_web)
        newItemName = intent?.getStringExtra("title") as String
        val bundle = intent.extras
        bitmap = bundle?.get("image") as Bitmap?
        apgKey = intent?.getStringArrayListExtra("key") as ArrayList<String>
        apgValue = intent?.getStringArrayListExtra("value") as ArrayList<String>
        apgExplanation = intent.getStringExtra("explanation") as String

        // スクレイピング結果をセットする
        // 追加する画像をセットする
        val imageView = findViewById<ImageView>(R.id.addImage)
        imageView?.setImageBitmap(bitmap)
        // 追加するアイテムの属性リストをセットする
        setItemList()
        // 追加する説明文をセットする
        val explanationText = findViewById<TextView>(R.id.addExplanation)
        explanationText.text = apgExplanation
    }

    // アイテムリストをセットする
    private fun setItemList() {
        var stringList = mutableListOf<String>()
        for (i in 0 until apgKey.size) {
            stringList.add(apgKey[i] + " : " + apgValue[i])
        }
        Log.i("AddItemFromWebActivity ", stringList.toString())
        val recyclerView = findViewById<RecyclerView>(R.id.addRecyclerMenu)
        recyclerView.adapter = RecyclerAdapter(stringList)
        val layout = LinearLayoutManager(this@AddItemFromWebActivity)
        recyclerView.layoutManager = layout
        // 区切り線
        val decorator = DividerItemDecoration(this@AddItemFromWebActivity, layout.orientation)
        recyclerView.addItemDecoration(decorator)
    }

    private inner class RecyclerAdapter(val list: List<String>) : RecyclerView.Adapter<ViewHolderList>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderList {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_children, parent, false)
            return ViewHolderList(itemView)
        }
        override fun onBindViewHolder(holder: ViewHolderList, position: Int) {
            holder.speciesName.text = list[position]
        }
        override fun getItemCount(): Int = list.size
    }
    private inner class ViewHolderList (item: View) : RecyclerView.ViewHolder(item) {
        val speciesName: TextView = item.findViewById(R.id.rowName)
        init {
        }
    }

    public fun addThisItem(view: View) {
        Log.i("MyPlantpediA", "add button clicked")
        return
        createNewItem()
        lifecycleScope.launch {
            val result = insertNewItem()
            if (result) {
                saveSpeiciesDatabase()
            }
        }
    }

    fun createNewItem() {
        newItem = SpeciesEntity(0, parentId, newItemName,"")
    }

    suspend fun insertNewItem(): Boolean {
        val db = SpeciesRoomDatabase.getDatabase(this@AddItemFromWebActivity)
        val speciesDao = db.speciesDao()
        if (speciesDao.isRowIsExist(newItemName)) {
            Toast.makeText(this@AddItemFromWebActivity, "この種類はすでに登録されています。違う名前を指定するか登録済みのアイテムを削除してください。", Toast.LENGTH_LONG).show()
            return false
        } else if (newItem == null) {
            Toast.makeText(this@AddItemFromWebActivity, "error newItem == null", Toast.LENGTH_SHORT).show()
            return false
        } else {
            speciesDao.insertWithTimestamp(newItem!!)
            return true
        }
    }

    // region saveData
    suspend fun saveSpeiciesDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@AddItemFromWebActivity)
        val speciesDao = db.speciesDao()
        newItem?.let { speciesDao.updateWithTimestamp(it) }
    }
}