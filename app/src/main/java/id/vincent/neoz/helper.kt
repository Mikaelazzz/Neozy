package id.vincent.neoz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object HeroRepository {
    fun loadHeroes(context: Context): List<heroes.Hero> {
        val jsonString = context.assets.open("heroes.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<heroes.Hero>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }

}







