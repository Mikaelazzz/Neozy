package id.vincent.neoz

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout

class lose : Fragment() {

    private lateinit var totalmatch: EditText
    private lateinit var totalwr: EditText
    private lateinit var targetwr: EditText
    private lateinit var jumlah: Button
    private lateinit var resultText: TextView

    private var hideTextJob: Job? = null // Coroutine for hiding text


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.lose, container, false)

        // Initialize view elements
        totalmatch = view.findViewById(R.id.totalmatch)
        totalwr = view.findViewById(R.id.totalwr)
        targetwr = view.findViewById(R.id.target)
        jumlah = view.findViewById(R.id.hitung)
        resultText = view.findViewById(R.id.result_text)

        // Hide result text initially
        resultText.visibility = View.GONE

        // Add listeners for EditText changes
        addEditTextListeners()

        // Setup calculation button
        setupCalculationButton()

        // Tambahkan listener untuk menyembunyikan keyboard
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
        jumlah.requestFocus()
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
                    // Change background color of EditText if it's not empty
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
            scheduleHideResult() // Schedule hiding the result text after button press
        }
    }

    private fun calculateRequiredMatches() {
        try {
            val totalMatches = totalmatch.text.toString().toDouble()
            val currentWR = totalwr.text.toString().toDouble()
            val loseStreak = targetwr.text.toString().toDouble()

            if (totalMatches == null || currentWR == null || loseStreak == null) {
                showError("Mohon masukkan angka yang valid")
                return
            }

//            if (currentWR > 100 || loseStreak < 0) {
//                showError("Win Rate tidak bisa lebih dari 100% dan lose streak tidak bisa kurang dari 0")
//                return
//            }

            if (loseStreak < 0 || currentWR < 0) {
                showError("Field tidak boleh lebih kecil dari 0")
                return
            }


            val currentWins = (totalMatches * currentWR) / 100
            val newTotalMatches = totalMatches + loseStreak

            // Calculate new Win Rate after lose streak
            val newWinRate = (currentWins / newTotalMatches) * 100

            showResult("Jika Anda losestreak sebanyak ${loseStreak.toInt()} kali, maka Win Rate Anda menjadi ${"%.1f".format(newWinRate)}%")
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
        // Cancel any previous job if exists
        hideTextJob?.cancel()

        // Schedule coroutine to hide result text
        hideTextJob = CoroutineScope(Dispatchers.Main).launch {
            delay(60000) // Wait for 60 seconds
            resultText.visibility = View.GONE // Hide the result text
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideTextJob?.cancel() // Cancel the job when the fragment is destroyed
    }

}
