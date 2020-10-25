package osp.leobert.android.maatsample

import android.util.Log
import kotlinx.coroutines.*
import osp.leobert.android.maat.MaatUtil

/**
 * <p><b>Package:</b> osp.leobert.android.maatsample </p>
 * <p><b>Project:</b> Maat </p>
 * <p><b>Classname:</b> MockAsync </p>
 * Created by leobert on 2020/10/24.
 */
object MockAsync {

    fun init(callback: () -> Unit) {
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default) {
                Log.e("maat", "模拟子线程异步初始化，回调主线程通知加载完成,isMain:${MaatUtil.isMainThread()},${System.currentTimeMillis()}")
                delay(200)
            }
            Log.e("maat", "模拟加载完成，主线程通知加载完成,isMain:${MaatUtil.isMainThread()},${System.currentTimeMillis()}")

            callback.invoke()
        }
    }
}