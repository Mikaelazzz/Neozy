package id.vincent.neoz


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val _itemlist = MutableLiveData<List<Item>>()
    val itemlist: LiveData<List<Item>> get() = _itemlist

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context = getApplication<Application>().applicationContext
            val json = context.assets.open("item.json").bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<Item>>() {}.type
            val items: List<Item> = Gson().fromJson(json, type)

            _itemlist.value = items
        } catch (e: Exception) {
            e.printStackTrace()
            _itemlist.value = emptyList()
        }
    }
}
