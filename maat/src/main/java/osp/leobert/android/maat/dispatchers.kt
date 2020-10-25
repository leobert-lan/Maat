package osp.leobert.android.maat

import osp.leobert.android.maat.JobChunk.Companion.info

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Project:</b> Maat </p>
 * <p><b>Classname:</b> dispatchers </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2020/10/24.
 */

abstract class Dispatcher {
    internal abstract fun start(maat: Maat, currentJobChunk: JobChunk?)
    internal abstract fun dispatchOnJobSuccess(maat: Maat, currentJobChunk: JobChunk?, job: JOB)
}

/**
 * 针对Job进行分发
 * */
class JobDispatcher : Dispatcher() {
    override fun start(maat: Maat, currentJobChunk: JobChunk?) {
        if (currentJobChunk?.haveNext() == true) {
            currentJobChunk.next()?.runInit(maat)
        } else {
            throw MaatException("none jobs!!!")
        }
    }

    override fun dispatchOnJobSuccess(maat: Maat, currentJobChunk: JobChunk?, job: JOB) {
        if (maat.logger.enable)
            maat.logger.log("onJobSuccess:${job.uniqueKey}, called on MainThread:${MaatUtil.isMainThread()}")
        if (currentJobChunk?.haveNext() == true) {
            currentJobChunk.next()?.runInit(maat)
        } else {
            maat.logger.takeIf { it.enable }?.log("all jobs finished")
            maat.callback?.onSuccess?.invoke(maat)
        }
    }
}

/**
 * 针对Chunk进行分发
 * */
class JobChuckDispatcher : Dispatcher() {
    override fun start(maat: Maat, currentJobChunk: JobChunk?) {
        if (currentJobChunk?.haveNext() == true) {
            while (currentJobChunk.haveNextInChunk()) {
                currentJobChunk.next()?.let {
                    currentJobChunk.markOnHandling(it.uniqueKey)
                    it.runInit(maat)
                }
            }
        } else {
            throw MaatException("none jobs!!!")
        }
    }

    override fun dispatchOnJobSuccess(maat: Maat, currentJobChunk: JobChunk?, job: JOB) {
        if (maat.logger.enable)
            maat.logger.log("onJobSuccess:${job.uniqueKey}, called on MainThread:${MaatUtil.isMainThread()}")

        currentJobChunk?.markHandled(job.uniqueKey)

        currentJobChunk?.let { chunk ->

            if (chunk.haveNextInChunk() || chunk.hasHandingJobsInChunk()) {
                if (maat.logger.enable) {
                    maat.logger.log("not finished all:${chunk.info()}")
                }

                null
            } else {
                chunk.move2NextChunk()?.let {cc->

                    if (chunk.haveNext()) { //我们可以置信不会存在空chunk
                        while (chunk.haveNextInChunk()) {
                            chunk.next()?.let {
                                currentJobChunk.markOnHandling(it.uniqueKey)
                                it.runInit(maat)
                            }
                        }
                    } else {
                        maat.logger.takeIf { it.enable }?.log("all jobs finished")
                        maat.callback?.onSuccess?.invoke(maat)
                    }
                }
            }
        }
    }
}