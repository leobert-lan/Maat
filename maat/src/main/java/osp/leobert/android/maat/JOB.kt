package osp.leobert.android.maat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> Job </p>
 * Created by leobert on 2020/9/24.
 */
abstract class JOB {
    abstract val uniqueKey: String

    abstract val dependsOn: List<String>

    abstract val dispatcher: CoroutineDispatcher

    private var finishEmitter: FinishEmitter? = null

    open val emitSuccessAfterInitBlock = true


    internal fun runInit(maat: Maat) {
        MainScope().launch {

            flow<Boolean> {
                if (!emitSuccessAfterInitBlock)
                    finishEmitter = FinishEmitter(maat)
                init(maat)
                if (emitSuccessAfterInitBlock)
                    emit(true)
            }.flowOn(dispatcher)
                .catch {
                    maat.onJobFailed(this@JOB, it)
                }.flowOn(Dispatchers.Main)
                .collect {
                    maat.onJobSuccess(this@JOB)
                }
        }
    }

    protected fun emitSuccessAfterInit() {
        MainScope().launch {
            finishEmitter?.emit()
        }
    }

    abstract fun init(maat: Maat)

    inner class FinishEmitter(val maat: Maat) {
        var hasEmit = false

        @Synchronized
        fun emit() {
            if (hasEmit) {
                Maat.getDefault().logger.log(
                    "not allowed to emit success twice!",
                    Throwable("from $uniqueKey")
                )
                return
            }
            hasEmit = true

            maat.onJobSuccess(this@JOB)
            finishEmitter = null
        }

    }

}