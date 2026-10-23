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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private var currentPage = 0
    private lateinit var contentContainer: LinearLayout
    private lateinit var dotsContainer: LinearLayout
    private lateinit var btnAction: Button
    private lateinit var btnSkip: TextView

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("ONBOARDING_COMPLETED", false)) {
            navigateToNext()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FBF5EE"))
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(32))
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        btnSkip = TextView(this).apply {
            text = "Lewati"
            textSize = 15f
            setTextColor(Color.parseColor("#704331"))
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            setOnClickListener { finishOnboarding() }
        }
        topBar.addView(btnSkip, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        root.addView(contentContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        dotsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        root.addView(
            dotsContainer,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dpToPx(24)
            }
        )

        btnAction = Button(this).apply {
            text = "Selanjutnya"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#704331"))
                cornerRadius = dpToPx(14).toFloat()
            }
            setOnClickListener {
                if (currentPage < 2) {
                    currentPage++
                    renderPage()
                } else {
                    finishOnboarding()
                }
            }
        }
        root.addView(btnAction, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(52)))

        setContentView(root)
        renderPage()
    }

    private fun renderPage() {
        contentContainer.removeAllViews()

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        when (currentPage) {
            0 -> {
                btnSkip.visibility = View.VISIBLE
                btnAction.text = "Selanjutnya"

                contentContainer.addView(
                    logo,
                    LinearLayout.LayoutParams(dpToPx(180), dpToPx(180)).apply { bottomMargin = dpToPx(24) }
                )

                val sub = TextView(this).apply {
                    text = "Wawasan Nusantara Indonesia"
                    textSize = 20f
                    setTextColor(Color.parseColor("#B76E4D"))
                    gravity = Gravity.CENTER
                }
                contentContainer.addView(sub, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(16) })

                val desc = TextView(this).apply {
                    text = "Kenali, Pelajari, dan Lestarikan Wastra Nusantara dengan Teknologi Kecerdasan Buatan (AI)."
                    textSize = 15f
                    setTextColor(Color.parseColor("#704331"))
                    gravity = Gravity.CENTER
                }
                contentContainer.addView(desc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            1 -> {
                btnSkip.visibility = View.VISIBLE
                btnAction.text = "Selanjutnya"

                contentContainer.addView(
                    logo,
                    LinearLayout.LayoutParams(dpToPx(160), dpToPx(160)).apply { bottomMargin = dpToPx(24) }
                )

                val title = TextView(this).apply {
                    text = "Kenali Motif Nusantara"
                    textSize = 26f
                    setTextColor(Color.parseColor("#2F1D18"))
                    gravity = Gravity.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                contentContainer.addView(title)

                val desc = TextView(this).apply {
                    text = "Cukup ambil foto kain batik menggunakan kamera smartphone Anda atau pilih dari galeri untuk analisis motif instan."
                    textSize = 15f
                    setTextColor(Color.parseColor("#704331"))
                    gravity = Gravity.CENTER
                }
                contentContainer.addView(desc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(12) })
            }
            2 -> {
                btnSkip.visibility = View.INVISIBLE
                btnAction.text = "Mulai Sekarang"

                contentContainer.addView(
                    logo,
                    LinearLayout.LayoutParams(dpToPx(160), dpToPx(160)).apply { bottomMargin = dpToPx(24) }
                )

                val title = TextView(this).apply {
                    text = "Jelajahi Kekayaan Budaya"
                    textSize = 26f
                    setTextColor(Color.parseColor("#2F1D18"))
                    gravity = Gravity.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                contentContainer.addView(title)

                val desc = TextView(this).apply {
                    text = "Temukan katalog lengkap 20 motif batik khas daerah, pahami makna filosofinya, dan simpan riwayat scan Anda secara lokal."
                    textSize = 15f
                    setTextColor(Color.parseColor("#704331"))
                    gravity = Gravity.CENTER
                }
                contentContainer.addView(desc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(12) })
            }
        }

        renderDots()
    }

    private fun renderDots() {
        dotsContainer.removeAllViews()
        for (i in 0..2) {
            val dot = View(this).apply {
                val isCurrent = (i == currentPage)
                background = GradientDrawable().apply {
                    setColor(if (isCurrent) Color.parseColor("#704331") else Color.parseColor("#E9D8C9"))
                    cornerRadius = dpToPx(4).toFloat()
                }
            }
            val width = if (i == currentPage) dpToPx(24) else dpToPx(8)
            dotsContainer.addView(
                dot,
                LinearLayout.LayoutParams(width, dpToPx(8)).apply {
                    setMargins(dpToPx(4), 0, dpToPx(4), 0)
                }
            )
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("ONBOARDING_COMPLETED", true).apply()
        navigateToNext()
    }

    private fun navigateToNext() {
        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)
        val targetClass = if (isLoggedIn) MainActivity::class.java else LoginActivity::class.java
        val intent = Intent(this, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
