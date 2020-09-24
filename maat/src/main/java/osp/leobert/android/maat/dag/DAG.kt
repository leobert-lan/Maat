package osp.leobert.android.maat.dag

import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * <p><b>Package:</b> osp.leobert.android.maat.dag </p>
 * <p><b>Classname:</b> DAG </p>
 * Created by leobert on 2020/9/23.
 */
class DAG<T>(val nameOf: (T) -> String, val printChunkMax: Int) {
    //记录两份网络节点,使用空间换时间,提升反向查询效率

    //网络初始化,x_k,y_k
    private val edgesByStart: MutableMap<T, MutableMap<T, Int>> = HashMap()

    //网络初始化,y_k,x_k
    private val edgesByEnd: MutableMap<T, MutableMap<T, Int>> = HashMap()

    //回环路径
    val loopbackList: MutableList<String> = LinkedList()

    //节点深度路径
    val deepPathList: MutableList<MutableList<T>> = LinkedList()

    fun addEdge(edge: Edge<T>) {
        if (!edgesByStart.containsKey(edge.from)) {
            edgesByStart[edge.from] = hashMapOf()
        }
        edgesByStart[edge.from]?.put(edge.to, edge.degree)

        if (!edgesByEnd.containsKey(edge.to)) {
            edgesByEnd[edge.to] = HashMap()
        }
        edgesByEnd[edge.to]?.put(edge.from, edge.degree)
    }

    private val allPoint: Set<T>
        get() = HashSet<T>().apply {
            this.addAll(edgesByStart.keys)
            this.addAll(edgesByEnd.keys)
        }


    fun getEdgeContainsPoint(point: T, type: Type): List<Edge<T>> {
        val linePointList: MutableList<Edge<T>> = ArrayList()
        if (type == Type.X) {
            edgesByStart[point]?.forEach {
                linePointList.add(Edge(point, it.key, it.value))
            }
        } else {
            edgesByEnd[point]?.forEach {
                linePointList.add(Edge(it.key, point, it.value))
            }
        }
        return linePointList
    }

    private fun debugPathInfo(pathList: MutableList<T>):String {
       return pathList.map { it.let(nameOf) }.toString()
    }

    fun recursive(startPoint: T, pathList: MutableList<T>) {
        if (pathList.contains(startPoint)) {
            loopbackList.add("${debugPathInfo(pathList)}->${startPoint.let(nameOf)}")
            return
        }
        pathList.add(startPoint)
        val linePoint = getEdgeContainsPoint(startPoint, Type.X)
        if (linePoint.isEmpty()) {
            val descList: ArrayList<T> = ArrayList(pathList.size)
            pathList.forEach { path -> descList.add(path) }
            deepPathList.add(descList)
        }
        linePoint.forEach {
            recursive(it.to, pathList)
        }

        pathList.remove(startPoint)
    }

    fun debugMatrix(): String {
        val placeholder = printChunkMax

        val placeholderString = StringBuilder()
        for (i in 0 until placeholder) {
            placeholderString.append("-")
        }
        val info = StringBuilder()
        val allSet: Set<T> = allPoint

        info.append(String.format("%-" + placeholder + "s", "")).append(" ")
        for (x in allSet) {
            info.append(String.format("%-" + placeholder + "s", x.let(nameOf)))
        }
        info.append("\n").append(String.format("%-" + placeholder + "s", "X\\Y")).append(" ")
        for (ignored in allSet) {
            info.append(placeholderString)
        }

        info.append("\n")

        for (x in allSet) {
            info.append(String.format("%-" + placeholder + "s|", x.let(nameOf)))

            for (y in allSet) {
                val linePoints: Map<T, Int>? = edgesByStart[x]
                var degree: Int? = 0
                if (linePoints != null && linePoints[y] != null) {
                    degree = linePoints[y]
                }
                info.append(String.format("%-" + placeholder + "s", (degree ?: 0).toString()))
            }
            info.append("\n")
        }
        return info.toString()
    }
}

fun main() {
    val dag = DAG<String>(nameOf = { it }, printChunkMax = 3)
    dag.addEdge(Edge("a", "b", 1))
    dag.addEdge(Edge("a", "c", 1))
    dag.addEdge(Edge("c", "d", 1))
    dag.addEdge(Edge("b", "d", 1))
    dag.addEdge(Edge("a", "e", 1))
    dag.debugMatrix().let {
        println(it)
    }
    dag.recursive("a", arrayListOf())
    if (dag.loopbackList.isNotEmpty()) {
        throw RuntimeException("cycle exist:"+dag.loopbackList)
    }
    println(dag.deepPathList)
}