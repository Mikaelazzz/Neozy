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
    private var isNetworkConnected = true // Untuk menyimpan status koneksi terakhir
    private var lastFragment: Fragment? = null // Menyimpan fragment terakhir

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private var isWaitingForUserAction = false // Flag to control if user needs to press the button





    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda) // Tampilkan layout normal saat pertama kali


        if (savedInstanceState != null) {
            lastFragment = supportFragmentManager.getFragment(savedInstanceState, "lastFragment")
        }

        showDefaultFragment()  // Tampilkan fragment default saat aplikasi dimulai

        setupNetworkReceiver()



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
        lastFragment?.let {
            supportFragmentManager.putFragment(outState, "lastFragment", it)
        }
    }


    private fun showDefaultFragment() {
        if (lastFragment == null) {
            replaceFragment(patch()) // Tampilkan fragment default
        } else {
            replaceFragment(lastFragment!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupNetworkReceiver() {
        networkChangeReceiver = NetworkChangeReceiver { isConnected ->
            if (isConnected) {
                if (!isNetworkConnected) { // Jika sebelumnya terputus
                    isNetworkConnected = true
                    // Menunggu konfirmasi dari pengguna melalui tombol signal
                    showProgressAndWaitForUserConfirmation()
                }
            } else {
                if (isNetworkConnected) { // Jika sebelumnya terhubung
                    isNetworkConnected = false
                    showNoInternetLayout() // Tampilkan halaman internet.xml
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    // Fungsi untuk menampilkan progress bar dan menunggu konfirmasi pengguna
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showProgressAndWaitForUserConfirmation() {
        val progressBar: ProgressBar = findViewById(R.id.progress_bar)
        val buttonSignal: Button = findViewById(R.id.button_signal)

        // Menampilkan ProgressBar dan menonaktifkan tombol
        progressBar.visibility = View.VISIBLE
        buttonSignal.isEnabled = false

        // Menunggu 2 detik sebelum memeriksa koneksi
        Handler(Looper.getMainLooper()).postDelayed({
            // Pengecekan koneksi
            if (NetworkUtil.isConnected(this)) {
                // Koneksi ada, lanjutkan dengan mengganti fragment
                progressBar.visibility = View.GONE
                buttonSignal.isEnabled = true
                buttonSignal.setOnClickListener {
                    // Tombol signal diklik, lanjutkan ke lastFragment
                    replaceFragment(lastFragment ?: patch())
                }
            } else {
                // Tidak ada koneksi, tampilkan layout internet.xml
                showNoInternetLayout()
                progressBar.visibility = View.GONE
                buttonSignal.isEnabled = true
                buttonSignal.setOnClickListener {
                    // Jika tombol diklik dan tidak ada koneksi, tetap di halaman internet.xml
                    showNoInternetLayout()
                }
            }
        }, 1500) // 2 detik delay
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
        lastFragment?.let { replaceFragment(it) } // Kembalikan fragment terakhir
    }


    private fun showNoInternetLayout() {
        replaceFragment(NoInternetFragment()) // Tampilkan fragment untuk halaman internet
    }




    private fun replaceFragment(fragment: Fragment) {
        if (fragment !is NoInternetFragment) {
            lastFragment = fragment // Simpan fragment terakhir sebelum mengganti
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun onInternetRestored() {
        isWaitingForUserAction = false
        if (lastFragment != null) {
            replaceFragment(lastFragment!!)
        } else {
            replaceFragment(patch()) // Fragment default jika tidak ada lastFragment
        }
        Toast.makeText(this, "Koneksi internet stabil!", Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun handleInternetRestoration() {
        // Jika koneksi pulih, kembali ke fragment terakhir yang dikunjungi
        if (lastFragment != null) {
            replaceFragment(lastFragment!!)
        } else {
            // Jika tidak ada lastFragment, tampilkan fragment default
            replaceFragment(patch()) // Atau fragment default lainnya
        }
        Toast.makeText(this, "Koneksi internet stabil!", Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Periksa koneksi internet
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Periksa koneksi internet anda", Toast.LENGTH_SHORT).show()
            setupNetworkReceiver()
            return false
        }

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
