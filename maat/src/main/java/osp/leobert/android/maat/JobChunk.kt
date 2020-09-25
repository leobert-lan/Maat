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

        fun JobChunk.info(): String {
            return buildString {
                append(this@info.jobs).append(", ")
                this@info.nextChunk?.info()?.let { append(it) }
            }
        }
    }

    private val jobs: LinkedHashSet<JOB> = LinkedHashSet()

    private var current: JobChunk? = null
    var nextChunk: JobChunk? = null

    @Synchronized
    fun haveNext(): Boolean = current?.run { haveNext(this) } ?: false


    private fun haveNext(chunk: JobChunk): Boolean {
        return chunk.jobs.isNotEmpty() || (chunk.nextChunk?.run { haveNext(this) } ?: false)
    }

    @Synchronized
    fun next(): JOB? {
        synchronized(this) {
            if (haveNext()) {
                if (current?.jobs.isNullOrEmpty()) {
                    current = current?.nextChunk
                }
                return current?.jobs?.firstOrNull()?.apply {
                    current?.jobs?.remove(this)
                }
            }
            return null
        }
    }

    internal fun addJobs(jobs: List<JOB>): JobChunk {
        this.jobs.addAll(jobs)
        return this
    }

    internal fun addJob(job: JOB): JobChunk {
        this.jobs.add(job)
        return this
    }
}