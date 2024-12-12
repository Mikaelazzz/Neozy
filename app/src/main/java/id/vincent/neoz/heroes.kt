package id.vincent.neoz

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
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
import com.google.gson.annotations.SerializedName


class heroes : Fragment() {
    // Data class Hero
    data class Hero(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String,
        @SerializedName("imageRes") val imageRes: String,
        @SerializedName("role") val role: String,
        @SerializedName("bp") val bp: String,
        @SerializedName("dm") val dm: String,
        @SerializedName("tiket") val tiket: String,
        @SerializedName("rolee") val rolee: String,
        @SerializedName("type") val type: String,
        @SerializedName("story") val story: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(description)
            parcel.writeString(imageRes)
            parcel.writeString(role)
            parcel.writeString(bp)
            parcel.writeString(dm)
            parcel.writeString(tiket)
            parcel.writeString(rolee)
            parcel.writeString(type)
            parcel.writeString(story)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Hero> {
            override fun createFromParcel(parcel: Parcel): Hero {
                return Hero(parcel)
            }

            override fun newArray(size: Int): Array<Hero?> {
                return arrayOfNulls(size)
            }
        }
    }


    private val heroViewModel: HeroViewModel by lazy {
        ViewModelProvider(requireActivity())[HeroViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.heroes, container, false)
        setupRecyclerViews(view)
        return view
    }

    private fun setupRecyclerViews(view: View) {
        val sortedHeroes = heroViewModel.heroesList.sortedBy { it.name }

        // RecyclerView untuk tombol filter
        val recyclerButtons = view.findViewById<RecyclerView>(R.id.recycler_buttons)
        recyclerButtons.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val buttons = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")

        // RecyclerView untuk daftar hero
        val heroRecyclerView = view.findViewById<RecyclerView>(R.id.list_hero)
        val paginatedHeroAdapter = PaginatedHeroAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        heroRecyclerView.layoutManager = linearLayoutManager
        heroRecyclerView.adapter = paginatedHeroAdapter

        // Initial load with first 10 heroes
        paginatedHeroAdapter.setInitialData(sortedHeroes)
        paginatedHeroAdapter.setOnItemClickListener(object : PaginatedHeroAdapter.OnItemClickListener {
            override fun onItemClick(hero: Hero) {
                val intent = Intent(requireContext(), tampilanhero::class.java)
                intent.putExtra("hero", hero)
                startActivity(intent)
            }
        })

        // Dalam fungsi setupRecyclerViews
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        // Dalam fungsi setupRecyclerViews
        val handler = Handler(Looper.getMainLooper())
// Add scroll listener for pagination
        heroRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = linearLayoutManager.childCount
                val totalItemCount = linearLayoutManager.itemCount
                val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && paginatedHeroAdapter.canLoadMore()) {
                    // Tampilkan progress bar
                    progressBar.visibility = View.VISIBLE

                    // Tambahkan delay sebelum memuat data berikutnya
                    handler.postDelayed({
                        paginatedHeroAdapter.loadNextPage()
                        progressBar.visibility = View.GONE
                    }, 500) // 0.5s
                }
            }
        })

        // Adapter untuk tombol filter
        recyclerButtons.adapter = ButtonsAdapter(buttons) { role ->
            val filteredHeroes = when (role) {
                "All" -> heroViewModel.heroesList.sortedBy { it.name }
                else -> heroViewModel.heroesList.filter { it.role == role }.sortedBy { it.name }
            }
            paginatedHeroAdapter.updateFilteredData(filteredHeroes)
        }
    }

    // Paginated Hero Adapter with incremental loading and animations
    class PaginatedHeroAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        companion object {
            private const val PAGE_SIZE = 10
            private const val ANIMATION_DURATION = 100L
            private const val LOAD_DELAY = 300L // Delay antar item
            private const val AUTO_LOAD_DELAY = 2000L // Delay sebelum memuat item selanjutnya
        }

        interface OnItemClickListener {
            fun onItemClick(hero: Hero)
        }

        private var onItemClickListener: OnItemClickListener? = null

        fun setOnItemClickListener(listener: OnItemClickListener) {
            onItemClickListener = listener
        }

        private var originalHeroes: List<Hero> = listOf()
        private val displayedHeroes: MutableList<Hero> = mutableListOf()
        private var loadedItemCount = 0
        private var hasMoreData = true
        private val handler = Handler(Looper.getMainLooper())
        private var isLoading = false

        fun setInitialData(heroes: List<Hero>) {
            originalHeroes = heroes
            displayedHeroes.clear()
            loadedItemCount = 0
            hasMoreData = true
            isLoading = false

            // Muat 10 item pertama
            loadInitialPage()

            // Mulai proses auto-load
            scheduleAutoLoad()
        }

        private fun loadInitialPage() {
            val initialPageSize = minOf(PAGE_SIZE, originalHeroes.size)
            for (i in 0 until initialPageSize) {
                displayedHeroes.add(originalHeroes[i])
            }
            loadedItemCount = initialPageSize
            hasMoreData = loadedItemCount < originalHeroes.size
            notifyDataSetChanged()
        }

        private fun scheduleAutoLoad() {
            // Jika masih ada data yang bisa dimuat
            if (canLoadMore() && !isLoading) {
                handler.postDelayed({
                    loadNextPage()
                }, AUTO_LOAD_DELAY)
            }
        }

        fun updateFilteredData(heroes: List<Hero>) {
            originalHeroes = heroes
            displayedHeroes.clear()
            loadedItemCount = 0
            hasMoreData = true
            isLoading = false

            // Muat 10 item pertama setelah filter
            loadInitialPage()

            // Mulai proses auto-load
            scheduleAutoLoad()
        }

        fun canLoadMore() = hasMoreData && loadedItemCount < originalHeroes.size

        fun loadNextPage() {
            if (!canLoadMore() || isLoading) return

            isLoading = true
            val remainingItems = originalHeroes.size - loadedItemCount
            val itemsToLoad = minOf(PAGE_SIZE, remainingItems)

            for (i in 0 until itemsToLoad) {
                handler.postDelayed({
                    if (loadedItemCount < originalHeroes.size) {
                        displayedHeroes.add(originalHeroes[loadedItemCount])
                        notifyItemInserted(displayedHeroes.size - 1)
                        loadedItemCount++

                        // Jika ini adalah item terakhir yang dimuat
                        if (i == itemsToLoad - 1) {
                            isLoading = false

                            // Jadwalkan auto-load berikutnya
                            scheduleAutoLoad()
                        }
                    }
                }, i * LOAD_DELAY)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_heroes, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val heroHolder = holder as HeroViewHolder
            val hero = displayedHeroes[position]

            heroHolder.name.text = hero.name
            heroHolder.description.text = hero.description
            heroHolder.image.setImageResource(
                heroHolder.itemView.context.resources.getIdentifier(
                    hero.imageRes,
                    "drawable",
                    heroHolder.itemView.context.packageName
                )
            )
            val context = heroHolder.itemView.context
            val imageResId = context.resources.getIdentifier(hero.imageRes, "drawable", context.packageName)
            if (imageResId != 0) {
                heroHolder.image.setImageResource(imageResId)
            } else {
                heroHolder.image.setImageResource(R.drawable.hero) // Gambar default jika tidak ditemukan
            }

            holder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(hero)
            }


            // Animate item entry
            animateItemEntry(heroHolder.itemView, position)
        }

        private fun animateItemEntry(itemView: View, position: Int) {
            // Slide and fade animation
            val translationY = ObjectAnimator.ofFloat(itemView, "translationY", 100f, 0f)


            AnimatorSet().apply {
                playTogether(translationY)
                duration = ANIMATION_DURATION + (position * 50L) // Stagger effect
                start()
            }
        }

        override fun getItemCount(): Int = displayedHeroes.size

        // Hero View Holder (tetap sama)
        class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.title)
            val description: TextView = itemView.findViewById(R.id.desc)
            val image: ImageView = itemView.findViewById(R.id.images)
        }
    }

    // ButtonsAdapter remains the same as in the previous implementation
    class ButtonsAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<ButtonsAdapter.ButtonViewHolder>() {

        private var activeRole: String = "All"

        inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val button: Button = view.findViewById(R.id.button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
            return ButtonViewHolder(view)
        }

        override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
            val role = items[position]
            holder.button.text = role

            val isActive = role == activeRole
            holder.button.isActivated = isActive

            holder.button.setOnClickListener {
                activeRole = role
                onClick(role)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}