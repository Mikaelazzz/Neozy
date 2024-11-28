package id.vincent.neoz


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Emblem(
    val titleE: String,
    val imageE: String
)

class EmblemViewModel(application: Application) : AndroidViewModel(application) {

    private val _emblemlist = MutableLiveData<List<Emblem>>()
    val emblemlist: LiveData<List<Emblem>> get() = _emblemlist

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context: Context = getApplication<Application>().applicationContext
            val json = context.assets.open("emblem.json").bufferedReader().use { it.readText() }

            // Mengonversi JSON ke List<Spell>
            val type = object : TypeToken<List<Emblem>>() {}.type
            val emblems: List<Emblem> = Gson().fromJson(json, type)

            _emblemlist.value = emblems
        } catch (e: Exception) {
            e.printStackTrace() // Debugging error
            _emblemlist.value = emptyList() // Pastikan aplikasi tetap berjalan
        }
    }
}