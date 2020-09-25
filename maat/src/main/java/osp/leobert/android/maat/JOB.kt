package osp.leobert.android.maat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    abstract val scope: CoroutineScope


    internal fun runInit(maat: Maat) {
        scope.launch {

            flow {
                init(maat)
                emit(true)
            }
                .catch {
                    maat.onJobFailed(this@JOB)
                }.flowOn(Dispatchers.Main)
                .collect {
                    maat.onJobSuccess(this@JOB)
                }
        }
    }

    abstract fun init(maat: Maat)


}