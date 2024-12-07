package id.vincent.neoz


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PatchViewModel(application: Application) : AndroidViewModel(application) {

    private val _patchlist = MutableLiveData<List<UPatch>>()
    val patchlist: LiveData<List<UPatch>> get() = _patchlist

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context = getApplication<Application>().applicationContext
            val json = context.assets.open("heropatch.json").bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<UPatch>>() {}.type
            val patchs: List<UPatch> = Gson().fromJson(json, type)

            _patchlist.value = patchs
        } catch (e: Exception) {
            e.printStackTrace()
            _patchlist.value = emptyList()
        }
    }
}
