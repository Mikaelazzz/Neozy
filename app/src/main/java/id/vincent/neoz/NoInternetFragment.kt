package id.vincent.neoz

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi

class NoInternetFragment : Fragment(R.layout.internet) {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retryButton: Button = view.findViewById(R.id.button_signal)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)

        retryButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            retryButton.isEnabled = false

            // Periksa koneksi internet selama 2 detik
            Handler(Looper.getMainLooper()).postDelayed({
                if (NetworkUtil.isConnected(requireContext())) {
//                    Toast.makeText(requireContext(), "Internet kembali terhubung!", Toast.LENGTH_SHORT).show()

                    // Kembalikan ke fragment sebelumnya
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Periksa koneksi internet anda", Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
                retryButton.isEnabled = true
            }, 1500)
        }
    }
}
