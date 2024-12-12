package id.vincent.neoz

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat

class tampilanhero : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tampilanhero)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val titleTextView = toolbar.findViewById<TextView>(androidx.appcompat.R.id.action_bar_title)
        if (titleTextView != null) {
            val poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppinsbold)
            titleTextView.typeface = poppinsSemiBold
        }

        val hero = intent.getParcelableExtra<heroes.Hero>("hero")

        if (hero != null) {
            // Set title setelah memastikan hero tidak null
            supportActionBar?.title = "Detail - ${hero.name}"

            val titleTextView = findViewById<TextView>(R.id.title)
            val tipeTextView = findViewById<TextView>(R.id.tipe)
            val bpTextView = findViewById<TextView>(R.id.bp)
            val dmTextView = findViewById<TextView>(R.id.dm)
            val tiketTextView = findViewById<TextView>(R.id.tiket)
            val descTextView = findViewById<TextView>(R.id.desc)
            val imghero = findViewById<ImageView>(R.id.images)
            val imgrole = findViewById<ImageView>(R.id.role)


            titleTextView.text = hero.name
            tipeTextView.text = hero.role
            bpTextView.text = hero.bp
            dmTextView.text = hero.dm
            tiketTextView.text = hero.tiket
            descTextView.text = hero.story
            val imageResId = resources.getIdentifier(hero.imageRes, "drawable", packageName)
            if (imageResId != 0) {
                imghero.setImageResource(imageResId)
            } else {
                imghero.setImageResource(R.drawable.hero) // Gambar default jika tidak ditemukan
            }
            // Set role image dynamically
            val roleImageResId = resources.getIdentifier(hero.rolee, "drawable", packageName)
            if (roleImageResId != 0) {
                imgrole.setImageResource(roleImageResId)
            } else {
                imgrole.setImageResource(R.drawable.hero) // Gambar default jika tidak ditemukan
            }

        } else {
            // Handle the case where hero is null
            // You might want to show a message or finish the activity
            finish() // or show a Toast message
        }
    }

    // Metode untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}