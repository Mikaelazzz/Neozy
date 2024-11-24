package id.vincent.neoz

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class zodiac : Fragment() {
    private lateinit var seekBar: SeekBar
    private lateinit var starPointsText: TextView
    private lateinit var diamondText: TextView
    private lateinit var resultText: TextView

    // Updated constants
    private val INITIAL_DIAMONDS = 1700
    private val MAX_STARS =
        100  // Set maximum stars to 100 for dynamic DIAMONDS_PER_STAR calculation

    // Calculate DIAMONDS_PER_STAR dynamically based on INITIAL_DIAMONDS and MAX_STARS
    private val diamondsPerStar: Int
        get() = INITIAL_DIAMONDS / MAX_STARS

    private val handler = android.os.Handler(Looper.getMainLooper())
    private val hideResultRunnable = Runnable {
        // Reset progress SeekBar ke 0
        seekBar.progress = 0
        // Sembunyikan resultText
        resultText.visibility = View.GONE
        // Update perhitungan untuk progress 0
        updateCalculations(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.zodiac, container, false)

        // Inisialisasi tampilan
        seekBar = view.findViewById(R.id.magicWheelSeekBar)
        starPointsText = view.findViewById(R.id.starPointsValue)
        diamondText = view.findViewById(R.id.diamondValue)
        resultText = view.findViewById(R.id.result_text)

        // Sembunyikan resultText pada awalnya
        resultText.visibility = View.GONE

        // Konfigurasi SeekBar
        seekBar.max = MAX_STARS
        seekBar.progress = 0

        // Set up SeekBar listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateCalculations(progress)
                resultText.visibility = View.VISIBLE  // Tampilkan resultText ketika drag SeekBar

                // Hapus dan reset callback hideResultRunnable setiap kali SeekBar diubah
                handler.removeCallbacks(hideResultRunnable)
                handler.postDelayed(hideResultRunnable, 30000)  // Sembunyikan setelah 30 detik
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Hapus dan reset callback sebelum interaksi dimulai
                handler.removeCallbacks(hideResultRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Hapus dan reset callback setelah interaksi selesai
                handler.removeCallbacks(hideResultRunnable)
                handler.postDelayed(
                    hideResultRunnable,
                    30000
                )  // Sembunyikan setelah 30 detik tidak ada aktivitas
            }
        })

        // Inisialisasi perhitungan
        updateCalculations(0)

        // Set up tampilan edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        return view
    }

    private fun updateCalculations(stars: Int) {
        // Hitung sisa diamond secara dinamis
        val remainingDiamonds = INITIAL_DIAMONDS - (stars * diamondsPerStar)
        val diamondsUsed = stars * diamondsPerStar  // Diamonds digunakan untuk menggambar bintang

        // Perbarui elemen UI
        starPointsText.text = stars.toString()

        // Hanya tampilkan diamond yang tersisa jika ada
        if (remainingDiamonds >= 0) {
            diamondText.text = remainingDiamonds.toString()

            // Perbarui teks hasil
            resultText.text = String.format(
                "Diamond yang digunakan %d",
                diamondsUsed
            )
        } else {
            // Jika melebihi diamond yang ada, batasi dengan 0
            diamondText.text = "0"
            resultText.text = String.format(
                "Diamond yang digunakan %d",
                diamondsUsed
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus handler callbacks ketika tampilan dihancurkan untuk mencegah kebocoran memori
        handler.removeCallbacks(hideResultRunnable)
    }
}