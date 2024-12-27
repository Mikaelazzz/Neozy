package id.vincent.neoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName

class emblem : Fragment() {
    private lateinit var emblemViewModel: EmblemViewModel

    data class Emblem(
        @SerializedName("titleE") val titleE: String,
        @SerializedName("imageE") val imageE: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.emblem, container, false)

        // Inisialisasi ViewModel
        emblemViewModel = ViewModelProvider(this)[EmblemViewModel::class.java]

        // Setup RecyclerView
        setupRecyclerView(view)

        // Observer untuk loading state
        emblemViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tampilkan/sembunyikan loading indicator jika diperlukan
        }

        // Observer untuk error
        emblemViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Observer untuk daftar emblem
        emblemViewModel.emblemlist.observe(viewLifecycleOwner) { emblems ->
            (view.findViewById<RecyclerView>(R.id.list_emblem).adapter as EmblemAdapter).updateData(emblems)
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_emblem)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = EmblemAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    class EmblemAdapter(private var emblems: List<Emblem>) :
        RecyclerView.Adapter<EmblemAdapter.EmblemViewHolder>() {

        class EmblemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.titleEmblem)
            val image: ImageView = itemView.findViewById(R.id.imageEmblem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmblemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_emblem, parent, false)
            return EmblemViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmblemViewHolder, position: Int) {
            val emblem = emblems[position]
            holder.name.text = emblem.titleE

            // URL dasar untuk gambar emblem
            val emblemImageBaseUrl = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/img/emblem/head/"

            // Memuat gambar dari GitHub menggunakan Glide
            Glide.with(holder.itemView.context)
                .load("$emblemImageBaseUrl${emblem.imageE}.png")
                .placeholder(R.drawable.hero) // Gambar placeholder
                .error(R.drawable.hero) // Gambar error
                .into(holder.image)
        }

        override fun getItemCount(): Int = emblems.size

        fun updateData(newEmblems: List<Emblem>) {
            emblems = newEmblems
            notifyDataSetChanged()
        }
    }
}