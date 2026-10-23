package com.example.wastrain

import android.content.Intent
import android.graphics.BitmapFactory
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: HistoryDatabaseHelper
    private lateinit var containerLayout: LinearLayout
    private lateinit var emptyView: LinearLayout

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = HistoryDatabaseHelper(this)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(255, 247, 240))
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
            setColorFilter(Color.rgb(67, 38, 28))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        topBar.addView(btnBack, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

        val tvTitle = TextView(this).apply {
            text = "Riwayat Klasifikasi"
            textSize = 20f
            setTextColor(Color.rgb(67, 38, 28))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        topBar.addView(tvTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val btnClearAll = Button(this).apply {
            text = "Hapus Semua"
            textSize = 12f
            setTextColor(Color.rgb(180, 40, 30))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.rgb(255, 235, 235))
                cornerRadius = dpToPx(8).toFloat()
            }
            setOnClickListener {
                confirmClearAll()
            }
        }
        topBar.addView(btnClearAll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36)))

        mainLayout.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Empty View
        emptyView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(64), dpToPx(32), dpToPx(64))
            visibility = View.GONE
        }

        val tvEmptyTitle = TextView(this).apply {
            text = "📜 Belum Ada Riwayat"
            textSize = 20f
            setTextColor(Color.rgb(135, 76, 55))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        emptyView.addView(tvEmptyTitle)

        val tvEmptyDesc = TextView(this).apply {
            text = "Hasil scan batik yang Anda simpan akan muncul di sini."
            textSize = 14f
            setTextColor(Color.rgb(140, 110, 100))
            gravity = Gravity.CENTER
        }
        emptyView.addView(tvEmptyDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

        mainLayout.addView(emptyView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Scrollable List Container
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

        loadHistoryList()
    }

    private fun loadHistoryList() {
        containerLayout.removeAllViews()
        val list = dbHelper.getAllHistory()

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
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(14).toFloat()
                    setStroke(dpToPx(1), Color.rgb(230, 215, 205))
                }
            }

            // Thumbnail
            val imgThumb = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                }
                clipToOutline = true

                val imgFile = File(item.imagePath)
                if (imgFile.exists()) {
                    setImageBitmap(BitmapFactory.decodeFile(imgFile.absolutePath))
                } else {
                    setImageResource(R.drawable.wastrain_logo)
                }
            }
            card.addView(imgThumb, LinearLayout.LayoutParams(dpToPx(70), dpToPx(70)))

            // Content
            val contentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(12), 0, dpToPx(8), 0)
            }

            val tvMotif = TextView(this).apply {
                text = item.motifName
                textSize = 17f
                setTextColor(Color.rgb(67, 38, 28))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            contentLayout.addView(tvMotif)

            val tvPct = TextView(this).apply {
                text = "Akurasi: ${item.percentage} (${item.modelName})"
                textSize = 13f
                setTextColor(Color.rgb(135, 76, 55))
            }
            contentLayout.addView(tvPct, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            val tvDate = TextView(this).apply {
                text = item.timestamp
                textSize = 12f
                setTextColor(Color.rgb(160, 130, 120))
            }
            contentLayout.addView(tvDate, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

            card.addView(contentLayout, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            // Delete Button
            val btnDelete = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.rgb(200, 60, 50))
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    confirmDeleteItem(item)
                }
            }
            card.addView(btnDelete, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))

            // Card click to detail
            card.setOnClickListener {
                val motifInfo = MotifDataRepository.getByName(item.motifId)
                if (motifInfo != null) {
                    val intent = Intent(this, MotifInfoActivity::class.java).apply {
                        putExtra("MOTIF_ID", motifInfo.id)
                    }
                    startActivity(intent)
                }
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

    private fun confirmDeleteItem(item: HistoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus '${item.motifName}' dari riwayat?")
            .setPositiveButton("Hapus") { _, _ ->
                dbHelper.deleteHistory(item.id)
                loadHistoryList()
                Toast.makeText(this, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat scan?")
            .setPositiveButton("Hapus Semua") { _, _ ->
                dbHelper.clearAllHistory()
                loadHistoryList()
                Toast.makeText(this, "Seluruh riwayat dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
