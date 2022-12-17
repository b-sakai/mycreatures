package com.websarva.wings.android.mycreatures

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.mycreatures.database.SpeciesEntity
import com.websarva.wings.android.mycreatures.database.SpeciesRoomDatabase
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

        val root = SpeciesEntity(1, 0,"root", "")

        var angiosperm = SpeciesEntity(2, 1, res.getString(R.string.angiosperm), "")
        angiosperm.explanation = "一般に花と呼ばれる生殖器官の特殊化が進んで、胚珠が心皮にくるまれて子房の中に収まったもの (from wikipedia)"

        var gymnosperm = SpeciesEntity(3, 1, "裸子植物", "")
        gymnosperm.explanation = "種子を形成する植物のうち、胚珠がむき出しになっているもの"

        var pteridophytes = SpeciesEntity(4, 1, res.getString(R.string.pteridophytes), "")
        pteridophytes.explanation = "種子植物でない植物の総称"


        var eudicots = SpeciesEntity(5, 2, res.getString(R.string.eudicots), "")
        eudicots.explanation = "別名三溝粒類と呼ばれる、花粉の発芽溝または発芽孔が基本的に3個あるグループ。"
        var chrysanthemums = SpeciesEntity(6, 5,"キク類", "")
        chrysanthemums.explanation = "バラ類と並ぶ分類のためのクレード（ある共通の祖先から進化した生物すべてを含む生物群のこと）の一つ"

        speciesDao.insertWithTimestamp(root)
        speciesDao.insertWithTimestamp(angiosperm)
        speciesDao.insertWithTimestamp(gymnosperm)
        speciesDao.insertWithTimestamp(pteridophytes)
        speciesDao.insertWithTimestamp(eudicots)
        speciesDao.insertWithTimestamp(chrysanthemums)
    }

    suspend fun checkDatabase() {
        val db = SpeciesRoomDatabase.getDatabase(this@MainActivity)
        val speciesDao = db.speciesDao()
        val eudicots = speciesDao.get(4)
        Log.i("MyplantpediA", eudicots.toString())

        val root = speciesDao.get(0)
        Log.i("MyplantpediA", root.toString())

    }


    public fun createPhylogeneticTreeView(view: View) {
        Log.i("MyPaintpediA", "cratePhylogeneticTreeView called.")
        val intent2PhylogeneticTree = Intent(this@MainActivity, PhylogeneticTree::class.java).apply {
            putExtra("currentId", 1)
        }

        startActivity(intent2PhylogeneticTree)
    }

    public fun createRecentItemView(view: View) {
        val intent2RecentItemView = Intent(this@MainActivity, RecentItemView::class.java)
        startActivity(intent2RecentItemView)
    }

    public fun createClearChecker(view: View) {
        Toast.makeText(this@MainActivity, "この機能はまだ実装されていません。", Toast.LENGTH_LONG).show()
        return

        val intent2TreeView = Intent(this@MainActivity, TreeView::class.java)
        startActivity(intent2TreeView)

    }

}