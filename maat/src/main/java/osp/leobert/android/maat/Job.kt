package osp.leobert.android.maat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> Job </p>
 * Created by leobert on 2020/9/24.
 */
abstract class Job {
    abstract val uniqueKey: String

    abstract val dependsOn: List<String>

    abstract val scope: CoroutineScope


    @InternalCoroutinesApi
    internal fun runInit(maat: Maat) {
        scope.launch {

            flow<Unit> {
                init(maat)
            }
                .flowOn(scope.coroutineContext)
                .catch {
                    maat.onJobFailed(this@Job)
                }.flowOn(Dispatchers.Main)
                .collect(object : FlowCollector<Unit> {
                    override suspend fun emit(value: Unit) {
                        maat.onJobSuccess(this@Job)
                    }

                })
        }
    }

    abstract fun init(maat: Maat)


}