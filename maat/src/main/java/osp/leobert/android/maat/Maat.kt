package osp.leobert.android.maat

import android.app.Application
import osp.leobert.android.maat.dag.DAG

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> Maat </p>
 * Created by leobert on 2020/9/23.
 */
class Maat(val application: Application, private val printChunkMax: Int) {

    val dag: DAG<Job> = DAG({ job -> job.uniqueKey }, printChunkMax)

    private val allJobsCache = linkedSetOf<Job>()
    private val allJobsKeyCache = hashSetOf<String>()

    fun append(job: Job): Maat {
        allJobsCache.add(job)
        allJobsKeyCache.addAll(job.dependsOn)
        return this
    }

    @Throws(MaatException::class)
    fun start() {

    }

    fun onJobFailed(job: Job) {

    }

    fun onJobSuccess(job: Job) {

    }
}