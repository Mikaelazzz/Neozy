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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    private lateinit var heroViewModel: HeroViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var paginatedHeroAdapter: PaginatedHeroAdapter
    private lateinit var heroRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.heroes, container, false)

        // Inisialisasi ProgressBar
        progressBar = view.findViewById(R.id.progress_bar)

        // Inisialisasi ViewModel
        heroViewModel = ViewModelProvider(requireActivity())[HeroViewModel::class.java]

        // Inisialisasi RecyclerView dan Adapter
        setupRecyclerViews(view)

        // Observer untuk loading state
        heroViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer untuk error
        heroViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observer untuk daftar hero
        heroViewModel.heroesList.observe(viewLifecycleOwner) { heroes ->
            paginatedHeroAdapter.updateFilteredData(heroes)
        }

        return view
    }

    private fun setupRecyclerViews(view: View) {
        // RecyclerView untuk tombol filter
        val recyclerButtons = view.findViewById<RecyclerView>(R.id.recycler_buttons)
        recyclerButtons.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Daftar role untuk filter
        val buttons = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")

        // RecyclerView untuk daftar hero
        heroRecyclerView = view.findViewById(R.id.list_hero)
        paginatedHeroAdapter = PaginatedHeroAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        heroRecyclerView.layoutManager = linearLayoutManager
        heroRecyclerView.adapter = paginatedHeroAdapter

        // Set click listener untuk item hero
        paginatedHeroAdapter.setOnItemClickListener(object : PaginatedHeroAdapter.OnItemClickListener {
            override fun onItemClick(hero: Hero) {
                val intent = Intent(requireContext(), tampilanhero::class.java)
                intent.putExtra("hero", hero)
                startActivity(intent)
            }
        })

        // Tambahkan scroll listener untuk pagination
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
                    Handler(Looper.getMainLooper()).postDelayed({
                        paginatedHeroAdapter.loadNextPage()
                        progressBar.visibility = View.GONE
                    }, 1000)
                }
            }
        })

        // Adapter untuk tombol filter
        recyclerButtons.adapter = ButtonsAdapter(buttons) { role ->
            val filteredHeroes = when (role) {
                "All" -> heroViewModel.heroesList.value ?: emptyList()
                else -> (heroViewModel.heroesList.value ?: emptyList())
                    .filter { it.role.split(",").map { r -> r.trim() }.contains(role) }
            }
            paginatedHeroAdapter.updateFilteredData(filteredHeroes)
        }
    }

    // PaginatedHeroAdapter - tetap sama seperti sebelumnya
    class PaginatedHeroAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        companion object {
            private const val PAGE_SIZE = 10
            private const val ANIMATION_DURATION = 150L
            private const val LOAD_DELAY = 500L
            private const val AUTO_LOAD_DELAY = 2000L
        }

        interface OnItemClickListener {
            fun onItemClick(hero: Hero)
        }

        private var onItemClickListener: OnItemClickListener? = null
        private var originalHeroes: List<Hero> = listOf()
        private val displayedHeroes: MutableList<Hero> = mutableListOf()
        private var loadedItemCount = 0
        private var hasMoreData = true
        private var isLoading = false

        fun setOnItemClickListener(listener: OnItemClickListener) {
            onItemClickListener = listener
        }

        fun updateFilteredData(heroes: List<Hero>) {
            originalHeroes = heroes
            displayedHeroes.clear()
            loadedItemCount = 0
            hasMoreData = true
            isLoading = false
            loadInitialPage()
        }

        fun canLoadMore() = hasMoreData && loadedItemCount < originalHeroes.size

        fun loadNextPage() {
            if (!canLoadMore() || isLoading) return

            isLoading = true
            val remainingItems = originalHeroes.size - loadedItemCount
            val itemsToLoad = minOf(PAGE_SIZE, remainingItems)

            for (i in 0 until itemsToLoad) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (loadedItemCount < originalHeroes.size) {
                        displayedHeroes.add(originalHeroes[loadedItemCount])
                        notifyItemInserted(displayedHeroes.size - 1)
                        loadedItemCount++

                        if (i == itemsToLoad - 1) {
                            isLoading = false
                        }
                    }
                }, i * LOAD_DELAY)
            }
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

            // Memuat gambar dari GitHub
            val heroImageBaseUrl = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/img/"
            Glide.with(heroHolder.itemView.context)
                .load("$heroImageBaseUrl${hero.imageRes}.png")
                .placeholder(R.drawable.hero)
                .error(R.drawable.hero)
                .into(heroHolder.image)

            holder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(hero)
            }

            // Animate item entry
            animateItemEntry(heroHolder.itemView, position)
        }

        private fun animateItemEntry(itemView: View, position: Int) {
            val translationY = ObjectAnimator.ofFloat(itemView, "translationY", 100f, 0f)
            AnimatorSet().apply {
                playTogether(translationY)
                duration = ANIMATION_DURATION + (position * 50L)
                start()
            }
        }

        override fun getItemCount(): Int = displayedHeroes.size

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