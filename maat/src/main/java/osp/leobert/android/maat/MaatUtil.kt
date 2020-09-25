package osp.leobert.android.maat

import android.os.Looper

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> MaatUtil </p>
 * Created by leobert on 2020/9/25.
 */
object MaatUtil {
    fun isMainThread():Boolean = Looper.getMainLooper() == Looper.myLooper()
}