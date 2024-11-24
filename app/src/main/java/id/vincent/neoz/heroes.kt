package id.vincent.neoz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.vincent.neoz.beranda.Hero

class heroes : Fragment() {

    private val heroViewModel: HeroViewModel by lazy {
        ViewModelProvider(requireActivity())[HeroViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.heroes, container, false)

        // Setup RecyclerViews
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
        val heroAdapter = HeroAdapter(sortedHeroes)
        heroRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        heroRecyclerView.adapter = heroAdapter

        // Adapter untuk tombol filter
        recyclerButtons.adapter = ButtonsAdapter(buttons) { role ->
            if (role == "All") {
                heroAdapter.updateData(sortedHeroes)
            } else {
                val filteredHeroes = heroViewModel.heroesList.filter { it.role == role }
                heroAdapter.updateData(filteredHeroes)
            }
        }
    }

    // HeroAdapter
    class HeroAdapter(private var heroes: List<Hero>) :
        RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {

        class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.title)
            val description: TextView = itemView.findViewById(R.id.desc)
            val image: ImageView = itemView.findViewById(R.id.images)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_heroes, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            val hero = heroes[position]
            holder.name.text = hero.name
            holder.description.text = hero.description
            holder.image.setImageResource(
                holder.itemView.context.resources.getIdentifier(hero.imageRes, "drawable", holder.itemView.context.packageName)
            )

        }

        override fun getItemCount(): Int = heroes.size

        fun updateData(newHeroes: List<Hero>) {
            heroes = newHeroes
            notifyDataSetChanged()
        }
    }

    // ButtonsAdapter
    class ButtonsAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<ButtonsAdapter.ButtonViewHolder>() {

        private var activeRole: String = "All" // Menyimpan role aktif

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

            // Update tampilan tombol berdasarkan apakah role aktif
            val isActive = role == activeRole
            holder.button.isActivated = isActive


            // Menangani klik tombol
            holder.button.setOnClickListener {
                activeRole = role // Set role aktif
                onClick(role) // Panggil callback untuk memfilter data
                notifyDataSetChanged() // Perbarui tampilan RecyclerView
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
