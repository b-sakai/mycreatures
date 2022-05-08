package com.websarva.wings.android.mycreatures

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.opengl.ShaderView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var glView: ShaderView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glView = findViewById<ShaderView>(R.id.shaderView)
        lifecycleScope.launch {
            //deleteDatabase()
            //createBaseDatabase()
            //checkDatabase()
        }
    }

    public suspend fun deleteDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@MainActivity)
        val speciesDao = db.speciesDao()
        speciesDao.clear()
    }

    public suspend fun createBaseDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@MainActivity)
        val speciesDao = db.speciesDao()

        val res = resources


        var angiosperm = SpeciesEntity(res.getString(R.string.angiosperm), "", listOf("root"), listOf<String>())
        angiosperm.explanation = "一般に花と呼ばれる生殖器官の特殊化が進んで、胚珠が心皮にくるまれて子房の中に収まったもの (from wikipedia)"

        var gymnosperm = SpeciesEntity(res.getString(R.string.gymnosperm), "", listOf("root"), listOf<String>())
        gymnosperm.explanation = "種子を形成する植物のうち、胚珠がむき出しになっているもの"

        var pteridophytes = SpeciesEntity(res.getString(R.string.pteridophytes), "", listOf("root"), listOf<String>())
        pteridophytes.explanation = "種子植物でない植物の総称"

        val rootChildren = listOf(angiosperm.name, gymnosperm.name, pteridophytes.name)
        val root = SpeciesEntity("root", "", listOf<String>(), rootChildren)

        var eudicots = SpeciesEntity(res.getString(R.string.eudicots), "", listOf(angiosperm.name), listOf<String>())
        eudicots.explanation = "別名三溝粒類と呼ばれる、花粉の発芽溝または発芽孔が基本的に3個あるグループ。"
        var chrysanthemums = SpeciesEntity("キク類", "", listOf<String>(), listOf<String>())
        chrysanthemums.explanation = "バラ類と並ぶ分類のためのクレード（ある共通の祖先から進化した生物すべてを含む生物群のこと）の一つ"

        setChildren(angiosperm, eudicots)
        setChildren(eudicots, chrysanthemums)



        speciesDao.insertWithTimestamp(root)
        speciesDao.insertWithTimestamp(angiosperm)
        speciesDao.insertWithTimestamp(gymnosperm)
        speciesDao.insertWithTimestamp(pteridophytes)
        speciesDao.insertWithTimestamp(eudicots)
        speciesDao.insertWithTimestamp(chrysanthemums)
    }

    fun setChildren(parent: SpeciesEntity, child: SpeciesEntity) {
        child.parentName = parent.parentName + parent.name
        parent.childrenName = parent.childrenName + child.name
    }

    suspend fun checkDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@MainActivity)
        val speciesDao = db.speciesDao()
        val eudicots = speciesDao.get(resources.getString(R.string.eudicots))
        Log.i("MyplantpediA", eudicots.toString())

        val root = speciesDao.get("root")
        Log.i("MyplantpediA", root.toString())

    }


    public fun createPhylogeneticTreeView(view: View) {
        Log.i("MyPaintpediA", "cratePhylogeneticTreeView called.")
        val intent2PhylogeneticTree = Intent(this@MainActivity, PhylogeneticTree::class.java).apply {
            putExtra("currentName", "root")
        }

        startActivity(intent2PhylogeneticTree)
    }

    public fun createRecentItemView(view: View) {
        val intent2RecentItemView = Intent(this@MainActivity, RecentItemView::class.java)
        startActivity(intent2RecentItemView)
    }

    public fun createClearChecker(view: View) {
        val intent2TreeView = Intent(this@MainActivity, TreeView::class.java)
        startActivity(intent2TreeView)
    }

}