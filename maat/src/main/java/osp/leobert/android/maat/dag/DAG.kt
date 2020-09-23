package osp.leobert.android.maat.dag

import java.util.*
import kotlin.collections.HashMap

/**
 * <p><b>Package:</b> osp.leobert.android.maat.dag </p>
 * <p><b>Classname:</b> DAG </p>
 * Created by leobert on 2020/9/23.
 */
class DAG<T>(val nameOf: (T) -> String) {
    //记录两份网络节点,使用空间换时间,提升反向查询效率

    //网络初始化,x_k,y_k
    private val xMapMap: Map<T, Map<String, String>> = HashMap()

    //网络初始化,y_k,x_k
    private val yMapMap: Map<T, Map<String, String>> = HashMap()

    //回环路径
    private val loopbackList: List<String> = LinkedList()

    //节点深度路径
    private val deepPathList: List<List<String>> = LinkedList()

    fun getAllPoint(): Set<T> {
        val allSet1 = xMapMap.keys
        val allSet2 = yMapMap.keys
        val allSet: MutableSet<T> = HashSet()
        allSet.addAll(allSet1)
        allSet.addAll(allSet2)
        return allSet
    }

    fun show() {
        val placeholder = 3
        val placeholderString = StringBuilder()
        for (i in 0 until placeholder) {
            placeholderString.append("-")
        }
        val allSet: Set<T> = getAllPoint() //获取所有的点,用于遍历
        print(String.format("%-" + placeholder + "s", ""))
        print(" ")
        for (x in allSet) {
            print(String.format("%-" + placeholder + "s", x))
        }
        println()
        print(String.format("%-" + placeholder + "s", "X\\Y"))
        print(" ")
        for (ignored in allSet) {
            print(placeholderString)
        }
        println()
        for (x in allSet) {
            print(String.format("%-" + placeholder + "s|", x))
            for (y in allSet) {
                val linePoints: Map<String, String?>? =
                    xMapMap.get(x)
                var point: String? = "0"
                if (linePoints != null && linePoints[y] != null) {
                    point = linePoints[y]
                }
                print(String.format("%-" + placeholder + "s", point))
            }
            println()
        }
    }
}