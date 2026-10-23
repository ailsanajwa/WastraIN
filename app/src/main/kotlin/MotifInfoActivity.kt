package com.example.wastrain

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MotifInfoActivity : AppCompatActivity() {

    private lateinit var appDb: AppDatabaseHelper
    private lateinit var btnFavToggle: ImageButton

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDb = AppDatabaseHelper(this)

        val motifId = intent.getStringExtra("MOTIF_ID") ?: "batik-kawung"
        val motif = MotifDataRepository.getById(motifId) ?: MotifDataRepository.getByName(motifId)

        if (motif == null) {
            Toast.makeText(this, "Informasi motif tidak ditemukan.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FBF5EE"))
        }

        // Top App Bar
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            setBackgroundColor(Color.WHITE)
            elevation = dpToPx(2).toFloat()
        }

        val btnBack = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setColorFilter(Color.parseColor("#704331"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        topBar.addView(btnBack, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

        val tvTitle = TextView(this).apply {
            text = "Informasi Motif"
            textSize = 20f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        topBar.addView(tvTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        // Favorite Toggle Icon Button
        var isFav = appDb.isFavorite(motif.id)
        btnFavToggle = ImageButton(this).apply {
            setImageResource(if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            setColorFilter(Color.parseColor("#B76E4D"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                isFav = appDb.toggleFavorite(motif.id, motif.name, motif.origin, motif.category)
                setImageResource(if (isFav) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
                val msg = if (isFav) "Ditambahkan ke Favorit" else "Dihapus dari Favorit"
                Toast.makeText(this@MotifInfoActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
        topBar.addView(btnFavToggle, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)).apply { marginEnd = dpToPx(8) })

        val btnShare = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_share)
            setColorFilter(Color.parseColor("#704331"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                val shareText = "Informasi Motif ${motif.name} (${motif.origin}):\n\nFilosofi: ${motif.philosophy}\n\nPelajari batik Indonesia di aplikasi WastraIN!"
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Informasi Motif"))
            }
        }
        topBar.addView(btnShare, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

        mainLayout.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Scrollable Content
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(32))
        }

        // Hero Card
        val heroCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(20))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(20).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        // Real Batik Image
        val resId = MotifDataRepository.getDrawableResId(this, motif.id)
        val imgHero = ImageView(this).apply {
            setImageResource(resId)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(14).toFloat()
            }
            clipToOutline = true
        }
        heroCard.addView(imgHero, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(220)))

        val tvMotifName = TextView(this).apply {
            text = motif.name
            textSize = 26f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        heroCard.addView(
            tvMotifName,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(14) }
        )

        val tagsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val tvOriginTag = TextView(this).apply {
            text = "📍 ${motif.origin}"
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FBF5EE"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }
        tagsLayout.addView(tvOriginTag, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dpToPx(6) })

        val tvCatTag = TextView(this).apply {
            text = "🏷️ ${motif.category}"
            textSize = 13f
            setTextColor(Color.parseColor("#2F1D18"))
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }
        tagsLayout.addView(tvCatTag)

        heroCard.addView(
            tagsLayout,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(10) }
        )

        root.addView(
            heroCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(20) }
        )

        // Visual Characteristics Card
        val traitCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvTraitHeader = TextView(this).apply {
            text = "🎨 Ciri Visual Motif"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        traitCard.addView(tvTraitHeader)

        val tvTraitBody = TextView(this).apply {
            text = motif.visualCharacteristics
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.2f)
        }
        traitCard.addView(
            tvTraitBody,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        )

        root.addView(
            traitCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        )

        // Philosophy Card
        val philCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvPhilHeader = TextView(this).apply {
            text = "📜 Filosofi & Makna Budaya"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        philCard.addView(tvPhilHeader)

        val tvPhilBody = TextView(this).apply {
            text = motif.philosophy
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.2f)
        }
        philCard.addView(
            tvPhilBody,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        )

        root.addView(
            philCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(24) }
        )

        // Share Button
        val btnShareBottom = Button(this).apply {
            text = "🔗  Bagikan Informasi Motif"
            textSize = 15f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#704331"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                val shareText = "Informasi Motif ${motif.name} (${motif.origin}):\n\nFilosofi: ${motif.philosophy}\n\nPelajari batik Indonesia di aplikasi WastraIN!"
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Informasi Motif"))
            }
        }
        root.addView(
            btnShareBottom,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            )
        )

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)
    }
}
