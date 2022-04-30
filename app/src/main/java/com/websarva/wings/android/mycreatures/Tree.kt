package com.websarva.wings.android.mycreatures

import java.io.Serializable
import java.util.*

data class Tree<T> (
    val data: T,
    var children: MutableList<Tree<T>>,
    var parent: Tree<T>? = null,
    ) : Serializable {

    fun setChildren (child: Tree<T>) {
        children.add(child)
        child.parent = this
    }

    fun <S> mapByDFS(f: (T) -> S): Tree<S> {
        // 深さ優先探索における次に処理すべきノードを格納するスタック
        val stack: Stack<Pair<Tree<T>, List<Int>>> = Stack()
        var tree: Tree<S>? = null
        stack.push(Pair(this, listOf()))

        while (stack.isNotEmpty()) {
            val (currentTree, indexList) = stack.pop()
            val newTree = Tree(
                f(currentTree.data),
                mutableListOf()
            )
            if (tree == null) {
                tree = newTree
            } else {
                tree.find(indexList.take(indexList.size - 1))
                    ?.children?.add(newTree)
            }
            currentTree.children.withIndex().reversed().forEach { pair ->
                val (index, t) = pair
                stack.push(Pair(t, indexList.plus(index)))
            }
        }
        return tree!!
    }

    fun <S> mapByBFS(f: (T) -> S): Tree<S> {
        // 幅優先探索における次に処理すべきノードを格納するキュー
        val queue: Queue<Pair<Tree<T>, List<Int>>> = LinkedList()
        var tree: Tree<S>? = null

        queue += Pair(this, listOf())
        while (queue.isNotEmpty()) {
            val (currentTree, indexList) = queue.poll()
            val newTree = Tree(
                f(currentTree.data),
                mutableListOf()
            )
            if (tree == null) {
                tree = newTree
            } else {
                tree.find(indexList.take(indexList.size - 1))
                    ?.children?.add(newTree)
            }
            currentTree.children.withIndex().forEach { pair ->
                val (index, t) = pair
                queue += Pair(t, indexList.plus(index))
            }
        }
        return tree!!
    }

    fun <S> mapRecursively(f: (T) -> S): Tree<S> {
        fun loop(tree: Tree<T>): Tree<S> {
            return Tree(f(tree.data), tree.children.map { loop(it) }.toMutableList())
        }
        return loop(this)
    }

    fun find(indexList: List<Int>): Tree<T>? {
        var current = this
        indexList.forEach { index ->
            val currentOpt = current.children.getOrNull(index)
            if (currentOpt == null) return currentOpt
            current = currentOpt
        }
        return current
    }

    fun <S> forEach(f: (T) -> S) {
        mapByDFS(f)
    }

    fun <S> reduce(acc: S, f: (S, T) -> S): S {
        var result = acc
        forEach {
            result = f(result, it)
        }
        return result
    }

    fun mapTree(f: (Tree<T>) -> Tree<T>): Tree<T> {
        fun loop(tree: Tree<T>): Tree<T> {
            val newTree = f(tree)
            return newTree.copy(
                children = newTree.children.map { loop(it) }.toMutableList()
            )
        }
        return loop(this)
    }

    companion object {
        fun <T, ID> of(getId: (T) -> ID, getParentId: (T) -> ID?, flatList: List<T>): List<Tree<T>> {
            val parentIdToChildren = flatList.groupBy { getParentId(it) }.toMutableMap()

            fun buildTree(root: T): Tree<T> {
                val queue: Queue<Pair<T, List<Int>>> = LinkedList()
                var tree: Tree<T>? = null

                queue += Pair(root, listOf())
                while (queue.isNotEmpty()) {
                    val (currentData, indexList) = queue.poll()
                    val newTree = Tree(
                        currentData,
                        mutableListOf()
                    )
                    if (tree == null) {
                        tree = newTree
                    } else {
                        tree.find(indexList.take(indexList.size - 1))
                            ?.children?.add(newTree)
                    }
                    val children = parentIdToChildren.getOrDefault(getId(currentData), mutableListOf())
                    parentIdToChildren.remove(getId(currentData))
                    children.withIndex().forEach { pair ->
                        val (index, t) = pair
                        queue += Pair(t, indexList.plus(index))
                    }
                    println(queue)
                }
                return tree!!
            }

            val rootDataList = parentIdToChildren[null] ?: listOf()
            parentIdToChildren.remove(null)
            return rootDataList.map { root -> buildTree(root) }
        }
    }
}