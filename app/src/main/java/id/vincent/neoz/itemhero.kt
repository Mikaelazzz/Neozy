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

// Kelas Item, berfungsi untuk mendefinisikan item yang akan ditampilkan pada RecyclerView
data class Item(
    @SerializedName("namaItem") val namaItem: String,
    @SerializedName("introItem") val introItem: String,
    @SerializedName("imageItem") val imageItem: String,
    @SerializedName("item1") val item1: String,
    @SerializedName("item2") val item2: String,
    @SerializedName("item3") val item3: String,
    @SerializedName("item4") val item4: String,
    @SerializedName("item5") val item5: String,
    @SerializedName("item6") val item6: String,
    @SerializedName("deskripsiItem") val deskripsiItem: String
)

class itemhero : Fragment() {

    // Mengambil data dari ItemViewModel
    private val itemViewModel: ItemViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[ItemViewModel::class.java]
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.itemhero, container, false)
        setupRecyclerView(view)

        itemViewModel.itemlist.observe(viewLifecycleOwner) { items ->
            (view.findViewById<RecyclerView>(R.id.list_item).adapter as ItemAdapter).updateData(items)
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_item)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ItemAdapter(emptyList()) // Set empty list as initial data
        recyclerView.adapter = adapter
    }

    // Adapter untuk RecyclerView
    class ItemAdapter(private var items: List<Item>) :
        RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

        // ViewHolder untuk mengikat data ke tampilan
        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.titleItem)
            val intro: TextView = itemView.findViewById(R.id.subtitleItem)
            val i1 : TextView = itemView.findViewById(R.id.item1)
            val i2 : TextView = itemView.findViewById(R.id.item2)
            val i3 : TextView = itemView.findViewById(R.id.item3)
            val i4 : TextView = itemView.findViewById(R.id.item4)
            val i5 : TextView = itemView.findViewById(R.id.item5)
            val i6 : TextView = itemView.findViewById(R.id.item6)
            val image: ImageView = itemView.findViewById(R.id.imagesItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]
            holder.name.text = item.namaItem
            holder.intro.text = item.introItem
            holder.i1.text = item.item1
            holder.i2.text = item.item2
            holder.i3.text = item.item3
            holder.i4.text = item.item4
            holder.i5.text = item.item5
            holder.i6.text = item.item6

            // Menampilkan gambar berdasarkan nama yang ada di imageItem
            val imageResId = holder.itemView.context.resources.getIdentifier(
                item.imageItem, "drawable", holder.itemView.context.packageName
            )
            holder.image.setImageResource(imageResId)
        }

        override fun getItemCount(): Int = items.size

        // Fungsi untuk memperbarui data dalam adapter
        fun updateData(newItems: List<Item>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}
