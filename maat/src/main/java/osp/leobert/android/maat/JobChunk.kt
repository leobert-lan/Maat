package osp.leobert.android.maat

/**
 * <p><b>Package:</b> osp.leobert.android.maat </p>
 * <p><b>Classname:</b> JobChunk </p>
 * Created by leobert on 2020/9/24.
 */
class JobChunk private constructor() {

    companion object {
        fun head() = JobChunk().apply {
            this.current = this
        }

        fun chunk() = JobChunk()

        fun JobChunk.append(): JobChunk {
            return chunk().apply {
                this@append.nextChunk = this
            }
        }
    }

    private val jobs: LinkedHashSet<Job> = LinkedHashSet()

    private var current: JobChunk? = null
    var nextChunk: JobChunk? = null

    @Synchronized
    fun haveNext(): Boolean = jobs.isNotEmpty() || nextChunk?.haveNext() == true

    @Synchronized
    fun next(): Job? {
        synchronized(this) {
            if (haveNext()) {
                if (current?.jobs?.isNullOrEmpty() == true) {
                    current = nextChunk
                }
                return current?.jobs?.firstOrNull()?.apply {
                    current?.jobs?.remove(this)
                }
            }
            return null
        }
    }

    internal fun addJobs(jobs: List<Job>): JobChunk {
        this.jobs.addAll(jobs)
        return this
    }
}