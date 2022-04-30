package com.websarva.wings.android.mycreatures

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.mycreatures.R.layout.options_menu
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class PhylogeneticTree : AppCompatActivity() {
    private var currentItem: SpeciesEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phylogenetic_tree)

        val name = intent.getStringExtra("currentName").toString()
        // データベースからカレントアイテムを取得する
        lifecycleScope.launch {
            currentItem = getCurrentItemFromDatabae(name)
            showUIs()
            currentItem?.toString()?.let { Log.i("MyplantpediA current item is ", it) }
        }
    }

    suspend fun getCurrentItemFromDatabae(name: String): SpeciesEntity? {
        val db = SpeciesRoomDatabase.getDatabase(this@PhylogeneticTree)
        val speciesDao = db.speciesDao()
        return speciesDao.get(name)
    }

    suspend fun showUIs() {
        // 親アイテムリンクを表示する
        showParentItemLink()

        // カレントアイテムを表示する
        showCurrentItem()

        // 説明を表示する
        showExplanation()

        // 子アイテムリストを表示する
        showChildrenItemList()

        // 画像を表示する
        showImages()
    }


    // region parentLink
    // 親アイテムリンクを表示する
    fun showParentItemLink() {
        // リスなクラスのインスタンスを生成
        val listener = ParentLinkListener()
        val layout = findViewById<LinearLayout>(R.id.parentBar)
        var parentTreeText = intent.getStringArrayListExtra("parentTree")
        if (parentTreeText.isNullOrEmpty()) {
            parentTreeText = currentItem?.parentName as ArrayList<String>
        }
        //Log.i("MyPlantpediA", ptext)
        if (parentTreeText != null) {
            for (pitem in parentTreeText) {
                //Log.i("MyPlantpediA", ptext)
                if (pitem == "") {
                    continue
                }
                // 親アイテムボタンの追加
                val parentLink = Button(this@PhylogeneticTree)
                parentLink.text = pitem
                parentLink.textSize = 10F
                parentLink.tag = pitem
                parentLink.setOnClickListener(listener)
                val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
                val lparams = LinearLayout.LayoutParams(wrapContent, wrapContent)
                lparams.weight = 1F
                layout.addView(parentLink, lparams)
                // 矢印の追加
                val arrow = TextView(this@PhylogeneticTree)
                arrow.text = ">"
                val aparams = LinearLayout.LayoutParams(wrapContent, wrapContent)
                aparams.weight = 0.5F
                layout.addView(arrow, aparams)
            }
        }
    }

    private inner class ParentLinkListener : View.OnClickListener {
        override fun onClick(view: View) {
            Log.i("MyPlantpediA", view.id.toString())
            Log.i("MyPlantpediA", view.tag.toString())

            val intent2PhylogeneticTree =
                     Intent(this@PhylogeneticTree, PhylogeneticTree::class.java).apply {
                            putExtra("currentName", view.tag.toString())
             }
            startActivity(intent2PhylogeneticTree)
        }
    }
    // endregion

    // region currentItem
    // カレントアイテムを表示する
    fun showCurrentItem() {
        val currentItemControl = findViewById<TextView>(R.id.currentItem)
        currentItemControl.text = currentItem?.name
    }
    // endregion

    // region imageView
    fun showImages() {
        val imageLayout = findViewById<LinearLayout>(R.id.photoBlock)
        currentItem?.imageUris?.forEach { uri ->
            val imageView = ImageView(this@PhylogeneticTree)
            imageView.setImageURI(Uri.parse(uri)) // handle chosen image
            imageView.adjustViewBounds = true
            // 画像ブロック領域にImageViewを追加
            val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
            val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
            val lparams = LinearLayout.LayoutParams(wrapContent, wrapContent)
            imageLayout.addView(imageView, lparams)
        }
    }

    // ImageButtonがクリックされたときに呼ばれる
    fun addImage(view: View) {
        Log.i("MyPlantpediA", "addImage Button is Clicked")
        openGalleryForImage()
    }
    private val REQUEST_GALLERY_TAKE = 2

    //ギャラリーを開くためのメソッド
    private fun openGalleryForImage() {
        //ギャラリーに画面を遷移するためのIntent
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_TAKE)
    }

    // onActivityResultにイメージ設定
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageLayout = findViewById<LinearLayout>(R.id.photoBlock)

        when (requestCode){
            2 -> {
                if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY_TAKE){
                    //選択された写真にImageViewを作成
                    val imageView = ImageView(this@PhylogeneticTree)
                    val internalUri = data?.data?.let { saveImageToInternalStorage(it) }
                    currentItem?.imageUris?.add(internalUri.toString())
                    lifecycleScope.launch {
                        saveSpeiciesDatabase()
                    }
                    imageView.setImageURI(data?.data) // handle chosen image
                    imageView.adjustViewBounds = true
                    // 画像ブロック領域にImageViewを追加
                    val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
                    val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
                    val lparams = LinearLayout.LayoutParams(wrapContent, wrapContent)
                    imageLayout.addView(imageView, lparams)
                }
            }
        }
    }


    // Method to save an image to internal storage
    private fun saveImageToInternalStorage(uri:Uri):Uri{
        // Get the bitmap from drawable object
        val bitmap =  MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)
        // Initializing a new file
        // The bellow line return a directory in internal storage
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flush the stream
            stream.flush()
            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }
    // endregion

    // region explanation
    // 説明欄を表示する
    fun showExplanation() {
        val explanationText = findViewById<EditText>(R.id.explanation)
        with(explanationText) {
            setText(currentItem?.explanation, TextView.BufferType.NORMAL)
            addTextChangedListener(EditEventListener())
        }
    }

    // 説明欄の更新を受け取るクラス
    private class EditEventListener : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            Log.i("MyPlantpediA", "onTextChanged")
        }

        override fun afterTextChanged(p0: Editable?) {
            Log.i("MyPlantpediA", "text is changed")
        }

    }

    fun saveExplanation() {
        val explanationText = findViewById<EditText>(R.id.explanation)
        currentItem?.explanation = explanationText.text.toString()
        lifecycleScope.launch {
            saveSpeiciesDatabase()
        }
    }

    fun PhylogeneticTree.hideKeyboard() {
        val view = this@PhylogeneticTree.currentFocus
        if (view != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    saveExplanation()
                    hideKeyboard()
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // endregion

    // region childrenItemList
    // 子アイテムリストを表示する
    fun showChildrenItemList() {
        var stringList = mutableListOf<String>()
        currentItem?.childrenName?.forEach { item ->
            stringList.add(item)
        }
        Log.i("MyPlantpediA children list = ", stringList.toString())
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMenu)
        recyclerView.adapter = RecyclerAdapter(stringList)
        val layout = LinearLayoutManager(this@PhylogeneticTree)
        recyclerView.layoutManager = layout
        // 区切り線
        val decorator = DividerItemDecoration(this@PhylogeneticTree, layout.orientation)
        recyclerView.addItemDecoration(decorator)

        /*
        val adapter = ArrayAdapter(this@PhylogeneticTree, android.R.layout.simple_list_item_1, stringList)
        val lvMenu = findViewById<ListView>(R.id.lvMenu)

        lvMenu.adapter = adapter
        lvMenu.onItemClickListener = ListItemClickListener()

        registerForContextMenu(lvMenu)
         */
    }

    private inner class ViewHolderList (item: View) : RecyclerView.ViewHolder(item) {
        val speciesName: TextView = item.findViewById(R.id.rowName)

        init {
            item.setOnClickListener(ListItemClickListener())
        }
    }

    private inner class RecyclerAdapter(val list: List<String>) : RecyclerView.Adapter<ViewHolderList>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderList {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_children, parent, false)
            itemView.setOnClickListener(ListItemClickListener())
            return ViewHolderList(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolderList, position: Int) {
            holder.speciesName.text = list[position]



            holder.itemView.setOnCreateContextMenuListener { contextMenu, _, _ ->
                contextMenu.add("このアイテムを削除する").setOnMenuItemClickListener {
                    // カレントアイテムを削除
                    showDeleteConfirmAlert(position)
                }
            }
        }

        override fun getItemCount(): Int = list.size
    }


    // 子アイテムをクリックしたときのリスナークラス
    private inner class ListItemClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val item = view.findViewById<TextView>(R.id.rowName).text.toString()
            val logText = "you choose " + item
            Log.i("MyPlantpediA children Item Click", logText)

            var parentTreeText = intent.getStringArrayListExtra("parentTree")
            parentTreeText?.add(currentItem?.name)

            val intent2PhylogeneticTree = Intent(this@PhylogeneticTree, PhylogeneticTree::class.java).apply {
                    putExtra("currentName", item)
                    putExtra("parentTree", parentTreeText)
           }
            startActivity(intent2PhylogeneticTree)
            // トーストの表示
            // Toast.makeText(this@PhylogeneticTree, show, Toast.LENGTH_LONG).show()
        }
    }

    // 削除確認アラートを表示する
    fun showDeleteConfirmAlert(pos: Int):Boolean {
        AlertDialog.Builder(this@PhylogeneticTree)
            .setTitle("このアイテムを削除する")
            .setMessage("子アイテムもすべて削除しますが、良いですか？")
            .setPositiveButton("OK") { dialog, which ->
                deleteCurrentItem(pos)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
        return true
    }

    // posの位置の子アイテムを削除する
    fun deleteCurrentItem(pos: Int) {
        val newList = currentItem?.childrenName as MutableList<String>?
        newList?.removeAt(pos)
        currentItem?.childrenName = newList!!
        // 再表示する
        showChildrenItemList()
        lifecycleScope.launch {
            saveSpeiciesDatabase()
        }
    }
    // endregion

    // region addChildrenItem
    public fun addChildrenItem(view: View) {
        Log.i("MyPlantpediA", "add button clicked")
        val editText = AppCompatEditText(this@PhylogeneticTree)
        AlertDialog.Builder(this@PhylogeneticTree)
            .setTitle("新しいアイテムを追加する")
            .setMessage("名前を入力してください")
            .setView(editText)
            .setPositiveButton("OK") { dialog, which ->
                val newItemName = editText.text.toString()
                lifecycleScope.launch {
                    val result = insertNewItem(newItemName) as Boolean
                    if (result) {
                        var childrenList = currentItem?.childrenName as MutableList<String>
                        childrenList.add(newItemName)
                        currentItem?.childrenName = childrenList
                        saveSpeiciesDatabase()
                        showChildrenItemList()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    suspend fun insertNewItem(newItemName: String): Boolean {
        val nextParentList = currentItem?.parentName!! + currentItem?.name
        val newItem = SpeciesEntity(newItemName, "", nextParentList as List<String>, listOf<String>())

        val db = SpeciesRoomDatabase.getDatabase(this@PhylogeneticTree)
        val speciesDao = db.speciesDao()
        if (speciesDao.isRowIsExist(newItemName)) {
            Toast.makeText(this@PhylogeneticTree, "この種類はすでに登録されています。違う名前を指定するか登録済みのアイテムを削除してください。", Toast.LENGTH_LONG).show()
            return false
        } else {
            speciesDao.insertWithTimestamp(newItem)
            return true
        }
    }
    // endregion

    fun onBackButtonClick(view: View) {
        finish()
    }

    // region optionMenu
    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // ホームに戻る
            R.id.menuListHome -> {
                val intent2MainActivity = Intent(this@PhylogeneticTree, MainActivity::class.java)
                startActivity(intent2MainActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    // endregion

    // region saveData
    suspend fun saveSpeiciesDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@PhylogeneticTree)
        val speciesDao = db.speciesDao()
        currentItem?.let { speciesDao.delete(it) }
        currentItem?.let { speciesDao.updateWithTimestamp(it) }
    }
    // endregion


    // 不使用　親アイテムをテキスト表示する
    //fun showParentText() {
        //var ptext = "" as String
        //var pitem = plantTree as Tree<Speicies>?
        //Log.i("MyPlantpediA", ptext)
        //while (pitem != null) {
        //    Log.i("MyPlantpediA", ptext)
        //    ptext = pitem.data.name + " -> " + ptext
        //    pitem = pitem.parent
        //}

        //val parentText = findViewById<TextView>(R.id.currentItem)
        //parentText.text = ptext
    //}
}