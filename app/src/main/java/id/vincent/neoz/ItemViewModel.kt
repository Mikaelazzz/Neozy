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

    private var allItems: List<Item> = emptyList() // Data lengkap untuk semua item
    private val _filteredItems = MutableLiveData<List<Item>>()
    val filteredItems: LiveData<List<Item>> get() = _filteredItems

    init {
        loadDataFromJson()
    }

    private fun loadDataFromJson() {
        try {
            val context = getApplication<Application>().applicationContext
            val json = context.assets.open("item.json").bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<Item>>() {}.type
            allItems = Gson().fromJson(json, type)
//            val items: List<Item> = Gson().fromJson(json, type)
//            _itemlist.value = items
            _itemlist.value = allItems
            _filteredItems.value = allItems // Tampilkan semua item sebagai default
        } catch (e: Exception) {
            e.printStackTrace()
            _itemlist.value = emptyList()
            _filteredItems.value = emptyList()
        }
    }
    fun filterItemsByRole(role: String) {
        _filteredItems.value = if (role == "all") allItems else allItems.filter { it.role == role }
    }
}

