// GitHubHeroService.kt
package id.vincent.neoz

import retrofit2.Response
import retrofit2.http.GET

interface GitHubHeroService {
    @GET("Mikaelazzz/assets/master/hero/heroes.json")
    suspend fun getHeroes(): Response<List<heroes.Hero>>
}