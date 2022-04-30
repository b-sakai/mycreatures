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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import kotlinx.coroutines.launch


class TreeView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tree_view)


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
                holder.textView.text = getNodeData(position).toString()
                holder.textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                holder.textView.setBackgroundResource(R.drawable.node_background)
                holder.textView.setTextColor(Color.WHITE)
            }
        }.apply {
            // 4.3 Submit the graph
            this.submitGraph(graph)
            recycler.adapter = this
        }
        recycler.adapter = adapter

        val edgeStyle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 5f
            color = R.color.teal_700// Color.BLUE
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            pathEffect = CornerPathEffect(10f)
        }

        recycler.addItemDecoration(TreeEdgeDecoration(edgeStyle))

    }

    private inner class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.treeNodeName)
    }

    private suspend fun createGraph():Graph {
        val db = SpeciesRoomDatabase.getDatabase(this@TreeView)
        val speciesDao = db.speciesDao()
        val allItem = speciesDao.getAllItem()

        val graph = Graph()
        for (item in allItem) {
            val name = item.name
            val currentNode = Node(name)
            for (child in item.childrenName) {
                val childNode = Node(child)
                graph.addEdge(currentNode, childNode)
                Log.i("MyPlantpediA", child)
            }
        }
        Log.i("MyPlantpediA", graph.toString())
        return graph
     }

    // 子アイテムをクリックしたときのリスナークラス
    private inner class NodeClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val item = view.findViewById<TextView>(R.id.treeNodeName).text.toString()
            val logText = "you choose " + item
            Log.i("MyPlantpediA children Item Click", logText)

            var parentTreeText = arrayListOf<String>()

            val intent2PhylogeneticTree = Intent(this@TreeView, PhylogeneticTree::class.java).apply {
                putExtra("currentName", item)
                putExtra("parentTree", parentTreeText)
            }
            startActivity(intent2PhylogeneticTree)
            // トーストの表示
            // Toast.makeText(this@PhylogeneticTree, show, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun updateGraph() {
        val graph2 = createGraph()
        val adapter = findViewById<RecyclerView>(R.id.recycler_tree_view).adapter as AbstractGraphAdapter<NodeViewHolder>
        adapter.submitGraph(graph2)
    }
}