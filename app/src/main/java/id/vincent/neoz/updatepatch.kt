package id.vincent.neoz

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar // Gunakan import ini
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


class updatepatch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.updatepatch)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val titleTextView = toolbar.findViewById<TextView>(androidx.appcompat.R.id.action_bar_title)
        if (titleTextView != null) {
            val poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppinsbold)
            titleTextView.typeface = poppinsSemiBold
        }

        // Ambil data patch dari intent
        val patchData = intent.getParcelableExtra<patch.UPatch>("PATCH_DATA")

        patchData?.let { patch ->

            supportActionBar?.title = "Patch - ${patch.titleHero}"

            // Temukan view yang akan diupdate
            val titleTextView = findViewById<TextView>(R.id.title)
            val tipeTextView = findViewById<TextView>(R.id.tipe)
            val patchLinearLayout = findViewById<LinearLayout>(R.id.patch)
            val textPatchTextView = findViewById<TextView>(R.id.textpatch)
            val updateHeroTextView = findViewById<TextView>(R.id.updatehero)
            val imageView = findViewById<ImageView>(R.id.images)

            // Set data ke view
            titleTextView.text = patch.titleHero
            tipeTextView.text = patch.type
            textPatchTextView.text = patch.textpatch
            updateHeroTextView.text = patch.update

            // Set background dan teks untuk patch/buff berdasarkan data
            when (patch.textpatch.lowercase()) {
                "buff" -> {
                    patchLinearLayout.setBackgroundResource(R.drawable.buff)
                    textPatchTextView.setTextColor(ContextCompat.getColor(this, R.color.font))
                }
                "nerf" -> {
                    patchLinearLayout.setBackgroundResource(R.drawable.nerf)
                    textPatchTextView.setTextColor(ContextCompat.getColor(this, R.color.font))
                }
                "penyesuaian" -> {
                    // Default style jika tidak ada kategori khusus
                    patchLinearLayout.setBackgroundResource(R.drawable.penyesuaian)
                    textPatchTextView.setTextColor(ContextCompat.getColor(this, R.color.font))
                }
            }

            // Set gambar
            val imageResId = resources.getIdentifier(
                patch.imageHero,
                "drawable",
                packageName
            )
            if (imageResId != 0) {
                imageView.setImageResource(imageResId)
            }
        }
        // Tangani tombol back di toolbar
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    // Metode untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}