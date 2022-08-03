package com.example.workmanagersample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOneTimeRequest = findViewById<Button>(R.id.btnOneTimeRequest)
        val tvOneTimeRequest = findViewById<TextView>(R.id.tvOneTimeRequest)
        val btnPeriodicRequest = findViewById<Button>(R.id.btnPeriodicRequest)

        btnOneTimeRequest.setOnClickListener {
            val oneTimeRequestConstraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val data = Data.Builder()
            data.putString("inputKey", "input value")

            val sampleWork = OneTimeWorkRequest
                .Builder(OneTimeRequestWorker::class.java)
                .setInputData(data.build())
                .setConstraints(oneTimeRequestConstraints)
                .build()

            WorkManager.getInstance(this).enqueue(sampleWork)
            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(sampleWork.id)
                .observe(this) { workInfo ->
                    OneTimeRequestWorker.Companion.logger(workInfo.state.name)

                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED -> tvOneTimeRequest.text = "Task enqueued"
                            WorkInfo.State.BLOCKED -> tvOneTimeRequest.text = "Task blocked"
                            WorkInfo.State.RUNNING -> tvOneTimeRequest.text = "Task running"
                            else -> tvOneTimeRequest.text = "Other tasks"
                        }
                    }

                    if (workInfo != null &&  workInfo.state.isFinished) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                tvOneTimeRequest.text = "Task successful"

                                val successOutputData = workInfo.outputData
                                val outputText = successOutputData.getString("outputKey")
                                Log.i("Worker output", "$outputText")
                            }
                            WorkInfo.State.FAILED -> tvOneTimeRequest.text = "Task falied"
                            WorkInfo.State.CANCELLED -> tvOneTimeRequest.text = "Task Cancelled"
                            else -> tvOneTimeRequest.text = "Task finished else part"
                        }
                    }
                }
        }

        btnPeriodicRequest.setOnClickListener {
            val periodicRequestConstraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                PeriodicRequestWorker::class.java,
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(periodicRequestConstraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "Periodic Work Request",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }
}