package id.vincent.neoz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var logo: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var progressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logo = findViewById(R.id.logo)
        progressBar = findViewById(R.id.loading)

        // Show the ProgressBar while loading
        progressBar.visibility = View.VISIBLE

        // Start updating the progress bar
        updateProgressBar()
    }

    private fun updateProgressBar() {
        Thread {
            val totalDuration = 1000 // Total duration in milliseconds
            val updateInterval = 40 // Update interval in milliseconds
            val totalUpdates = totalDuration / updateInterval // Total number of updates
            val increment = 100 / totalUpdates // Increment per update

            while (progressStatus < 100) {
                // Simulate some work being done by sleeping the thread for a short time
                try {
                    Thread.sleep(updateInterval.toLong()) // Sleep for the defined interval
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                // Increment progress status based on calculated increment
                progressStatus += increment.toInt()

                // Update the progress bar on the UI thread
                handler.post {
                    if (progressStatus > 100) {
                        progressStatus = 100 // Ensure it doesn't exceed 100%
                    }
                    progressBar.progress = progressStatus
                }
            }

            // After completing the progress, launch the next activity
            launchNextActivity()
        }.start()
    }

    private fun launchNextActivity() {
        handler.post {
            progressBar.visibility = View.GONE // Hide the ProgressBar when done

            if (isFirstTime()) {
                val intent = Intent(this@MainActivity, MainActivity2::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@MainActivity, beranda::class.java)
                startActivity(intent)
            }
            finish() // Close this activity
        }
    }

    private fun isFirstTime(): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)
        if (isFirstTime) {
            with(sharedPref.edit()) {
                putBoolean("isFirstTime", false)
                apply()
            }
        }
        return isFirstTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Clean up handler callbacks if needed
    }
}