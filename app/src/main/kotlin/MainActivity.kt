package com.example.wastrain

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var appDb: AppDatabaseHelper
    private var currentTab = 0 // 0: Beranda, 1: Klasifikasi, 2: Riwayat, 3: Favorit, 4: Profil
    private lateinit var tabContainer: FrameLayout
    private lateinit var navBar: LinearLayout

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private val selectGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val intent = Intent(this, ClassificationResultActivity::class.java).apply {
                putExtra("IMAGE_URI", uri.toString())
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDb = AppDatabaseHelper(this)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FAF3E8")) // Cream background
        }

        // Tab Content Area
        tabContainer = FrameLayout(this).apply {
            id = View.generateViewId()
        }
        mainLayout.addView(tabContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // Bottom Navigation Bar
        navBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            elevation = dpToPx(6).toFloat()
            weightSum = 5f
        }

        mainLayout.addView(navBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(60)))

        setContentView(mainLayout)

        currentTab = 0
        renderBottomNav()
        renderHomeView()
    }

    override fun onResume() {
        super.onResume()
        currentTab = 0
        renderBottomNav()
    }

    private fun switchTab(tabIndex: Int) {
        currentTab = tabIndex
        renderBottomNav()

        when (tabIndex) {
            0 -> renderHomeView()
            1 -> startActivity(Intent(this, ClassificationActivity::class.java))
            2 -> startActivity(Intent(this, HistoryActivity::class.java))
            3 -> startActivity(Intent(this, FavoritesActivity::class.java))
            4 -> startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun renderBottomNav() {
        navBar.removeAllViews()

        val navItems = listOf(
            Triple("Beranda", android.R.drawable.ic_menu_today, 0),
            Triple("Klasifikasi", android.R.drawable.ic_menu_camera, 1),
            Triple("Riwayat", android.R.drawable.ic_menu_recent_history, 2),
            Triple("Favorit", android.R.drawable.btn_star_big_on, 3),
            Triple("Profil", android.R.drawable.ic_menu_myplaces, 4)
        )

        for ((label, iconRes, index) in navItems) {
            val isSelected = (index == currentTab)

            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(6), 0, dpToPx(6))
                setOnClickListener { switchTab(index) }
            }

            // Top active indicator line
            val indicator = View(this).apply {
                setBackgroundColor(if (isSelected) Color.parseColor("#70452F") else Color.TRANSPARENT)
            }
            itemLayout.addView(indicator, LinearLayout.LayoutParams(dpToPx(28), dpToPx(3)).apply { bottomMargin = dpToPx(4) })

            val icon = ImageView(this).apply {
                setImageResource(iconRes)
                setColorFilter(if (isSelected) Color.parseColor("#70452F") else Color.parseColor("#927D6E"))
            }
            itemLayout.addView(icon, LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)))

            val tvLabel = TextView(this).apply {
                text = label
                textSize = 11f
                setTextColor(if (isSelected) Color.parseColor("#70452F") else Color.parseColor("#927D6E"))
                if (isSelected) typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            itemLayout.addView(tvLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            navBar.addView(itemLayout, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun renderHomeView() {
        tabContainer.removeAllViews()

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#FAF3E8"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(28))
        }

        // 1. HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logoHeader = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        header.addView(logoHeader, LinearLayout.LayoutParams(dpToPx(38), dpToPx(38)).apply { marginEnd = dpToPx(12) })

        val greetingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingText = when (hour) {
            in 4..11 -> "Selamat Pagi ☀️"
            in 12..15 -> "Selamat Siang 🌤️"
            in 16..18 -> "Selamat Sore 🌇"
            else -> "Selamat Malam 🌙"
        }

        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("USER_EMAIL", "") ?: ""
        val displayName = if (userEmail.isEmpty() || userEmail.equals("tamu@wastrain.id", ignoreCase = true)) {
            "Tamu"
        } else if (userEmail.contains("@")) {
            userEmail.substringBefore("@").capitalizeWords()
        } else {
            userEmail.capitalizeWords()
        }

        val tvGreeting = TextView(this).apply {
            text = greetingText
            textSize = 12f
            setTextColor(Color.parseColor("#927D6E"))
        }
        greetingLayout.addView(tvGreeting)

        val tvUser = TextView(this).apply {
            text = displayName
            textSize = 18f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        greetingLayout.addView(tvUser)

        header.addView(greetingLayout, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val btnNotif = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_popup_reminder)
            setColorFilter(Color.parseColor("#70452F"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Tidak ada notifikasi baru.", Toast.LENGTH_SHORT).show()
            }
        }
        header.addView(btnNotif, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

        root.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // 2. HERO CARD (Dark Brown background)
        val heroCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(20), dpToPx(18), dpToPx(16), dpToPx(18))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#2F1B14")) // Cokelat gelap
                cornerRadius = dpToPx(18).toFloat()
            }
        }

        val heroContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tvHeroTitle = TextView(this).apply {
            text = "Kenali Motif Batik dengan AI"
            textSize = 19f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        heroContent.addView(tvHeroTitle)

        val tvHeroDesc = TextView(this).apply {
            text = "Ambil foto atau pilih gambar dari galeri untuk analisis motif & filosofi."
            textSize = 13f
            setTextColor(Color.parseColor("#F1E1D2"))
        }
        heroContent.addView(tvHeroDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6); bottomMargin = dpToPx(14) })

        val btnStartClassify = Button(this).apply {
            text = "Mulai Klasifikasi  ➔"
            textSize = 13f
            setTextColor(Color.parseColor("#70452F"))
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FAF3E8"))
                cornerRadius = dpToPx(15).toFloat()
            }
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ClassificationActivity::class.java))
            }
        }
        heroContent.addView(btnStartClassify, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38)))

        heroCard.addView(heroContent, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val imgHeroLogo = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        heroCard.addView(imgHeroLogo, LinearLayout.LayoutParams(dpToPx(72), dpToPx(72)).apply { marginStart = dpToPx(8) })

        root.addView(heroCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // 3. KAMERA DAN GALERI (Quick Action Buttons)
        val quickMenuRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnQuickCam = Button(this).apply {
            text = "Kamera Batik"
            textSize = 15f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#70452F")) // Primary Brown
                cornerRadius = dpToPx(12).toFloat()
            }
            elevation = dpToPx(2).toFloat()
            setOnClickListener {
                startActivity(Intent(this@MainActivity, CameraActivity::class.java))
            }
        }
        quickMenuRow.addView(btnQuickCam, LinearLayout.LayoutParams(0, dpToPx(50), 1f).apply { marginEnd = dpToPx(6) })

        val btnQuickGal = Button(this).apply {
            text = "Galeri Foto"
            textSize = 15f
            setTextColor(Color.parseColor("#70452F"))
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#70452F"))
            }
            elevation = dpToPx(2).toFloat()
            setOnClickListener {
                selectGalleryLauncher.launch("image/*")
            }
        }
        quickMenuRow.addView(btnQuickGal, LinearLayout.LayoutParams(0, dpToPx(50), 1f).apply { marginStart = dpToPx(6) })

        root.addView(quickMenuRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(24) })

        // 4. JELAJAHI WASTRA
        val tvSectionJelajah = TextView(this).apply {
            text = "Jelajahi Wastra"
            textSize = 20f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(tvSectionJelajah, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        val jelajahRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnKatalog = createMenuCard("Informasi Motif", "Katalog 20 Motif") {
            startActivity(Intent(this, CollectionActivity::class.java))
        }
        jelajahRow1.addView(btnKatalog, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dpToPx(6) })

        val btnHistory = createMenuCard("Riwayat Klarifikasi", "Scan Tersimpan") {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        jelajahRow1.addView(btnHistory, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dpToPx(6) })

        root.addView(jelajahRow1, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        val jelajahRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnFavorites = createMenuCard("Motif Favorit", "Koleksi Saya") {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        jelajahRow2.addView(btnFavorites, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dpToPx(6) })

        val btnGuide = createMenuCard("Profil & Settings", "Opsi Model AI") {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        jelajahRow2.addView(btnGuide, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dpToPx(6) })

        root.addView(jelajahRow2, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(24) })

        // 5. MOTIF POPULER NUSANTARA
        val popHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvPopTitle = TextView(this).apply {
            text = "Motif Populer Nusantara"
            textSize = 18f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        popHeader.addView(tvPopTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val tvSeeAll = TextView(this).apply {
            text = "Lihat Semua ➔"
            textSize = 13f
            setTextColor(Color.parseColor("#927D6E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                startActivity(Intent(this@MainActivity, CollectionActivity::class.java))
            }
        }
        popHeader.addView(tvSeeAll)

        root.addView(popHeader, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        val popScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val popContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val popularMotifs = MotifDataRepository.motifs.take(6)
        for (m in popularMotifs) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(14).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
                }
                setOnClickListener {
                    val intent = Intent(this@MainActivity, MotifInfoActivity::class.java).apply {
                        putExtra("MOTIF_ID", m.id)
                    }
                    startActivity(intent)
                }
            }

            val resId = MotifDataRepository.getDrawableResId(this, m.id)
            val imgMotif = ImageView(this).apply {
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(10).toFloat()
                }
                clipToOutline = true
            }
            card.addView(imgMotif, LinearLayout.LayoutParams(dpToPx(130), dpToPx(95)))

            val tvMName = TextView(this).apply {
                text = m.name
                textSize = 14f
                setTextColor(Color.parseColor("#35251E"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            card.addView(tvMName, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

            val tvMOrigin = TextView(this).apply {
                text = "📍 ${m.origin}"
                textSize = 12f
                setTextColor(Color.parseColor("#70452F"))
            }
            card.addView(tvMOrigin, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            popContainer.addView(
                card,
                LinearLayout.LayoutParams(dpToPx(150), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dpToPx(12) }
            )
        }

        popScroll.addView(popContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(popScroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        tabContainer.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun createMenuCard(title: String, subtitle: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(14).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
            setOnClickListener { onClick() }

            val tvTitle = TextView(this@MainActivity).apply {
                text = title
                textSize = 15f
                setTextColor(Color.parseColor("#35251E"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            addView(tvTitle)

            val tvSub = TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 12f
                setTextColor(Color.parseColor("#927D6E"))
            }
            addView(tvSub, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })
        }
    }

    private fun String.capitalizeWords(): String {
        return this.split(" ", "-").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
