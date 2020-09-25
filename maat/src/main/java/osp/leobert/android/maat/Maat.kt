package osp.leobert.android.maat

import android.content.Context
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import osp.leobert.android.maat.JobChunk.Companion.append
import osp.leobert.android.maat.JobChunk.Companion.info
import osp.leobert.android.maat.dag.DAG
import osp.leobert.android.maat.dag.Edge
import osp.leobert.android.maat.dag.Type
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> Maat </p>
 * Created by leobert on 2020/9/23.
 */
class Maat(val application: Context, private val printChunkMax: Int,
           private val logger: Logger
) {

    abstract class Logger {
        abstract val enable: Boolean
        abstract fun log(msg: String)
    }

    companion object {


        private fun DAG<JOB>.bfs(): JobChunk {

            val zeroDeque = ArrayDeque<JOB>()
            val inDegrees = HashMap<JOB, Int>().apply {
                putAll(this@bfs.inDegreeCache)
            }
            inDegrees.forEach { (v, d) ->
                if (d == 0)
                    zeroDeque.offer(v)
            }

            val head = JobChunk.head()
            var currentChunk = head

            val tmpDeque = ArrayDeque<JOB>()

            while (zeroDeque.isNotEmpty() || tmpDeque.isNotEmpty()) {
                if (zeroDeque.isEmpty()) {
                    currentChunk = currentChunk.append()
                    zeroDeque.addAll(tmpDeque)
                    tmpDeque.clear()
                }
                zeroDeque.poll()?.let { vertex ->
                    currentChunk.addJob(vertex)

                    this.getEdgeContainsPoint(vertex, Type.X).forEach { edge ->
                        inDegrees[edge.to] = (inDegrees[edge.to] ?: 0).minus(edge.weight).apply {
                            if (this == 0)
                                tmpDeque.offer(edge.to)
                        }
                    }
                }
            }
            return head
        }
    }

    private val dag: DAG<JOB> = DAG({ job -> job.uniqueKey }, printChunkMax)
    private val start = arrayListOf("start")
    private val startJob = object : JOB() {
        override val uniqueKey: String = "start"
        override val dependsOn: List<String> = emptyList()
        override val scope: CoroutineScope =
            CoroutineScope(Dispatchers.Main + kotlinx.coroutines.Job())

        override fun init(maat: Maat) {
        }

        override fun toString(): String {
            return uniqueKey
        }

    }

    private val allJobsCache = hashMapOf<String, JOB>()
    private val allJobsNameCache = linkedSetOf<String>()
    private val allDependsOnCache = hashSetOf<String>()
    private var currentJobChunk: JobChunk? = null

    fun append(job: JOB): Maat {
        allJobsCache[job.uniqueKey] = job
        allJobsNameCache.add(job.uniqueKey)
        allDependsOnCache.addAll(job.dependsOn)
        return this
    }

    @Throws(MaatException::class)
    fun start() {
        allJobsNameCache.forEach { allDependsOnCache.remove(it) }
        allDependsOnCache.takeIf { it.isNotEmpty() }
            ?.let { throw MaatException("missing jobs:$it") }

        //below 24
        for (entry in allJobsCache) {
            entry.value.let { job ->
                (job.dependsOn.takeIf { it.isNotEmpty() } ?: start).forEach {
                    dag.addEdge(Edge(allJobsCache[it] ?: startJob, job))
                }
            }
        }

        if (logger.enable) {
            logger.log(dag.debugMatrix())
        }

        dag.recursive(startJob, arrayListOf())
        if (dag.loopbackList.isNotEmpty()) {
            throw MaatException("cycle exist:${dag.loopbackList}")
        }

        if (logger.enable) {
            logger.log("全部路径：")
            dag.deepPathList.forEach {
                logger.log(it.toString())
            }
        }

        currentJobChunk = createJobChunk()

        if (logger.enable)
            logger.log("init order: ${currentJobChunk?.info()}")

//        createJobChunk().let {
//            println(it.info())
//            print("sort:")
//
//            while (it.haveNext()) {
//                print(it.next()?.uniqueKey + ", ")
//            }
//        }
        if (currentJobChunk?.haveNext() == true) {
            currentJobChunk?.next()?.runInit(this)
        } else {

        }

    }

    @MainThread
    fun onJobFailed(job: JOB) {

    }

    @MainThread
    fun onJobSuccess(job: JOB) {
        if (currentJobChunk?.haveNext() == true) {
            currentJobChunk?.next()?.runInit(this)
        } else {

        }
    }

    fun createJobChunk(): JobChunk {
        return dag.bfs()
    }
}