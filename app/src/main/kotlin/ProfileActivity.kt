package com.example.wastrain

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var appDb: AppDatabaseHelper
    private lateinit var tvTotalScans: TextView
    private lateinit var tvTotalFavs: TextView
    private lateinit var tvTotalExplored: TextView

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDb = AppDatabaseHelper(this)

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
            text = "Profil & Pengaturan"
            textSize = 20f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        topBar.addView(tvTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        mainLayout.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Scrollable Content
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(32))
        }

        // Profile Header Card
        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("USER_EMAIL", "pengguna@wastrain.id") ?: "pengguna@wastrain.id"

        val profileCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(20), dpToPx(24), dpToPx(20), dpToPx(24))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val avatar = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        profileCard.addView(avatar, LinearLayout.LayoutParams(dpToPx(90), dpToPx(90)))

        val tvUserName = TextView(this).apply {
            text = "Pengguna WastraIN"
            textSize = 20f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        profileCard.addView(tvUserName, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(10) })

        val tvEmail = TextView(this).apply {
            text = userEmail
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            gravity = Gravity.CENTER
        }
        profileCard.addView(tvEmail, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

        val btnEditProfile = Button(this).apply {
            text = "✏️  Edit Profil"
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(16).toFloat()
            }
            setOnClickListener {
                Toast.makeText(this@ProfileActivity, "Fitur Pengubahan Profil.", Toast.LENGTH_SHORT).show()
            }
        }
        profileCard.addView(btnEditProfile, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36)).apply { topMargin = dpToPx(12) })

        root.addView(profileCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Real Statistics Row
        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }

        // Stat 1: Total Scans
        val cardStat1 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }
        tvTotalScans = TextView(this).apply {
            text = "0"
            textSize = 22f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        cardStat1.addView(tvTotalScans)
        val lblStat1 = TextView(this).apply {
            text = "Klasifikasi"
            textSize = 12f
            setTextColor(Color.parseColor("#704331"))
        }
        cardStat1.addView(lblStat1)

        statsRow.addView(cardStat1, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dpToPx(4) })

        // Stat 2: Favorites
        val cardStat2 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }
        tvTotalFavs = TextView(this).apply {
            text = "0"
            textSize = 22f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        cardStat2.addView(tvTotalFavs)
        val lblStat2 = TextView(this).apply {
            text = "Favorit"
            textSize = 12f
            setTextColor(Color.parseColor("#704331"))
        }
        cardStat2.addView(lblStat2)

        statsRow.addView(cardStat2, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dpToPx(2); marginEnd = dpToPx(2) })

        // Stat 3: Explored
        val cardStat3 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }
        tvTotalExplored = TextView(this).apply {
            text = "0"
            textSize = 22f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        cardStat3.addView(tvTotalExplored)
        val lblStat3 = TextView(this).apply {
            text = "Dijelajahi"
            textSize = 12f
            setTextColor(Color.parseColor("#704331"))
        }
        cardStat3.addView(lblStat3)

        statsRow.addView(cardStat3, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dpToPx(4) })

        root.addView(statsRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // Model Settings Card
        val settingsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvModelHeader = TextView(this).apply {
            text = "🤖 Model AI Klasifikasi"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        settingsCard.addView(tvModelHeader)

        val savedModel = prefs.getString("SELECTED_MODEL", TFLiteClassifier.EFFICIENTNET_MODEL)

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val rbEfficientNet = RadioButton(this).apply {
            text = "EfficientNetV2-B0 (Rekomendasi - Akurasi Tinggi)"
            textSize = 14f
            setTextColor(Color.parseColor("#2F1D18"))
            id = View.generateViewId()
            isChecked = (savedModel == TFLiteClassifier.EFFICIENTNET_MODEL)
        }
        radioGroup.addView(rbEfficientNet, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

        val rbMobileNet = RadioButton(this).apply {
            text = "MobileNetV3-Large (Ringan & Cepat)"
            textSize = 14f
            setTextColor(Color.parseColor("#2F1D18"))
            id = View.generateViewId()
            isChecked = (savedModel == TFLiteClassifier.MOBILENET_MODEL)
        }
        radioGroup.addView(rbMobileNet, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selected = if (checkedId == rbEfficientNet.id) TFLiteClassifier.EFFICIENTNET_MODEL else TFLiteClassifier.MOBILENET_MODEL
            prefs.edit().putString("SELECTED_MODEL", selected).apply()
            val name = if (selected == TFLiteClassifier.EFFICIENTNET_MODEL) "EfficientNetV2-B0" else "MobileNetV3-Large"
            Toast.makeText(this, "Model aktif diubah ke $name", Toast.LENGTH_SHORT).show()
        }

        settingsCard.addView(radioGroup, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

        root.addView(settingsCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // Re-run Onboarding Button
        val btnReOnboarding = Button(this).apply {
            text = "📱  Lihat Ulang Onboarding"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                prefs.edit().apply {
                    putBoolean("ONBOARDING_COMPLETED", false)
                    apply()
                }
                val intent = Intent(this@ProfileActivity, OnboardingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
        root.addView(btnReOnboarding, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48)).apply { bottomMargin = dpToPx(12) })

        // Logout Button
        val btnLogout = Button(this).apply {
            text = "🚪  Keluar dari Akun"
            textSize = 15f
            setTextColor(Color.parseColor("#C62828"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFEBEE"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Keluar Akun")
                    .setMessage("Apakah Anda yakin ingin keluar dari WastraIN? Anda akan diarahkan ke halaman onboarding.")
                    .setPositiveButton("Keluar") { _, _ ->
                        prefs.edit().apply {
                            putBoolean("IS_LOGGED_IN", false)
                            putBoolean("ONBOARDING_COMPLETED", false)
                            apply()
                        }
                        Toast.makeText(this@ProfileActivity, "Anda telah keluar", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ProfileActivity, OnboardingActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
        root.addView(btnLogout, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)))

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        updateStats()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    private fun updateStats() {
        val historyList = appDb.getAllHistory()
        val favList = appDb.getAllFavorites()

        tvTotalScans.text = historyList.size.toString()
        tvTotalFavs.text = favList.size.toString()

        val exploredSet = mutableSetOf<String>()
        historyList.forEach { exploredSet.add(it.motifId) }
        favList.forEach { exploredSet.add(it.motifId) }
        tvTotalExplored.text = exploredSet.size.toString()
    }
}
