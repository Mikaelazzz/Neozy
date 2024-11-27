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

class bspell : Fragment() {
    private val spellViewModel: SpellViewModel by lazy {
        ViewModelProvider(requireActivity())[SpellViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.battlespell, container, false)

        setupRecyclerView(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list_spell)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = SpellAdapter(emptyList()) // Adapter kosong saat pertama kali
        recyclerView.adapter = adapter

        // Observasi data dari ViewModel
        spellViewModel.spellList.observe(viewLifecycleOwner) { spells ->
            adapter.updateData(spells)
        }
    }

    class SpellAdapter(private var spells: List<Spell>) :
        RecyclerView.Adapter<SpellAdapter.SpellViewHolder>() {

        class SpellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.titleSpell)
            val intro: TextView = itemView.findViewById(R.id.subtitleSpell)
            val desc: TextView = itemView.findViewById(R.id.descSpell)
            val cooldown: TextView = itemView.findViewById(R.id.cdSpell)
            val image: ImageView = itemView.findViewById(R.id.imagesSpell)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpellViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_spell, parent, false)
            return SpellViewHolder(view)
        }

        override fun onBindViewHolder(holder: SpellViewHolder, position: Int) {
            val spell = spells[position]
            holder.name.text = spell.spell
            holder.intro.text = spell.introSpell
            holder.desc.text = spell.deskripsiSpell
            holder.cooldown.text = "Cooldown : ${spell.cdSpell}s"
            holder.image.setImageResource(
                holder.itemView.context.resources.getIdentifier(spell.imageSpell, "drawable", holder.itemView.context.packageName)
            )
        }

        override fun getItemCount(): Int = spells.size

        fun updateData(newSpells: List<Spell>) {
            spells = newSpells
            notifyDataSetChanged()
        }
    }
}
