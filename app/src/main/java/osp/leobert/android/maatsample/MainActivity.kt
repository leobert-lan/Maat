package osp.leobert.android.maatsample

import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import osp.leobert.android.maat.JOB
import osp.leobert.android.maat.Maat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val maat = Maat(this, 6, object : Maat.Logger() {
            override val enable: Boolean = true


            override fun log(msg: String) {
//                println(msg)
                Log.d("maat", msg)
            }

        })
        print(Thread.currentThread().name)
        maat.append(object : JOB() {
            override val uniqueKey: String = "a"
            override val dependsOn: List<String> = emptyList()
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/

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
            override val dispatcher: CoroutineDispatcher = Dispatchers.Main /* + Job()*/

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
            override val dispatcher: CoroutineDispatcher = Dispatchers.IO /* + Job()*/

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