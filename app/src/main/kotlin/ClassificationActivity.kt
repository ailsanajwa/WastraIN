package com.example.wastrain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.InputStream

class ClassificationActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var btnClassify: Button
    private lateinit var btnClear: ImageButton
    private lateinit var tvActiveModel: TextView

    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null

    private val selectGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImagePath = null
            showPreview(uri)
        }
    }

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
            text = "Klasifikasi Motif AI"
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

        // Active Model Badge
        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        val currentModel = prefs.getString("SELECTED_MODEL", TFLiteClassifier.EFFICIENTNET_MODEL)
        val modelDisplayName = if (currentModel?.contains("efficientnet", ignoreCase = true) == true) "EfficientNetV2-B0" else "MobileNetV3-Large"

        tvActiveModel = TextView(this).apply {
            text = "🤖 Model AI Aktif: $modelDisplayName"
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }
        root.addView(tvActiveModel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER; bottomMargin = dpToPx(16) })

        // Image Preview Container Box
        val previewFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(18).toFloat()
                setStroke(dpToPx(2), Color.parseColor("#E9D8C9"))
            }
        }

        placeholderLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24))
        }

        val iconPlaceholder = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        placeholderLayout.addView(iconPlaceholder, LinearLayout.LayoutParams(dpToPx(80), dpToPx(80)))

        val tvPlaceholder = TextView(this).apply {
            text = "Belum Ada Gambar Dipilih"
            textSize = 16f
            setTextColor(Color.parseColor("#704331"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        placeholderLayout.addView(tvPlaceholder, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(12) })

        val tvSubPlace = TextView(this).apply {
            text = "Gunakan kamera atau galeri untuk mengunggah foto batik"
            textSize = 13f
            setTextColor(Color.parseColor("#8A6B5C"))
            gravity = Gravity.CENTER
        }
        placeholderLayout.addView(tvSubPlace, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

        previewFrame.addView(placeholderLayout, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Image View (hidden initially)
        imgPreview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
            }
            clipToOutline = true
        }
        previewFrame.addView(imgPreview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(240)))

        root.addView(previewFrame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // Source Option Buttons (Kamera vs Galeri)
        val rowSource = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnCamera = Button(this).apply {
            text = "📷  Buka Kamera"
            textSize = 14f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#704331"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                startActivity(Intent(this@ClassificationActivity, CameraActivity::class.java))
            }
        }
        rowSource.addView(btnCamera, LinearLayout.LayoutParams(0, dpToPx(48), 1f).apply { marginEnd = dpToPx(6) })

        val btnGallery = Button(this).apply {
            text = "🖼️  Galeri Foto"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#704331"))
            }
            setOnClickListener {
                selectGalleryLauncher.launch("image/*")
            }
        }
        rowSource.addView(btnGallery, LinearLayout.LayoutParams(0, dpToPx(48), 1f).apply { marginStart = dpToPx(6) })

        root.addView(rowSource, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // Classify Button
        btnClassify = Button(this).apply {
            text = "🔍  KLASIFIKASIKAN MOTIF"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isEnabled = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#A89589")) // Disabled initial
                cornerRadius = dpToPx(14).toFloat()
            }
            setOnClickListener {
                if (selectedImageUri != null || selectedImagePath != null) {
                    val intent = Intent(this@ClassificationActivity, ClassificationResultActivity::class.java).apply {
                        if (selectedImageUri != null) putExtra("IMAGE_URI", selectedImageUri.toString())
                        if (selectedImagePath != null) putExtra("IMAGE_PATH", selectedImagePath)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ClassificationActivity, "Silakan pilih foto batik terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
        }
        root.addView(btnClassify, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(52)).apply { bottomMargin = dpToPx(24) })

        // Tips Card
        val tipsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(14).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvTipTitle = TextView(this).apply {
            text = "💡 Petunjuk Hasil Akurat"
            textSize = 15f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tipsCard.addView(tvTipTitle)

        val tvTipsBody = TextView(this).apply {
            text = "• Gunakan pencahayaan yang cukup dan terang.\n• Posisikan kain batik rata tegak lurus mengarah ke kamera.\n• Hindari bayangan gelap berlebih di atas motif."
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.2f)
        }
        tipsCard.addView(tvTipsBody, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6) })

        root.addView(tipsCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)
    }

    private fun showPreview(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap)
                imgPreview.visibility = View.VISIBLE
                placeholderLayout.visibility = View.GONE

                btnClassify.isEnabled = true
                btnClassify.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#704331"))
                    cornerRadius = dpToPx(14).toFloat()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat preview gambar", Toast.LENGTH_SHORT).show()
        }
    }
}
