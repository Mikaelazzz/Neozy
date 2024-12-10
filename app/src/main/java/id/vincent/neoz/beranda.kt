package id.vincent.neoz


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.gson.annotations.SerializedName



object NetworkUtil {
    @RequiresApi(Build.VERSION_CODES.M)
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class NetworkChangeReceiver(private val onNetworkChange: (Boolean) -> Unit) : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val isConnected = NetworkUtil.isConnected(it)
            onNetworkChange(isConnected)
        }
    }
}


class beranda : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private var isWaitingForUserAction = false // Flag to control if user needs to press the button

    // Data class Hero
    data class Hero(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String,
        @SerializedName("imageRes") val imageRes: String,
        @SerializedName("role") val role: String
    )

    private var currentFragment: Fragment? = null // Variabel untuk menyimpan fragment saat ini

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        if (savedInstanceState != null) {
            currentFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
        }

        setupNetworkReceiver()
        checkInternetAndSetup()

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val titleTool: TextView = findViewById(R.id.toolbartitle)
        setSupportActionBar(toolbar)

        titleTool.text = "Home"
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup DrawerLayout
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentFragment?.let {
            supportFragmentManager.putFragment(outState, "currentFragment", it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupNetworkReceiver() {
        try {
            networkChangeReceiver = NetworkChangeReceiver { isConnected ->
                if (isConnected) {
                    if (isWaitingForUserAction) {
                        showNoInternetLayout()
                    } else {
                        isWaitingForUserAction = true
                        Toast.makeText(this, "Koneksi internet kembali!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showNoInternetLayout()
                }
            }

            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(networkChangeReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace() // Handle exceptions
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkInternetAndSetup() {
        if (NetworkUtil.isConnected(this)) {
            setupMainLayout()
        } else {
            showNoInternetLayout()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupMainLayout() {
        if (isNetworkConnected()) {
            // Jika koneksi internet kembali, tampilkan fragment terakhir yang disimpan
            if (currentFragment != null) {
                replaceFragment(currentFragment!!)
            } else {
                // Jika tidak ada fragment yang tersimpan, tampilkan fragment default (misalnya, patch())
                replaceFragment(patch())
            }
        } else {
            showNoInternetLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showNoInternetLayout() {
        // Avoid switching layout too often
        setContentView(R.layout.internet)

        val retryButton: Button = findViewById(R.id.button_signal)
        val progressBar: ProgressBar = findViewById(R.id.progress_bar)

        retryButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            retryButton.isEnabled = false

            // Retry network check with delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (NetworkUtil.isConnected(this)) {
                    setupMainLayout() // Reload main layout
                    isWaitingForUserAction = false
                } else {
                    Toast.makeText(this, "Tidak ada koneksi internet!", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    retryButton.isEnabled = true
                }
            }, 2500) // Delay for checking again
        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }


    private fun replaceFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val titletool : TextView = findViewById(R.id.toolbartitle)
        when (item.itemId) {
            R.id.home -> {
                titletool.text = "Home"
                replaceFragment(patch())
            }
            R.id.game -> {
                titletool.text = "Find Nick Game"
                replaceFragment(findergame())
            }
            R.id.filter -> {
                titletool.text = "Filter Draft"
                replaceFragment(draft())

            }
            R.id.hero -> {
                titletool.text = "Hero"
                replaceFragment(heroes())
            }
            R.id.emblem -> {
                titletool.text = "Emblem"
                replaceFragment(emblem())
            }
            R.id.spell -> {
                titletool.text = "Spell"
                replaceFragment(bspell())
            }
            R.id.item_hero -> {
                titletool.text = "Item Hero"
                replaceFragment(itemhero())
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
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }
}
