package id.vincent.neoz

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class magicwheel : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var starPointsText: TextView
    private lateinit var diamondText: TextView
    private lateinit var resultText: TextView

    // Updated constants
    private val INITIAL_DIAMONDS = 10800
    private val DIAMONDS_PER_STAR = 54
    private val MAX_STARS = 200  // 10800/54 = 200 maximum possible stars

    // Handler untuk auto-hiding resultText setelah 30 detik
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val hideResultRunnable = Runnable {
        // Sembunyikan resultText
        resultText.visibility = View.GONE
        seekBar.progress = 0  // Reset progress SeekBar ke awal
        updateCalculations(0)  // Reset perhitungan ke awal
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.magicwheel, container, false)

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

        // Atur listener untuk SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    // Jika progress = 0, sembunyikan resultText
                    resultText.visibility = View.GONE
                } else {
                    // Jika progress > 0, tampilkan resultText
                    resultText.visibility = View.VISIBLE
                }

                // Perbarui perhitungan berdasarkan progress
                updateCalculations(progress)

                // Hapus callback hideRunnable sebelumnya dan atur ulang
                handler.removeCallbacks(hideResultRunnable)
                handler.postDelayed(hideResultRunnable, 30000)  // Sembunyikan setelah 30 detik
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Hapus callback sebelumnya saat pengguna mulai berinteraksi
                handler.removeCallbacks(hideResultRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Atur ulang hideRunnable saat interaksi selesai
                handler.removeCallbacks(hideResultRunnable)
                handler.postDelayed(hideResultRunnable, 30000)  // Sembunyikan setelah 30 detik
            }
        })

        // Inisialisasi perhitungan awal
        updateCalculations(0)

        // Atur edge-to-edge display
        applyWindowInsets(view)

        // Setup toolbar dan back press handling
        setupToolbar(view)

        return view
    }

    private fun applyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateCalculations(stars: Int) {
        // Hitung diamond yang tersisa dan yang digunakan
        val remainingDiamonds = INITIAL_DIAMONDS - (stars * DIAMONDS_PER_STAR)
        val diamondsUsed = stars * DIAMONDS_PER_STAR

        // Perbarui elemen UI
        starPointsText.text = stars.toString()

        if (remainingDiamonds >= 0) {
            diamondText.text = remainingDiamonds.toString()

            // Tampilkan jumlah diamond yang digunakan
            resultText.text = String.format(
                "Diamond yang digunakan %d",
                diamondsUsed
            )
        } else {
            diamondText.text = "0"
            resultText.text = String.format(
                "Diamond yang digunakan %d",
                diamondsUsed
            )
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.font))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus callback handler untuk mencegah kebocoran memori
        handler.removeCallbacks(hideResultRunnable)
    }
}
