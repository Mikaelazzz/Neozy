package id.vincent.neoz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class findergame : Fragment() {
    private lateinit var idInput: EditText
    private lateinit var zoneInput: EditText
    private lateinit var nicknameTextView: TextView
    private lateinit var searchButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.findergame, container, false)

        idInput = view.findViewById(R.id.idgame)
        zoneInput = view.findViewById(R.id.server)
        nicknameTextView = view.findViewById(R.id.result_text)
        searchButton = view.findViewById(R.id.search)

        // Sembunyikan result_text di awal
        nicknameTextView.visibility = View.GONE

        // Set zone input always enabled for ML
        zoneInput.isEnabled = true
        // Menambahkan listener untuk menutup keyboard saat sentuhan di luar EditText
        val mainLayout = view.findViewById<ConstraintLayout>(R.id.main)
        mainLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard() // Menyembunyikan keyboard
                v.requestFocus() // Meminta fokus ke layout utama
                true
            } else {
                false
            }
        }

        val scrollView = view.findViewById<ScrollView>(R.id.scrollview)
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = requireActivity().currentFocus
                if (currentFocus is EditText) {
                    hideKeyboard() // Menyembunyikan keyboard
                    currentFocus.clearFocus() // Menghapus fokus dari EditText
                }
            }
            false
        }





        // Menyiapkan touch listener untuk menutup keyboard saat area selain EditText disentuh
        setupUI(view)
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchButton.setOnClickListener {
            checkNickname()

        }

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

        val scrollView = view.findViewById<ScrollView>(R.id.scrollview)
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = requireActivity().currentFocus
                if (currentFocus is EditText) {
                    hideKeyboard() // Menyembunyikan keyboard
                    currentFocus.clearFocus() // Menghapus fokus dari EditText
                }
            }
            false
        }





        // Menyiapkan touch listener untuk menutup keyboard saat area selain EditText disentuh
        setupUI(view)
    }



    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            view.clearFocus() // Menghapus fokus
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        if (view !is EditText) { // Hanya untuk view yang bukan EditText
            view.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard() // Menyembunyikan keyboard
                    v.requestFocus() // Meminta fokus pada view
                    true
                } else {
                    false
                }
            }
        }

//        if (view is ViewGroup) { // Jika view adalah container (ViewGroup), lakukan rekursi
//            for (i in 0 until view.childCount) {
//                val childView = view.getChildAt(i)
//                setupUI(childView) // Memanggil setupUI untuk anak-anak view
//            }
//        }
    }


    private fun checkNickname() {
        // Menyembunyikan text view hasil dan progress bar hingga proses selesai
        nicknameTextView.visibility = View.GONE
        val progressBar = view?.findViewById<ProgressBar>(R.id.progress_bar)
        val resultText = view?.findViewById<TextView>(R.id.result_text)

        // Tampilkan progress bar dan pesan "Mencari data..."
        progressBar?.visibility = View.VISIBLE
        resultText?.visibility = View.VISIBLE
        resultText?.text = "Mencari data..." // Tampilkan pesan pencarian

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulasikan delay untuk menunggu respons
                Thread.sleep(3000) // Simulasi penundaan 3 detik

                val id = idInput.text.toString().trim()
                val zone = zoneInput.text.toString().trim()
                val url = URL("https://api.isan.eu.org/nickname/ml?id=${URLEncoder.encode(id, "UTF-8")}&server=${URLEncoder.encode(zone, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    // Cek apakah respons berhasil
                    if (responseCode == 200) {
                        val jsonObject = JSONObject(responseBody)
                        val nickname = jsonObject.optString("name", "Nickname not found")
                        resultText?.text = nickname
                    } else {
                        // Jika tidak berhasil
                        resultText?.text = "Nickname not found"
                    }

                    // Sembunyikan progress bar dan tampilkan hasil setelah mendapatkan nickname
                    progressBar?.visibility = View.GONE
                    nicknameTextView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Menangani error jika terjadi masalah selama request
                    resultText?.text = "Nickname not Found"
                    progressBar?.visibility = View.GONE
                    nicknameTextView.visibility = View.VISIBLE
                }
            }
        }
    }

}
