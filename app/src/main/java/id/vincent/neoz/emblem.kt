package id.vincent.neoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName


class emblem : Fragment() {
    private val emblemViewModel: EmblemViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[EmblemViewModel::class.java]
    }

    data class Emblem(
        @SerializedName ("titleE")val titleE: String,
        @SerializedName ("imageE")val imageE: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.emblem, container, false)
        setupRecyclerView(view)

        emblemViewModel.emblemlist.observe(viewLifecycleOwner) { emblems: List<Emblem> ->
            (view.findViewById<RecyclerView>(R.id.list_emblem).adapter as EmblemAdapter).updateData(emblems)
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_emblem)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = EmblemAdapter(emptyList()) // Empty adapter at first
        recyclerView.adapter = adapter

        // Observe data from ViewModel
        emblemViewModel.emblemlist.observe(viewLifecycleOwner) { emblems: List<Emblem> ->
            adapter.updateData(emblems)
        }
    }

    class EmblemAdapter(private var emblems: List<Emblem>) :
        RecyclerView.Adapter<EmblemAdapter.EmblemViewHolder>() {

        class EmblemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.titleEmblem)
            val image: ImageView = itemView.findViewById(R.id.imageEmblem)
            // Remove role TextView since it won't be displayed
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmblemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_emblem, parent, false)
            return EmblemViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmblemViewHolder, position: Int) {
            val emblem = emblems[position]
            holder.name.text = emblem.titleE // Only display titleE
            holder.image.setImageResource(
                holder.itemView.context.resources.getIdentifier(emblem.imageE, "drawable", holder.itemView.context.packageName)
            )
        }

        override fun getItemCount(): Int = emblems.size

        fun updateData(newEmblems: List<Emblem>) {
            emblems = newEmblems
            notifyDataSetChanged()
        }
    }
}