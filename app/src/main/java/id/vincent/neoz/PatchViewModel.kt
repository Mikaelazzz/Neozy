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
import retrofit2.http.Path

class PatchViewModel(application: Application) : AndroidViewModel(application) {

    private val _patchlist = MutableLiveData<List<patch.UPatch>>()
    val patchlist: LiveData<List<patch.UPatch>> get() = _patchlist

    private val _patchFiles = MutableLiveData<List<String>>()
    val patchFiles: LiveData<List<String>> get() = _patchFiles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Interface untuk service GitHub
    interface GitHubService {
        @GET("{filename}")
        suspend fun getPatchData(@Path("filename") filename: String): Response<List<patch.UPatch>>
    }

    interface GitHubApiService {
        @GET("repos/{username}/{repo}/contents/{path}")
        suspend fun getRepositoryContents(
            @Path("username") username: String,
            @Path("repo") repo: String,
            @Path("path") path: String
        ): Response<List<GitHubFile>>
    }

    // Data class untuk respons GitHub API
    data class GitHubFile(
        val name: String,
        val path: String,
        val type: String
    )

    private val gitHubService: GitHubService
    private val gitHubApiService: GitHubApiService

    init {
        val client = OkHttpClient.Builder().build()

        // Ubah base URL untuk mengambil raw file dari GitHub
        val retrofitRaw = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/Mikaelazzz/assets/master/data/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Service untuk mengambil daftar file dari GitHub API
        val retrofitApi = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gitHubService = retrofitRaw.create(GitHubService::class.java)
        gitHubApiService = retrofitApi.create(GitHubApiService::class.java)

        loadPatchFiles()
    }

    private fun loadPatchFiles() {
        viewModelScope.launch {
            try {
                // Dapatkan daftar file
                val response = gitHubApiService.getRepositoryContents(
                    username = "Mikaelazzz",
                    repo = "assets",
                    path = "data"
                )

                // Filter file JSON yang sesuai
                val patchFiles = response.body()
                    ?.filter { it.name.endsWith(".json") }
                    ?.map { it.name }
                    ?: emptyList()

                _patchFiles.value = patchFiles

                // Muat file pertama secara default
                if (patchFiles.isNotEmpty()) {
                    loadDataFromGitHub(patchFiles.first())
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat daftar patch: ${e.message}"
            }
        }
    }

    // Tambahkan method untuk mengambil daftar file secara manual jika Retrofit gagal
    private fun loadPatchFilesManual() {
        viewModelScope.launch {
            try {
                val url = "https://api.github.com/repos/Mikaelazzz/assets/contents/data"
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val files = parseGitHubFiles(jsonString)

                    val patchFiles = files
                        .filter { it.endsWith(".json") }

                    _patchFiles.value = patchFiles

                    // Muat file pertama secara default
                    if (patchFiles.isNotEmpty()) {
                        loadDataFromGitHub(patchFiles.first())
                    }
                } else {
                    _error.value = "Gagal memuat daftar file: ${response.code}"
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan: ${e.message}"
            }
        }
    }

    // Method parsing file GitHub
    private fun parseGitHubFiles(jsonString: String?): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<GitHubFile>>() {}.type
        val files: List<GitHubFile> = gson.fromJson(jsonString, type)

        return files.map { it.name }
    }

    // Fallback method jika Retrofit gagal
    fun loadDataFromGitHubManual(fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val url = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/data/$fileName"
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val patchList = parsePatches(jsonString)
                    _patchlist.value = patchList
                } else {
                    _error.value = "Gagal memuat data: ${response.code}"
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan jaringan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Method parsing patch
    private fun parsePatches(jsonString: String?): List<patch.UPatch> {
        val gson = Gson()
        val type = object : TypeToken<List<patch.UPatch>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    // Override method loadDataFromGitHub untuk tambahkan fallback
    fun loadDataFromGitHub(fileName: String) {
        viewModelScope.launch {
            try {
                // Coba dengan Retrofit terlebih dahulu
                val response = gitHubService.getPatchData(fileName)

                if (response.isSuccessful) {
                    _patchlist.value = response.body() ?: emptyList()
                } else {
                    // Jika gagal, gunakan metode manual
                    loadDataFromGitHubManual(fileName)
                }
            } catch (e: Exception) {
                // Jika terjadi exception, gunakan metode manual
                loadDataFromGitHubManual(fileName)
            }
        }
    }
}