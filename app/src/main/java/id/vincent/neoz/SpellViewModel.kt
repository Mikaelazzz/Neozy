package id.vincent.neoz

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Spell(
    val spell: String,
    val introSpell: String,
    val cdSpell: String,
    val imageSpell: String,
    val deskripsiSpell: String
)
class SpellViewModel(application: Application) : AndroidViewModel(application) {
    private val _spellList = MutableLiveData<List<Spell>>()
    val spellList: LiveData<List<Spell>> get() = _spellList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Interface untuk service GitHub
    interface GitHubService {
        @GET("Mikaelazzz/assets/master/spell/spell.json")
        suspend fun getSpellData(): Response<List<Spell>>
    }

    private val gitHubService: GitHubService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gitHubService = retrofit.create(GitHubService::class.java)

        // Muat data saat ViewModel dibuat
        fetchSpells()
    }

    fun fetchSpells() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = gitHubService.getSpellData()
                if (response.isSuccessful) {
                    val spells = response.body() ?: emptyList()
                    _spellList.value = spells
                } else {
                    _error.value = "Failed to load spells: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading spells: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}