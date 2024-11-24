package id.vincent.neoz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*


class total : Fragment() {
    private var hideResultJob: Job? = null // Coroutine untuk menyembunyikan teks


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.total, container, false)

        setupToolbar(view)
        applyWindowInsets(view)

        // Inisialisasi elemen UI
        val totalMatchInput = view.findViewById<EditText>(R.id.totalmatch)
        val winRateInput = view.findViewById<EditText>(R.id.totalwr)
        val resultText = view.findViewById<TextView>(R.id.result_text)
        val hitungButton = view.findViewById<Button>(R.id.hitung)

        // Sembunyikan resultText di awal
        resultText.visibility = View.GONE

        // Tambahkan listener untuk perubahan teks
        addEditTextListeners(listOf(totalMatchInput, winRateInput))

        // Fungsi ketika tombol "Hitung" ditekan
        hitungButton.setOnClickListener {
            val totalMatchStr = totalMatchInput.text.toString()
            val winRateStr = winRateInput.text.toString()

            if (totalMatchStr.isEmpty() || winRateStr.isEmpty()) {
                tampilkanPesanKesalahan(resultText, "Masukkan nilai yang valid")
                return@setOnClickListener
            }

            val totalMatch = totalMatchStr.toDoubleOrNull()
            val winRate = winRateStr.replace(",", ".").toDoubleOrNull()

            if (totalMatch == null || winRate == null || winRate > 100) {
                tampilkanPesanKesalahan(resultText, "Impossible broo")
                return@setOnClickListener
            }

            val totalWin = (totalMatch * winRate / 100).toInt()
            val totalLose = totalMatch.toInt() - totalWin

            resultText.text = "Total win: $totalWin match\nTotal lose: $totalLose match"
            resultText.setTextColor(resources.getColor(R.color.font))
            resultText.typeface = resources.getFont(R.font.poppinsbold)
            resultText.visibility = View.VISIBLE

            scheduleHideResult(resultText) // Jadwalkan penghapusan teks
        }

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

    private fun addEditTextListeners(editTexts: List<EditText>) {
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Ubah background jika EditText tidak kosong
                    val drawableRes = if (!s.isNullOrEmpty()) {
                        R.drawable.editextactive
                    } else {
                        R.drawable.editext
                    }
                    editText.background = ContextCompat.getDrawable(requireContext(), drawableRes)
                }
            })
        }
    }

    private fun scheduleHideResult(resultText: TextView) {
        // Batalkan tugas sebelumnya jika ada
        hideResultJob?.cancel()

        // Jadwalkan coroutine untuk menyembunyikan teks
        hideResultJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000) // Tunggu 30 detik
            resultText.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun tampilkanPesanKesalahan(resultText: TextView, pesan: String) {
        resultText.text = pesan
        resultText.setTextColor(resources.getColor(R.color.font))
        resultText.typeface = resources.getFont(R.font.poppinsbold)
        resultText.visibility = View.VISIBLE

        scheduleHideResult(resultText) // Jadwalkan penghapusan teks meski terdapat kesalahan
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(resources.getColor(R.color.font))
    }

    private fun applyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideResultJob?.cancel() // Hentikan tugas ketika fragment dihancurkan
    }
}
