package id.vincent.neoz

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
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
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val textToolbar = findViewById<TextView>(R.id.texttoolbar)
        val poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppinsbold)
        textToolbar.typeface = poppinsSemiBold

        // Ambil data patch dari intent
        val patchData = intent.getParcelableExtra<patch.UPatch>("PATCH_DATA")

        patchData?.let { patch ->
            textToolbar.text = "Patch - ${patch.titleHero}"

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
                    patchLinearLayout.setBackgroundResource(R.drawable.penyesuaian)
                    textPatchTextView.setTextColor(ContextCompat.getColor(this, R.color.font))
                }
                "new" -> {
                    patchLinearLayout.setBackgroundResource(R.drawable.neww)
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

            // Set atribut, passive, skill1, skill2, skill3, ultimate jika ada
            setTextOrHide(R.id.atribut, patch.atribute)
            setTextOrHide(R.id.pasif, patch.passive)
            setTextOrHide(R.id.skill1, patch.skill1)
            setTextOrHide(R.id.skill2, patch.skill2)
            setTextOrHide(R.id.skill3, patch.skill3)
            setTextOrHide(R.id.skill4, patch.skill4)
            setTextOrHide(R.id.ultimate, patch.ultimate)
        }

        // Tangani tombol back di toolbar
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setTextOrHide(viewId: Int, text: String) {
        val textView = findViewById<TextView>(viewId)
        val parentLayout = textView.parent as LinearLayout

        if (text.isEmpty()) {
            parentLayout.visibility = LinearLayout.GONE // Sembunyikan LinearLayout jika kosong
        } else {
            textView.text = text
            parentLayout.visibility = LinearLayout.VISIBLE // Tampilkan LinearLayout jika ada teks
        }
    }

    // Metode untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}