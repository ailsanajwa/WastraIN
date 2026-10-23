package com.example.wastrain

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FavoritesActivity : AppCompatActivity() {

    private lateinit var appDb: AppDatabaseHelper
    private lateinit var containerLayout: LinearLayout
    private lateinit var emptyView: LinearLayout

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
            text = "Motif Favorit Saya"
            textSize = 20f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        topBar.addView(tvTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        mainLayout.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Empty View
        emptyView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(64), dpToPx(32), dpToPx(64))
            visibility = View.GONE
        }

        val tvEmptyTitle = TextView(this).apply {
            text = "❤️ Belum Ada Favorit"
            textSize = 20f
            setTextColor(Color.parseColor("#704331"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        emptyView.addView(tvEmptyTitle)

        val tvEmptyDesc = TextView(this).apply {
            text = "Jelajahi koleksi motif batik dan tekan ikon hati pada motif pilihan Anda untuk menyimpannya di sini."
            textSize = 14f
            setTextColor(Color.parseColor("#8A6B5C"))
            gravity = Gravity.CENTER
        }
        emptyView.addView(tvEmptyDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

        mainLayout.addView(emptyView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Scrollable List
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(24))
        }

        scrollView.addView(containerLayout, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        containerLayout.removeAllViews()
        val list = appDb.getAllFavorites()

        if (list.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        } else {
            emptyView.visibility = View.GONE
        }

        for (item in list) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(14).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                }
            }

            val resId = MotifDataRepository.getDrawableResId(this, item.motifId)
            val imgLogo = ImageView(this).apply {
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(10).toFloat()
                }
                clipToOutline = true
            }
            card.addView(imgLogo, LinearLayout.LayoutParams(dpToPx(60), dpToPx(60)))

            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(12), 0, 0, 0)
            }

            val tvName = TextView(this).apply {
                text = item.motifName
                textSize = 17f
                setTextColor(Color.parseColor("#2F1D18"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            content.addView(tvName)

            val tvOrigin = TextView(this).apply {
                text = "📍 ${item.origin}"
                textSize = 13f
                setTextColor(Color.parseColor("#704331"))
            }
            content.addView(tvOrigin, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            val tvCat = TextView(this).apply {
                text = "Disimpan: ${item.timestamp}"
                textSize = 12f
                setTextColor(Color.parseColor("#8A6B5C"))
            }
            content.addView(tvCat, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            card.addView(content, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            val btnHeart = ImageButton(this).apply {
                setImageResource(android.R.drawable.btn_star_big_on)
                setColorFilter(Color.parseColor("#B76E4D"))
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    appDb.removeFavorite(item.motifId)
                    Toast.makeText(this@FavoritesActivity, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    loadFavorites()
                }
            }
            card.addView(btnHeart, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

            card.setOnClickListener {
                val intent = Intent(this, MotifInfoActivity::class.java).apply {
                    putExtra("MOTIF_ID", item.motifId)
                }
                startActivity(intent)
            }

            containerLayout.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(12) }
            )
        }
    }
}
