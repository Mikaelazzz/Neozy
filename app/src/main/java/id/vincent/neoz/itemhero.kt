package id.vincent.neoz

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.AnimatorSetCompat.playTogether
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
    @SerializedName("deskripsiItem") val deskripsiItem: String,
    @SerializedName("role") val role: String
)
class itemhero : Fragment() {

    private val itemViewModel: ItemViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ItemViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.itemhero, container, false)
        setupRecyclerView(view)
        setupRoleButtons(view)

        itemViewModel.filteredItems.observe(viewLifecycleOwner) { items ->
            (view.findViewById<RecyclerView>(R.id.list_item).adapter as PaginatedItemAdapter).setInitialData(items)
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_item)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PaginatedItemAdapter(progressBar)
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!adapter.isCurrentlyLoading() && lastVisibleItem + 1 >= totalItemCount) {
                    adapter.loadNextPage()
                }
            }
        })

    }


    private fun setupRoleButtons(view: View) {
        val roleButtons = listOf("all", "physical", "magic", "tank", "movement", "roam", "jungler")
        val recyclerButtons: RecyclerView = view.findViewById(R.id.recycler_buttons)

        recyclerButtons.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val buttonAdapter = RoleButtonAdapter(roleButtons) { role ->
            itemViewModel.filterItemsByRole(role)
        }
        recyclerButtons.adapter = buttonAdapter
    }

    companion object {
        private const val PAGE_SIZE = 5
        private const val ANIMATION_DURATION = 150L
        private const val LOAD_DELAY = 1000L // Delay antar item
        private const val AUTO_LOAD_DELAY = 2000L // Delay sebelum memuat item selanjutnya
    }

    class PaginatedItemAdapter(private val progressBar: ProgressBar) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var originalItems: List<Item> = listOf()
        private val displayedItems: MutableList<Item> = mutableListOf()
        private var loadedItemCount = 0
        private var hasMoreData = true
        private val handler = Handler(Looper.getMainLooper())
        private var isLoading = false

        fun isCurrentlyLoading() = isLoading

        fun setInitialData(items: List<Item>) {
            originalItems = items
            displayedItems.clear()
            loadedItemCount = 0
            hasMoreData = true
            isLoading = false

            loadInitialPage()
            scheduleAutoLoad()
        }

        private fun loadInitialPage() {
            val initialPageSize = minOf(PAGE_SIZE, originalItems.size)
            for (i in 0 until initialPageSize) {
                displayedItems.add(originalItems[i])
            }
            loadedItemCount = initialPageSize
            hasMoreData = loadedItemCount < originalItems.size
            notifyDataSetChanged()
        }

        private fun scheduleAutoLoad() {
            if (canLoadMore() && !isLoading) {
                handler.postDelayed({
                    loadNextPage()
                }, AUTO_LOAD_DELAY)
            }
        }

        fun canLoadMore() = hasMoreData && loadedItemCount < originalItems.size

        fun loadNextPage() {
            if (!canLoadMore() || isLoading) return

            isLoading = true
            progressBar.visibility = View.VISIBLE // Tampilkan ProgressBar
            val remainingItems = originalItems.size - loadedItemCount
            val itemsToLoad = minOf(PAGE_SIZE, remainingItems)

            for (i in 0 until itemsToLoad) {
                handler.postDelayed({
                    if (loadedItemCount < originalItems.size) {
                        displayedItems.add(originalItems[loadedItemCount])
                        notifyItemInserted(displayedItems.size - 1)
                        loadedItemCount++

                        if (i == itemsToLoad - 1) {
                            isLoading = false
                            progressBar.visibility = View.GONE
                            scheduleAutoLoad()
                        }
                    }
                }, i * LOAD_DELAY)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder as ItemViewHolder
            val item = displayedItems[position]

            itemHolder.name.text = item.namaItem
            itemHolder.intro.text = item.introItem
            itemHolder.i1.text = item.item1
            itemHolder.i2.text = item.item2
            itemHolder.i3.text = item.item3
            itemHolder.i4.text = item.item4
            itemHolder.i5.text = item.item5
            itemHolder.i6.text = item.item6
            itemHolder.descItem.text = item.deskripsiItem

            // Set visibility based on data availability
            itemHolder.i1.visibility = if (item.item1.isNotEmpty()) View.VISIBLE else View.GONE
            itemHolder.i2.visibility = if (item.item2.isNotEmpty()) View.VISIBLE else View.GONE
            itemHolder.i3.visibility = if (item.item3.isNotEmpty()) View.VISIBLE else View.GONE
            itemHolder.i4.visibility = if (item.item4.isNotEmpty()) View.VISIBLE else View.GONE
            itemHolder.i5.visibility = if (item.item5.isNotEmpty()) View.VISIBLE else View.GONE
            itemHolder.i6.visibility = if (item.item6.isNotEmpty()) View.VISIBLE else View.GONE


            val imageResId = holder.itemView.context.resources.getIdentifier(
                item.imageItem, "drawable", holder.itemView.context.packageName
            )
            itemHolder.image.setImageResource(imageResId)

            animateItemEntry(itemHolder.itemView, position)
        }

        private fun animateItemEntry(itemView: View, position: Int) {
            val translationY = ObjectAnimator.ofFloat(itemView, "translationY", 100f, 0f)
            AnimatorSet().apply {
                playTogether(translationY)
                duration = ANIMATION_DURATION + (position * 50L)
                start()
            }
        }

        override fun getItemCount(): Int = displayedItems.size

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.titleItem)
            val intro: TextView = itemView.findViewById(R.id.subtitleItem)
            val i1: TextView = itemView.findViewById(R.id.item1)
            val i2: TextView = itemView.findViewById(R.id.item2)
            val i3: TextView = itemView.findViewById(R.id.item3)
            val i4: TextView = itemView.findViewById(R.id.item4)
            val i5: TextView = itemView.findViewById(R.id.item5)
            val i6: TextView = itemView.findViewById(R.id.item6)
            val descItem: TextView = itemView.findViewById(R.id.descItem)
            val image: ImageView = itemView.findViewById(R.id.imagesItem)
        }
    }

    class RoleButtonAdapter(
        private val roles: List<String>,
        private val onRoleSelected: (String) -> Unit
    ) : RecyclerView.Adapter<RoleButtonAdapter.RoleViewHolder>() {

        inner class RoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val button: Button = itemView.findViewById(R.id.button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
            return RoleViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
            val role = roles[position]
            holder.button.text = role.capitalize()
            holder.button.setOnClickListener { onRoleSelected(role) }
        }

        override fun getItemCount(): Int = roles.size
    }
}

