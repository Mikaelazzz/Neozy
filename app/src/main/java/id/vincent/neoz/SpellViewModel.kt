package id.vincent.neoz

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Spell(
    val spell: String,
    val introSpell: String,
    val cdSpell: String,
    val imageSpell: String,
    val deskripsiSpell: String
)

class SpellViewModel (application: Application) : AndroidViewModel(application) {

    private val _spellList = MutableLiveData<List<Spell>>()
    val spellList: LiveData<List<Spell>> get() = _spellList

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context: Context = getApplication<Application>().applicationContext
            val json = context.assets.open("spell.json").bufferedReader().use { it.readText() }

            // Mengonversi JSON ke List<Spell>
            val type = object : TypeToken<List<Spell>>() {}.type
            val spells: List<Spell> = Gson().fromJson(json, type)

            _spellList.value = spells
        } catch (e: Exception) {
            e.printStackTrace() // Debugging error
            _spellList.value = emptyList() // Pastikan aplikasi tetap berjalan
        }
    }
}
