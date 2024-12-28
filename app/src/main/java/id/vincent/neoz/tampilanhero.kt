package id.vincent.neoz

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide

class tampilanhero : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tampilanhero)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val textToolbar = findViewById<TextView>(R.id.texttoolbar)
        val poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppinsbold)
        textToolbar.typeface = poppinsSemiBold

        val hero = intent.getParcelableExtra<heroes.Hero>("hero")

        if (hero != null) {
            // Set title setelah memastikan hero tidak null
            textToolbar.text = "Detail - ${hero.name}"

            val titleTextView = findViewById<TextView>(R.id.title)
            val tipeTextView = findViewById<TextView>(R.id.tipe)
            val bpTextView = findViewById<TextView>(R.id.bp)
            val dmTextView = findViewById<TextView>(R.id.dm)
            val tiketTextView = findViewById<TextView>(R.id.tiket)
            val descTextView = findViewById<TextView>(R.id.desc)
            val imghero = findViewById<ImageView>(R.id.images)
            val imgrole = findViewById<ImageView>(R.id.role)

            titleTextView.text = hero.name
            tipeTextView.text = hero.type
            bpTextView.text = hero.bp
            dmTextView.text = hero.dm
            tiketTextView.text = hero.tiket
            descTextView.text = hero.story

            // URL dasar untuk gambar hero dan role
            val heroImageBaseUrl = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/img/hero/"
            val roleLaneBaseUrl = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/img/lane/"

            // Memuat gambar hero dari GitHub
            Glide.with(this)
                .load("$heroImageBaseUrl${hero.imageRes}.png")
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imghero)

            // Memuat gambar role dari GitHub dengan fallback
            val roleImageUrl = "$roleLaneBaseUrl${hero.rolee}.png"
            Glide.with(this)
                .load(roleImageUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imgrole)

        } else {
            // Handle the case where hero is null
            Toast.makeText(this, "Hero data not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Metode untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}