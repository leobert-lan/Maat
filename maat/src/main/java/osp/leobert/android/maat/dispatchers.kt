package osp.leobert.android.maat

import osp.leobert.android.maat.JobChunk.Companion.info

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Project:</b> Maat </p>
 * <p><b>Classname:</b> dispatchers </p>
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
                //fix:https://github.com/leobert-lan/Maat/issues/1 at 2020-11-06 release version 1.1.1; affect 1.1.0
                chunk.move2NextChunk()
                    .let { _ -> //if it (current chunk) is null means all job have bean handled successfully

                        // we can believe that if a chunk is not null, it must not be empty.
                        // And, the local param chunk is the original chunk-header, it can delegate
                        // the visitation of the chunk current handing. thus, if chunk.haveNext() return true,
                        // means still existing jobs to execute (in the current chunk), otherwise, all jobs have bean handled.

                        if (chunk.haveNext()) {
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