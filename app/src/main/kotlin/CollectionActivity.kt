package com.example.wastrain

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CollectionActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var categoryContainer: LinearLayout
    private lateinit var gridContainer: LinearLayout

    private var currentCategory = "Semua"
    private var currentQuery = ""

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            text = "Koleksi Motif Batik"
            textSize = 20f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        topBar.addView(tvTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        mainLayout.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Search Input Box
        val searchBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        searchInput = EditText(this).apply {
            hint = "🔍 Cari motif batik, daerah, filosofi..."
            textSize = 14f
            setTextColor(Color.parseColor("#2F1D18"))
            setHintTextColor(Color.parseColor("#A89589"))
            background = null
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentQuery = s.toString().trim()
                    renderMotifs()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        searchBox.addView(searchInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        mainLayout.addView(
            searchBox,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(12))
            }
        )

        // Categories Chips Horizontal ScrollView
        val categoryScrollView = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        categoryContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), 0, dpToPx(16), 0)
        }

        categoryScrollView.addView(categoryContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        mainLayout.addView(categoryScrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        renderCategoryChips()

        // Motif List Container
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        gridContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(24))
        }

        scrollView.addView(gridContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        renderMotifs()
    }

    private fun renderCategoryChips() {
        categoryContainer.removeAllViews()
        val categories = MotifDataRepository.getCategories()

        for (cat in categories) {
            val isSelected = cat.equals(currentCategory, ignoreCase = true)

            val btnChip = Button(this).apply {
                text = cat
                textSize = 13f
                isAllCaps = false
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#2F1D18"))
                background = GradientDrawable().apply {
                    setColor(if (isSelected) Color.parseColor("#704331") else Color.WHITE)
                    cornerRadius = dpToPx(20).toFloat()
                    if (!isSelected) {
                        setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                    }
                }
                setOnClickListener {
                    currentCategory = cat
                    renderCategoryChips()
                    renderMotifs()
                }
            }

            categoryContainer.addView(
                btnChip,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dpToPx(38)
                ).apply { marginEnd = dpToPx(8) }
            )
        }
    }

    private fun renderMotifs() {
        gridContainer.removeAllViews()
        val list = MotifDataRepository.search(currentQuery, currentCategory)

        if (list.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "Tidak ada motif batik yang cocok."
                textSize = 15f
                setTextColor(Color.parseColor("#704331"))
                gravity = Gravity.CENTER
            }
            gridContainer.addView(
                tvEmpty,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(32) }
            )
            return
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

            // Real Motif Image Thumbnail
            val resId = MotifDataRepository.getDrawableResId(this, item.id)
            val imgMotif = ImageView(this).apply {
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(10).toFloat()
                }
                clipToOutline = true
            }
            card.addView(imgMotif, LinearLayout.LayoutParams(dpToPx(70), dpToPx(70)))

            // Info
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(12), 0, 0, 0)
            }

            val tvName = TextView(this).apply {
                text = item.name
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
                text = "Kategori: ${item.category}"
                textSize = 12f
                setTextColor(Color.parseColor("#8A6B5C"))
            }
            content.addView(tvCat, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            card.addView(content, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            card.setOnClickListener {
                val intent = Intent(this, MotifInfoActivity::class.java).apply {
                    putExtra("MOTIF_ID", item.id)
                }
                startActivity(intent)
            }

            gridContainer.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(12) }
            )
        }
    }
}
