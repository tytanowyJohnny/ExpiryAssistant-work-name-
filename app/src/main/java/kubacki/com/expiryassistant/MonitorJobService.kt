package kubacki.com.expiryassistant

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

class MonitorJobService : JobService() {

    object MonitorJobScheduler {

        fun scheduleJob(context: Context) {

            val monitorServiceComponent = ComponentName(context, MonitorJobService::class.java)

            val builder = JobInfo.Builder(0, monitorServiceComponent)


        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}