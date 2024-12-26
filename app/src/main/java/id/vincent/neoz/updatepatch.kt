package id.vincent.neoz

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable

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

            // Set gambar menggunakan Glide
            val heroImageBaseUrl = "https://raw.githubusercontent.com/Mikaelazzz/assets/master/img/hero/"

            Glide.with(this)
                .load("$heroImageBaseUrl${patch.imageHero}.png")
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imageView)

            // Set background patch
            val patchUrl = "$heroImageBaseUrl${patch.patch}.png"

            Glide.with(this)
                .load(patchUrl)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        patchLinearLayout.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Implementasi jika diperlukan
                    }
                })

            // Set atribut, passive, skill1, skill2, skill3, ultimate jika ada
            setTextOrHide(R.id.atribut, patch.atribute)
            setTextOrHide(R.id.pasif, patch.passive)
            setTextOrHide(R.id.skill1, patch.skill1)
            setTextOrHide(R.id.skill2, patch.skill2)
            setTextOrHide(R.id.skill3, patch.skill3)
            setTextOrHide(R.id.skill4, patch.skill4)
            setTextOrHide(R.id.ultimate, patch.ultimate)

            // Set background untuk patch berdasarkan data
            val patchBackgroundResId = resources.getIdentifier(
                patch.patch, "drawable", packageName
            )
            patchLinearLayout.setBackgroundResource(patchBackgroundResId)
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