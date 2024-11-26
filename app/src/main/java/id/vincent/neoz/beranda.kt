package id.vincent.neoz

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.gson.annotations.SerializedName

class beranda : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Data class Hero
    data class Hero(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String,
        @SerializedName("imageRes") val imageRes: String,
        @SerializedName("role") val role: String
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        // Setup Toolbar
//        val typeface = ResourcesCompat.getFont(this,R.font.poppinssemibold)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val titletool: TextView = findViewById(R.id.toolbartitle)
        setSupportActionBar(toolbar)

        // Set the initial toolbar title to "Home"
        titletool.text = "Home"
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup DrawerLayout
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Setup ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

    }

    private fun replaceFragment(fragment: Fragment) {
        // Hide the ScrollView content
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val titletool : TextView = findViewById(R.id.toolbartitle)
        when (item.itemId) {
            R.id.home -> {
                titletool.text = "Home"
            }
            R.id.hero -> {
                titletool.text = "Hero"
                replaceFragment(heroes())
            }
            R.id.emblem -> {
                titletool.text = "Emblem"
                // TODO: Add code to display heroes list
            }
            R.id.spell -> {
                titletool.text = "Spell"
                replaceFragment(bspell())
            }
            R.id.item_hero -> {
                titletool.text = "Item Hero"
                // TODO: Add code to display heroes list
            }
            R.id.winstreak -> {
                titletool.text = "Win Rate"
                replaceFragment(winrate())
            }
            R.id.lose_streak -> {
                titletool.text = "Target Match"
                replaceFragment(lose())
            }
            R.id.mgcwheel -> {
                titletool.text = "Magic Wheel"
                replaceFragment(magicwheel())

            }
            R.id.zodiac -> {
                titletool.text = "Zodiac"
                replaceFragment(zodiac())
            }
            R.id.allmatch -> {
                titletool.text = "Total Match"
                replaceFragment(total())
            }
            R.id.aboutus -> {
                titletool.text = "About Us"
//                title = "About Us"
                // TODO: Add code to show About Us information
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
