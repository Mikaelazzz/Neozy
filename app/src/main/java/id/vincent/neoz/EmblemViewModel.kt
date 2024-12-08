package id.vincent.neoz


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class EmblemViewModel(application: Application) : AndroidViewModel(application) {

    private val _emblemlist = MutableLiveData<List<emblem.Emblem>>()
    val emblemlist: LiveData<List<emblem.Emblem>> get() = _emblemlist

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context: Context = getApplication<Application>().applicationContext
            val json = context.assets.open("emblem.json").bufferedReader().use { it.readText() }

            // Mengonversi JSON ke List<Spell>
            val type = object : TypeToken<List<emblem.Emblem>>() {}.type
            val emblems: List<emblem.Emblem> = Gson().fromJson(json, type)

            _emblemlist.value = emblems
        } catch (e: Exception) {
            e.printStackTrace() // Debugging error
            _emblemlist.value = emptyList() // Pastikan aplikasi tetap berjalan
        }
    }
}