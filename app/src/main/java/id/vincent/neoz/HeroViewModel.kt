package id.vincent.neoz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HeroViewModel(application: Application) : AndroidViewModel(application) {
    private val _heroesList = MutableLiveData<List<heroes.Hero>>()
    val heroesList: LiveData<List<heroes.Hero>> = _heroesList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val gitHubService: GitHubHeroService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gitHubService = retrofit.create(GitHubHeroService::class.java)

        // Muat data saat ViewModel dibuat
        fetchHeroes()
    }

    fun fetchHeroes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = gitHubService.getHeroes()
                if (response.isSuccessful) {
                    val heroes = response.body() ?: emptyList()
                    _heroesList.value = heroes
                } else {
                    _error.value = "Failed to load heroes: ${response.code()}"
                    // Fallback ke data lokal jika gagal
                    _heroesList.value = HeroRepository.loadHeroes(getApplication())
                }
            } catch (e: Exception) {
                _error.value = "Error loading heroes: ${e.message}"
                // Fallback ke data lokal jika terjadi error
                _heroesList.value = HeroRepository.loadHeroes(getApplication())
            } finally {
                _isLoading.value = false
            }
        }
    }
}