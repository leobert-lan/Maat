package osp.leobert.android.maatsample

import android.app.Application
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import osp.leobert.android.maat.JOB
import osp.leobert.android.maat.JobChuckDispatcher
import osp.leobert.android.maat.Maat

/**
 * <p><b>Package:</b> osp.leobert.android.maatsample </p>
 * <p><b>Project:</b> Maat </p>
 * <p><b>Classname:</b> App </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2020/10/24.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val maat = Maat.init(application = this, printChunkMax = 10,
            logger = object : Maat.Logger() {
                override val enable: Boolean = true


                override fun log(msg: String, throws: Throwable?) {
                    Log.d("maat", "\n"+msg, throws)
                }

            }, callback = Maat.Callback(onSuccess = {}, onFailure = { maat, job, throwable ->

            }), dispatcher = JobChuckDispatcher()
        )

        maat
            .append(object : JOB() {
                override val uniqueKey: String = "chunk1_1"
                override val dependsOn: List<String> = emptyList()
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO
                override val emitSuccessAfterInitBlock: Boolean = false
                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    MockAsync.init() {
                        Log.e(
                            "maat",
                            "run:" + uniqueKey + "异步完成 isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                        )
                        emitSuccessAfterInit()
                    }
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })
            .append(object : JOB() {
                override val uniqueKey: String = "chunk1_2"
                override val dependsOn: List<String> = emptyList()
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO
                override val emitSuccessAfterInitBlock: Boolean = false
                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    MockAsync.init() {
                        Log.e(
                            "maat",
                            "run:" + uniqueKey + "异步完成 isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                        )
                        emitSuccessAfterInit()
                    }
                    //test exception
//                throw NullPointerException("just a test")
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })
            .append(object : JOB() {
                override val uniqueKey: String = "a"
                override val dependsOn: List<String> = emptyList()
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO
                override val emitSuccessAfterInitBlock: Boolean = false
                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    MockAsync.init() {
                        Log.e(
                            "maat",
                            "run:" + uniqueKey + "异步完成 isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                        )
                        emitSuccessAfterInit()
                    }
                    //test exception
//                throw NullPointerException("just a test")
                }

                override fun toString(): String {
                    return uniqueKey
                }

            }).append(object : JOB() {
                override val uniqueKey: String = "b"
                override val dependsOn: List<String> = arrayListOf("a")
                override val dispatcher: CoroutineDispatcher = Dispatchers.Main /* + Job()*/

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    //模拟误操作1：同步初始化，但是调用了触发： 并不会出问题
                    emitSuccessAfterInit()
                }

                override fun toString(): String {
                    return uniqueKey
                }

            }).append(object : JOB() {
                override val uniqueKey: String = "c"
                override val dependsOn: List<String> = arrayListOf("a")
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/
                override val emitSuccessAfterInitBlock: Boolean = false

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    //模拟误操作2：多次触发：
                    emitSuccessAfterInit()
                    emitSuccessAfterInit() //已经释放，并不会有
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })
            .append(object : JOB() {
                override val uniqueKey: String = "b"
                override val dependsOn: List<String> = arrayListOf("chunk1_1")
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/
                override val emitSuccessAfterInitBlock: Boolean = false

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    //模拟误操作2：多次触发：
                    emitSuccessAfterInit()
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })
            .append(object : JOB() {
                override val uniqueKey: String = "a_chunk2_1"
                override val dependsOn: List<String> = arrayListOf("a")
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/
                override val emitSuccessAfterInitBlock: Boolean = false

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    //模拟误操作2：多次触发：
                    emitSuccessAfterInit()
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })
            .append(object : JOB() {
                override val uniqueKey: String = "a_chunk2_2"
                override val dependsOn: List<String> = arrayListOf("a")
                override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/
                override val emitSuccessAfterInitBlock: Boolean = false

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                    //模拟误操作2：多次触发：
                    emitSuccessAfterInit()
                }

                override fun toString(): String {
                    return uniqueKey
                }

            })


            .append(object : JOB() {
                override val uniqueKey: String = "d"
                override val dependsOn: List<String> = arrayListOf("a", "b", "c")
                override val dispatcher: CoroutineDispatcher = Dispatchers.Main

                override fun init(maat: Maat) {
                    Log.e(
                        "maat",
                        "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                    )
                }

                override fun toString(): String {
                    return uniqueKey
                }

            }).start()
    }
}