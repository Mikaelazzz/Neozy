package id.vincent.neoz

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException

class PatchViewModel(application: Application) : AndroidViewModel(application) {

    // Data class untuk patch
    data class PatchData(
        val patchVersion: String,
        val patches: List<patch.UPatch>
    )

    private val _patchlist = MutableLiveData<List<patch.UPatch>>()
    val patchlist: LiveData<List<patch.UPatch>> get() = _patchlist

    private val _patchFiles = MutableLiveData<List<String>>()
    val patchFiles: LiveData<List<String>> get() = _patchFiles

    private val _patchVersions = MutableLiveData<List<String>>()
    val patchVersions: LiveData<List<String>> get() = _patchVersions

    private val _currentPatchVersion = MutableLiveData<String>()
    val currentPatchVersion: LiveData<String> get() = _currentPatchVersion

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Interface untuk service GitHub
    interface GitHubService {
        @GET("{filename}")
        suspend fun getPatchData(@Path("filename") filename: String): Response<String>
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
    private val client = OkHttpClient()

    init {
        val retrofitRaw = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/Mikaelazzz/assets/master/data/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

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
                val response = gitHubApiService.getRepositoryContents(
                    username = "Mikaelazzz",
                    repo = "assets",
                    path = "data"
                )

                val patchFiles = response.body()
                    ?.filter { it.name.startsWith("heropatch") && it.name.endsWith(".json") }
                    ?.map { it.name }
                    ?: emptyList()

                _patchFiles.value = patchFiles

                // Ekstrak patch versions secara asynchronous
                val patchVersions = extractPatchVersions(patchFiles)
                _patchVersions.value = patchVersions

                // Muat file pertama secara default
                if (patchFiles.isNotEmpty()) {
                    loadDataFromGitHub(patchFiles.first())
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat daftar patch: ${e.message}"
                loadPatchFilesManual()
            }
        }
    }

    private suspend fun extractPatchVersions(patchFiles: List<String>): List<String> = withContext(Dispatchers.IO) {
        patchFiles.map { fileName ->
            async {
                try {
                    val url = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/data/$fileName"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val jsonString = response.body?.string()
                        if (jsonString != null) {
                            val patchData = parseJson(jsonString)
                            "Patch ${patchData.patchVersion}"
                        } else {
                            "Patch ${fileName.replace("heropatch", "").replace(".json", "")}"
                        }
                    } else {
                        "Patch ${fileName.replace("heropatch", "").replace(".json", "")}"
                    }
                } catch (e: Exception) {
                    Log.e("PatchViewModel", "Error extracting patch version", e)
                    "Patch ${fileName.replace("heropatch", "").replace(".json", "")}"
                }
            }
        }.awaitAll()
    }

    private fun loadPatchFilesManual() {
        viewModelScope.launch {
            try {
                val url = "https://api.github.com/repos/Mikaelazzz/assets/contents/data"
                val request = Request.Builder().url(url).build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val files = parseGitHubFiles(jsonString)

                    val patchFiles = files
                        .filter { it.startsWith("heropatch") && it.endsWith(".json") }

                    _patchFiles.value = patchFiles

                    // Ekstrak patch versions
                    val patchVersions = extractPatchVersions(patchFiles)
                    _patchVersions.value = patchVersions

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

    private fun parseGitHubFiles(jsonString: String?): List<String> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<GitHubFile>>() {}.type
            val files: List<GitHubFile> = gson.fromJson(jsonString, type)
            files.map { it.name }
        } catch (e: Exception) {
            Log.e("PatchViewModel", "Error parsing GitHub files", e)
            emptyList()
        }
    }

    fun loadDataFromGitHub(fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = gitHubService.getPatchData(fileName)

                if (response.isSuccessful) {
                    val jsonString = response.body()
                    if (jsonString != null) {
                        val patchData = parseJson(jsonString)
                        _patchlist.value = patchData.patches
                        _currentPatchVersion.value = patchData.patchVersion
                    } else {
                        _error.value = "Tidak ada data yang diterima"
                    }
                } else {
                    loadDataFromGitHubManual(fileName)
                }
            } catch (e: Exception) {
                loadDataFromGitHubManual(fileName)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadDataFromGitHubManual(fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val url = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/data/$fileName"
                val request = Request.Builder().url(url).build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    if (jsonString != null) {
                        val patchData = parseJson(jsonString)
                        _patchlist.value = patchData.patches
                        _currentPatchVersion.value = patchData.patchVersion
                    } else {
                        _error.value = "Tidak ada data yang diterima"
                    }
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

    private fun parseJson(jsonString: String): PatchData {
        return Gson().fromJson(jsonString, PatchData::class.java)
    }
}