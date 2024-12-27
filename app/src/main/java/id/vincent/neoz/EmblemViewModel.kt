package id.vincent.neoz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class EmblemViewModel(application: Application) : AndroidViewModel(application) {
    private val _emblemlist = MutableLiveData<List<emblem.Emblem>>()
    val emblemlist: LiveData<List<emblem.Emblem>> get() = _emblemlist

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Interface untuk service GitHub
    interface GitHubService {
        @GET("Mikaelazzz/assets/master/emblem/emblem.json")
        suspend fun getEmblemData(): Response<List<emblem.Emblem>>
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
        fetchEmblems()
    }

    fun fetchEmblems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = gitHubService.getEmblemData()
                if (response.isSuccessful) {
                    val emblems = response.body() ?: emptyList()
                    _emblemlist.value = emblems
                } else {
                    _error.value = "Failed to load emblems: ${response.code()}"
                    // Fallback ke metode manual jika Retrofit gagal
                    fetchEmblemsManual()
                }
            } catch (e: Exception) {
                _error.value = "Error loading emblems: ${e.message}"
                // Fallback ke metode manual jika terjadi exception
                fetchEmblemsManual()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchEmblemsManual() {
        viewModelScope.launch {
            try {
                val url = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/emblem/emblem.json"
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val type = object : TypeToken<List<emblem.Emblem>>() {}.type
                    val emblems: List<emblem.Emblem> = Gson().fromJson(jsonString, type)
                    _emblemlist.value = emblems
                } else {
                    _error.value = "Failed to load emblems manually: ${response.code}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading emblems manually: ${e.message}"
            }
        }
    }
}