package com.websarva.wings.android.mycreatures

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.mycreatures.database.SpeciesEntity
import com.websarva.wings.android.mycreatures.database.SpeciesRoomDatabase
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class TreeView : AppCompatActivity() {
    private var currentItem : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("TreeView", "onCreate() is Called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tree_view)
        val recycler = findViewById<RecyclerView>(R.id.recycler_tree_view)
        recycler.visibility = View.GONE

        currentItem = intent.getStringExtra("currentItem") as String

        lifecycleScope.launch {
            setupGraphView(this@TreeView)
        }
    }

    @SuppressLint("ResourceAsColor")
    private suspend fun setupGraphView(context: Context) {
        val recycler = findViewById<RecyclerView>(R.id.recycler_tree_view)

        // 1. Set a layout manager of the ones described above that the RecyclerView will use.
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        recycler.layoutManager = BuchheimWalkerLayoutManager(context, configuration).apply {
           useMaxSize = true
        }

        // 2. Attach item decorations to draw edges
        recycler.addItemDecoration(TreeEdgeDecoration())

        // 3. Build your graph
        val graph = createGraph()
        //val graph = createTestGraph()


        // 4. You will need a simple Adapter/ViewHolder.
        // 4.1 Your Adapter class should extend from `AbstractGraphAdapter`
        val adapter = object : AbstractGraphAdapter<NodeViewHolder>() {

            // 4.2 ViewHolder should extend from `RecyclerView.ViewHolder`
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tree_node, parent, false)
                view.setOnClickListener(NodeClickListener())
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                val isLeaf = !graph.hasSuccessor(graph.nodes[position])
                holder.textView.text = getNodeData(position).toString()
                holder.textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                if (holder.textView.text == currentItem) {
                    holder.textView.setBackgroundResource(R.drawable.current_background)
                } else if (isLeaf) {
                    holder.textView.setBackgroundResource(R.drawable.leaf_background)
                } else {
                    holder.textView.setBackgroundResource(R.drawable.node_background)
                }
                holder.textView.setTextColor(Color.WHITE)
            }
        }.apply {
            // 4.3 Submit the graph
            this.submitGraph(graph)
        }

        recycler.adapter = adapter
        val edgeStyle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 5f
            color = R.color.teal_700// Color.BLUE
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            pathEffect = CornerPathEffect(20f)
        }

        recycler.addItemDecoration(TreeEdgeDecoration(edgeStyle))
        recycler.visibility = View.VISIBLE
    }

    private inner class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.treeNodeName)
    }

    private suspend fun createGraph():Graph {
        val db = SpeciesRoomDatabase.getDatabase(this@TreeView)
        val speciesDao = db.speciesDao()
        val allItem = speciesDao.getAllItem()

        val graph = Graph()

        val nodes = mutableMapOf<String, Node>()
        for (item in allItem) {
            val name = item.name
            val currentNode = Node(name)
            nodes[name] = currentNode
        }

        for (item in allItem) {
            val name = item.name
            val currentNode = nodes[name]
            //Log.i("MyPlantpediA parent", name)
            val childrenName = getChildItemListFromDatabase(item)
            for (child in childrenName) {
                val childNode = nodes[child]
                if (currentNode != null && childNode != null) {
                    graph.addEdge(currentNode, childNode)
                }
                //Log.i("MyPlantpediA", child)
            }
        }
        Log.i("MyPlantpediA", graph.toString())
        return graph
     }

    // 子アイテムリストをデータベースから取得する
    suspend fun getChildItemListFromDatabase(item: SpeciesEntity?): List<String> {
        val curId = item?.id as Int
        val db = SpeciesRoomDatabase.getDatabase(this@TreeView)
        val speciesDao = db.speciesDao()
        val childrenItem = speciesDao.getChildrenItem(curId) as List<SpeciesEntity>
        return childrenItem.map { it.name } as List<String>
    }

    private suspend fun createTestGraph():Graph {
        val graph = Graph()

        val node1 = Node("Parent")
        val node2 = Node("Child 1")
        val node3 = Node("Child 2")

        val node4 = Node("Child 4")
        val node5 = Node("Child 5")
        graph.addNode(node1)
        graph.addNode(node2)
        graph.addNode(node3)
        graph.addNode(node4)
        graph.addNode(node5)




        graph.addEdge(node3, node4)
        graph.addEdge(node4, node5)

        val node6 = Node("Child 6")
        graph.addEdge(node3, node6)
        graph.addEdge(node1, node2)
        graph.addEdge(node1, node3)

        return graph
    }

    // 子アイテムをクリックしたときのリスナークラス
    private inner class NodeClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val item = view.findViewById<TextView>(R.id.treeNodeName).text.toString()
            val logText = "you choose " + item
            Log.i("MyPlantpediA children Item Click", logText)

            var nextId :Int?
            runBlocking {
                nextId = getIdFromName(item)
            }

            val intent2PhylogeneticTree = Intent(this@TreeView, PhylogeneticTree::class.java).apply {
                putExtra("currentId", nextId)
            }
            startActivity(intent2PhylogeneticTree)
            // トーストの表示
            // Toast.makeText(this@PhylogeneticTree, show, Toast.LENGTH_LONG).show()
        }
    }
    suspend fun getIdFromName(name: String): Int? {
        val db = SpeciesRoomDatabase.getDatabase(this@TreeView)
        val speciesDao = db.speciesDao()
        val item = speciesDao.getByName(name)
        return item?.id
    }

    private suspend fun updateGraph() {
        val graph2 = createGraph()
        val adapter = findViewById<RecyclerView>(R.id.recycler_tree_view).adapter as AbstractGraphAdapter<NodeViewHolder>
        adapter.submitGraph(graph2)
    }
}