package osp.leobert.android.maat

import android.os.Looper
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("osp.leobert.android.maat.test", appContext.packageName)

        val maat = Maat(appContext.applicationContext, 6, object : Maat.Logger() {
            override val enable: Boolean = true


            override fun log(msg: String, throws: Throwable?) {
//                println(msg)
                Log.d("maat", msg, throws)
            }

        })
        print(Thread.currentThread().name)
        maat.append(object : JOB() {
            override val uniqueKey: String = "a"
            override val dependsOn: List<String> = emptyList()
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
            }

            override fun toString(): String {
                return uniqueKey
            }

        }).append(object : JOB() {
            override val uniqueKey: String = "b"
            override val dependsOn: List<String> = arrayListOf("a")
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

        }).append(object : JOB() {
            override val uniqueKey: String = "c"
            override val dependsOn: List<String> = arrayListOf("a")
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO

            override fun init(maat: Maat) {
                Log.e(
                    "maat",
                    "run:" + uniqueKey + " isMain:" + (Looper.getMainLooper() == Looper.myLooper())
                )
            }

            override fun toString(): String {
                return uniqueKey
            }

        }).append(object : JOB() {
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