package id.vincent.neoz

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class HeroViewModel(application: Application) : AndroidViewModel(application) {
    val heroesList: List<heroes.Hero> = HeroRepository.loadHeroes(application)
}
