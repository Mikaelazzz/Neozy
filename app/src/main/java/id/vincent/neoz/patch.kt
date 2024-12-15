package id.vincent.neoz

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName





class patch : Fragment() {


    data class UPatch(
        @SerializedName("patch") val patch: String,
        @SerializedName("textpatch") val textpatch: String,
        @SerializedName("titleHero") val titleHero: String,
        @SerializedName("imageHero") val imageHero: String,
        @SerializedName("descHero") val descHero: String,
        @SerializedName("update") val update: String,
        @SerializedName("type") val type: String,
        @SerializedName("atribut") val atribute: String,
        @SerializedName("pasif") val passive: String,
        @SerializedName("skill1") val skill1: String,
        @SerializedName("skill2") val skill2: String,
        @SerializedName("skill3") val skill3: String,
        @SerializedName("skill4") val skill4: String,
        @SerializedName("ultimate") val ultimate: String
    ) : Parcelable {
        // Implementasi manual jika diperlukan
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(patch)
            parcel.writeString(textpatch)
            parcel.writeString(titleHero)
            parcel.writeString(imageHero)
            parcel.writeString(descHero)
            parcel.writeString(update)
            parcel.writeString(type)
            parcel.writeString(atribute)
            parcel.writeString(passive)
            parcel.writeString(skill1)
            parcel.writeString(skill2)
            parcel.writeString(skill3)
            parcel.writeString(skill4)
            parcel.writeString(ultimate)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UPatch> {
            override fun createFromParcel(parcel: Parcel): UPatch {
                return UPatch(parcel)
            }

            override fun newArray(size: Int): Array<UPatch?> {
                return arrayOfNulls(size)
            }
        }
    }

    private val patchViewModel: PatchViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[PatchViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.patch, container, false)
        setupRecyclerView(view)

        patchViewModel.patchlist.observe(viewLifecycleOwner) { patches ->
            (view.findViewById<RecyclerView>(R.id.list_patch).adapter as PatchAdapter).updateData(patches)
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_patch)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PatchAdapter(emptyList()) { selectedPatch ->
            // Navigasi ke UpdatePatchActivity
            val intent = Intent(requireContext(), updatepatch::class.java).apply {
                putExtra("PATCH_DATA", selectedPatch)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    // Adapter untuk RecyclerView
    class PatchAdapter(private var patches: List<UPatch>, private val onItemClick: (UPatch) -> Unit) :
        RecyclerView.Adapter<PatchAdapter.PatchViewHolder>() {

        // ViewHolder untuk mengikat data
        class PatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nama: TextView = itemView.findViewById(R.id.titleHeroes)
            val intro: TextView = itemView.findViewById(R.id.descHeroes)
            val image: ImageView = itemView.findViewById(R.id.imageHeroes)
            val pimg: LinearLayout = itemView.findViewById(R.id.patched)
            val ptext: TextView = itemView.findViewById(R.id.textpatched)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_patch, parent, false) // Pastikan layout patch.xml ada
            return PatchViewHolder(view) // Tidak perlu mendeklarasikan viewHolder terlebih dahulu
        }


        override fun onBindViewHolder(holder: PatchViewHolder, position: Int) {
            val patch = patches[position]
            holder.nama.text = patch.titleHero
            holder.intro.text = patch.descHero
            holder.ptext.text = patch.textpatch

            // Mengambil gambar berdasarkan imageHero
            val imageResId = holder.itemView.context.resources.getIdentifier(
                patch.imageHero, "drawable", holder.itemView.context.packageName
            )
            if (imageResId != 0) {
                holder.image.setImageResource(imageResId) // Menggunakan setImageResource untuk ImageView
            } else {
                holder.image.setImageResource(R.drawable.hero) // Gambar default jika tidak ditemukan
            }

            // Menampilkan background untuk patch berdasarkan nama di patch
        val patchResId = holder.itemView.context.resources.getIdentifier(
            patch.patch, "drawable", holder.itemView.context.packageName
        )
        holder.pimg.setBackgroundResource(patchResId)
            holder.itemView.setOnClickListener {
                onItemClick(patch)
            }


    }

    override fun getItemCount(): Int = patches.size

        // Memperbarui data di adapter
        fun updateData(newPatches: List<UPatch>) {
            patches = newPatches
            notifyDataSetChanged()
        }
    }
}
