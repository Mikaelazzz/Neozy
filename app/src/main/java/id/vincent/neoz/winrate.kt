package id.vincent.neoz

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import android.text.Editable
import android.text.TextWatcher
import kotlin.math.abs
import kotlin.math.ceil
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout

class winrate : Fragment() {

    private lateinit var totalmatch: EditText
    private lateinit var totalwr: EditText
    private lateinit var targetwr: EditText
    private lateinit var jumlah: Button
    private lateinit var resultText: TextView

    private var hideTextJob: Job? = null // Coroutine untuk menyembunyikan teks


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.winrate, container, false)

        // Inisialisasi view
        totalmatch = view.findViewById(R.id.totalmatch)
        totalwr = view.findViewById(R.id.totalwr)
        targetwr = view.findViewById(R.id.target)
        jumlah = view.findViewById(R.id.hitung)
        resultText = view.findViewById(R.id.result_text)

        // Sembunyikan teks hasil di awal
        resultText.visibility = View.GONE

        // Tambahkan listener untuk EditText
        addEditTextListeners()

        // Setup tombol kalkulasi
        setupCalculationButton()

        val mainLayout = view.findViewById<ConstraintLayout>(R.id.main)
        mainLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                v.requestFocus() // Meminta fokus ke layout utama
                true
            } else {
                false
            }
        }

        // Tambahkan listener untuk menyembunyikan keyboard
        val scrollView = view.findViewById<ScrollView>(R.id.scrollview) // Tambahkan id ke ScrollView di layout
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Cek apakah touch event terjadi di luar EditText
                val currentFocus = requireActivity().currentFocus
                if (currentFocus is EditText) {
                    hideKeyboard()
                    currentFocus.clearFocus()
                }
            }
            false
        }

        setupUI(view)
        return view
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            view.clearFocus()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch listener for non-EditText views to hide keyboard
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard()
                    v.requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        // If a layout container, iterate over children and seed recursion
//        if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                val innerView = view.getChildAt(i)
//                setupUI(innerView)
//            }
//        }
    }

    private fun addEditTextListeners() {
        val editTexts = listOf(totalmatch, totalwr, targetwr)

        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Ubah warna EditText jika tidak kosong
                    if (!s.isNullOrEmpty()) {
                        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.editextactive)
                    } else {
                        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.editext)
                    }
                }
            })
        }
    }

    private fun setupCalculationButton() {
        jumlah.setOnClickListener {
            calculateRequiredMatches()
            scheduleHideResult() // Jadwalkan penghapusan teks setelah tombol ditekan
        }
    }

    private fun calculateRequiredMatches() {
        try {
            val totalMatches = totalmatch.text.toString().toDouble()
            val currentWR = totalwr.text.toString().toDouble()
            val targetWR = targetwr.text.toString().toDouble()

            if (totalMatches == null || currentWR == null || targetWR == null) {
                showError("Mohon masukkan angka yang valid")
                return
            }

            if (currentWR > 100.0 || targetWR > 100.0) {
                showError("Win Rate tidak bisa lebih dari 100%")
                return
            }

            if (currentWR < 0 || targetWR < 0){
                showError("Field tidak boleh lebih kecil dari 0")
                return
            }

//            if (targetWR == 100.0 && currentWR == 100.0) {
//                // Jika target WR adalah 100 dan current WR juga 100
//                showResult("Membutuhkan 0 Menang Tanpa Kalah untuk mendapatkan ${"%.0f".format(targetWR)}% Win Rate")
//                return
//            }

            if (targetWR == 100.0 && currentWR < 100.0) {
                showError("Yo ndak bisa bree\nYang bisa cuma Moonton")
                return
            }

//            if (targetWR <= currentWR) {
//                showError("Silakan beralih ke Kalkulator Lose Streak untuk menghitung penurunan Win Rate")
//                return
//            }

            val currentWins = (totalMatches * currentWR) / 100
//            val requiredWins =
//                abs(ceil((targetWR * totalMatches - 100 * currentWins) / (targetWR - 100)).toInt())
//            showResult("Membutuhkan $requiredWins menang Tanpa Kalah untuk mendapatkan ${"%.0f".format(targetWR)}% Win Rate")

            if (targetWR > currentWR) {
                // Perhitungan untuk menaikkan WR
                val requiredWins =
                    abs(ceil((targetWR * totalMatches - 100 * currentWins) / (targetWR - 100)).toInt())
                showResult("Membutuhkan $requiredWins Menang Tanpa Kalah untuk mendapatkan ${"%.0f".format(targetWR)}% Win Rate")
            } else if ( targetWR == 100.0 && currentWR == 100.0 ){
                showResult("Membutuhkan 0 Menang Tanpa Kalah untuk mendapatkan ${"%.0f".format(targetWR)}% Win Rate")
            }
            else {
                // Perhitungan untuk menurunkan WR
                val requiredLosses =
                    abs(ceil((currentWins - totalMatches * targetWR / 100) / (targetWR / 100 - 1)).toInt())
                showResult("Membutuhkan $requiredLosses Kalah Tanpa Menang untuk mendapatkan ${"%.0f".format(targetWR)}% Win Rate")
            }

        } catch (e: NumberFormatException) {
            showError("Mohon masukkan angka yang valid")
        } catch (e: Exception) {
            showError("Terjadi kesalahan dalam perhitungan")
        }
    }

    private fun showError(message: String) {
        resultText.visibility = View.VISIBLE
        resultText.text = message
        resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.font))
    }

    private fun showResult(message: String) {
        resultText.visibility = View.VISIBLE
        resultText.text = message
        resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.font))
    }

    private fun scheduleHideResult() {
        // Batalkan job sebelumnya jika ada
        hideTextJob?.cancel()

        // Jadwalkan coroutine untuk menyembunyikan teks
        hideTextJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000) // Tunggu 30 detik
            resultText.visibility = View.GONE // Sembunyikan teks
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideTextJob?.cancel() // Batalkan job saat Fragment dihancurkan
    }
}
