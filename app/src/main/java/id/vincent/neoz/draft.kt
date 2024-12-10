package id.vincent.neoz

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import kotlinx.coroutines.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


class HeroShuffleManager(
    private val mainActivity: FragmentActivity,
    private val heroData: List<HeroItem>,
    private val funCheckbox: RadioButton,
    private val roleCheckbox: RadioButton,
    private val player3Checkbox: RadioButton,
    private val player5Checkbox: RadioButton,
    private val heroesRecyclerView: RecyclerView,
    private val shuffleButton: Button
) {
    // Data class to represent hero information
    data class HeroItem(
        val hero: String,
        val imageHero: String,
        val role: String
    )

    // ViewHolder for the RecyclerView
    class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerTextView: TextView = itemView.findViewById(R.id.player)
        val heroImageView: ImageView = itemView.findViewById(R.id.imgHero)
        val heroNameTextView: TextView = itemView.findViewById(R.id.namaHero)
        val roleImageView: ImageView = itemView.findViewById(R.id.imgRole)
    }

    // Adapter for RecyclerView
    inner class HeroAdapter(private val heroes: List<HeroItem>) :
        RecyclerView.Adapter<HeroViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            val hero = heroes[position]

            // Set player number
            holder.playerTextView.text = "Player ${position + 1}"

            // Set hero name
            holder.heroNameTextView.text = hero.hero

            // Set hero image
            val heroImageResourceId = mainActivity.resources.getIdentifier(
                hero.imageHero, "drawable", mainActivity.packageName
            )
            if (heroImageResourceId != 0) {
                holder.heroImageView.setImageResource(heroImageResourceId)
            }

            // Set role image based on role
            val roleImageResourceId = when (hero.role.lowercase()) {
                "mage" -> R.drawable.mid
                "gold" -> R.drawable.gold
                "jung" -> R.drawable.jung
                "roam" -> R.drawable.roam
                "exp" -> R.drawable.exp
                else -> R.drawable.vengeance
            }
            holder.roleImageView.setImageResource(roleImageResourceId)

            // Check the length of hero's name and adjust text size
            val heroName = hero.hero
            if (heroName.length > 6) {
                holder.heroNameTextView.textSize = 11f  // Adjust text size if more than 7 characters
                holder.heroNameTextView.setPadding(0,5.2.toInt(),0,0 )
            } else {
                holder.heroNameTextView.textSize = 13f  // Default text size
            }
        }

        override fun getItemCount() = heroes.size
    }

    fun setupShuffleLogic() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        // Ensure only one checkbox can be checked at a time
        funCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) roleCheckbox.isChecked = false
        }

        roleCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) funCheckbox.isChecked = false
        }

        // Ensure only one player count checkbox can be checked at a time
        player3Checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) player5Checkbox.isChecked = false
        }

        player5Checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) player3Checkbox.isChecked = false
        }

        shuffleButton.setOnClickListener {
            val selectedHeroes: List<HeroShuffleManager.HeroItem> = when {
                !funCheckbox.isChecked && !roleCheckbox.isChecked -> {
                    return@setOnClickListener // Menghentikan eksekusi jika tidak ada mode yang dipilih
                }
                !player3Checkbox.isChecked && !player5Checkbox.isChecked -> {
                    return@setOnClickListener
                }
                funCheckbox.isChecked -> {
                    shuffleFunMode()
                    listOf()
                }
                roleCheckbox.isChecked -> {
                    shuffleHeroesByRole()
                    listOf()
                }
                else -> {
                    emptyList()
                }
            }

            if (selectedHeroes.isEmpty()) {
                return@setOnClickListener
            }

            // Update RecyclerView dengan tampilan satu per satu
            updateRecyclerViewSequentially(selectedHeroes)

            val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)
            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateRecyclerViewSequentially(heroes: List<HeroItem>) {
        val availableHeroes = heroes.toMutableList()
        val selectedHeroes = mutableListOf<HeroItem>()

        CoroutineScope(Dispatchers.Main).launch {
            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)

            heroesRecyclerView.adapter = HeroAdapter(listOf()) // Start with an empty adapter

            for (i in 1..20) {
                delay(500)
                val shuffledHeroes = availableHeroes.shuffled()
                heroesRecyclerView.adapter = HeroAdapter(shuffledHeroes)
            }

            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            for (i in 1..playerCount) {
                delay(0)
                val heroToPick = availableHeroes.random()
                selectedHeroes.add(heroToPick)
                availableHeroes.remove(heroToPick)
            }

            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes)
        }
    }

    private fun shuffleFunMode() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        CoroutineScope(Dispatchers.Main).launch {
            val availableHeroes = heroData.toMutableList()
            val selectedHeroes = mutableListOf<HeroItem>()
            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(listOf())

            for (cycle in 1..20) {
                delay(50)
                val tempHeroes = availableHeroes.shuffled().take(playerCount)
                val tempAdapterHeroes = tempHeroes.map {
                    it.copy(role = listOf("Mage", "Gold", "Jung", "Roam", "Exp").random())
                }
                heroesRecyclerView.adapter = HeroAdapter(tempAdapterHeroes)
            }

            while (selectedHeroes.size < playerCount) {
                val heroToPick = availableHeroes.random()
                val randomRole = listOf("Mage", "Gold", "Jung", "Roam", "Exp").random()

                if (selectedHeroes.any { it.hero == heroToPick.hero || it.role == randomRole }) continue

                selectedHeroes.add(heroToPick.copy(role = randomRole))
                availableHeroes.remove(heroToPick)
            }

            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes)

            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun shuffleHeroesByRole() {
        val hideTextView = mainActivity.findViewById<TextView>(R.id.hidetext)

        CoroutineScope(Dispatchers.Main).launch {
            val availableHeroes = heroData.toMutableList()
            val selectedHeroes = mutableListOf<HeroItem>()

            if (availableHeroes.isEmpty()) {
                return@launch
            }

            val playerCount = if (player3Checkbox.isChecked) 3 else 5

            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(listOf())

            for (cycle in 1..20) {
                delay(50)
                val tempHeroes = availableHeroes.shuffled().take(playerCount)
                val tempAdapterHeroes = tempHeroes.map { it.copy() }

                heroesRecyclerView.adapter = HeroAdapter(tempAdapterHeroes)
            }

            while (selectedHeroes.size < playerCount) {
                val heroToPick = availableHeroes.random()
                val randomRole = heroToPick.role

                if (selectedHeroes.any { it.hero == heroToPick.hero || it.role == randomRole }) {
                    continue
                }

                selectedHeroes.add(heroToPick)
                availableHeroes.remove(heroToPick)
            }

            heroesRecyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
            heroesRecyclerView.adapter = HeroAdapter(selectedHeroes)

            hideTextView.visibility = View.GONE
            heroesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateRecyclerView(heroes: List<HeroItem>) {
        heroesRecyclerView.layoutManager = GridLayoutManager(mainActivity, heroes.size)
        heroesRecyclerView.adapter = HeroAdapter(heroes)
    }

    companion object {
        // Helper method to parse JSON data
        fun parseHeroData(jsonString: String): List<HeroItem> {
            val jsonArray = JSONArray(jsonString)
            return (0 until jsonArray.length()).map { index ->
                val heroJson = jsonArray.getJSONObject(index)
                HeroItem(
                    hero = heroJson.getString("hero"),
                    imageHero = heroJson.getString("imageHero"),
                    role = heroJson.getString("role")
                )
            }
        }
    }
}

class draft : Fragment() {
    private lateinit var heroShuffleManager: HeroShuffleManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.draft, container, false)

        setupMainLayout(view)

        return view
    }

    private fun setupMainLayout(view: View) {
        val context = requireContext()
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(R.layout.draft, null)

        val jsonString = context.assets.open("heroesdraft.json").bufferedReader().use { it.readText() }

        heroShuffleManager = HeroShuffleManager(
            mainActivity = requireActivity(),
            heroData = HeroShuffleManager.parseHeroData(jsonString),
            funCheckbox = view.findViewById(R.id.funrole),
            roleCheckbox = view.findViewById(R.id.role),
            player3Checkbox = view.findViewById(R.id.main3),
            player5Checkbox = view.findViewById(R.id.main5),
            heroesRecyclerView = view.findViewById(R.id.heroes),
            shuffleButton = view.findViewById(R.id.shuffle)
        )

        heroShuffleManager.setupShuffleLogic()
    }
}
