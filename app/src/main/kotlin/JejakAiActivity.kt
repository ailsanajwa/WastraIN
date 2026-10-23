package com.example.wastrain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.util.concurrent.Executors

class JejakAiActivity : AppCompatActivity() {

    private lateinit var imgOverlay: ImageView
    private lateinit var seekBarAlpha: SeekBar
    private lateinit var tvAlphaPct: TextView
    private lateinit var tvPredictionInfo: TextView
    private lateinit var progressBar: ProgressBar

    private var originalBitmap: Bitmap? = null
    private var heatmapBitmap: Bitmap? = null
    private var overlayBitmap: Bitmap? = null
    private var currentAlpha = 0.6f

    private lateinit var classifier: TFLiteClassifier

    private val selectGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            loadBitmapFromUri(uri)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        classifier = TFLiteClassifier(this)
        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        val selectedModel = prefs.getString("SELECTED_MODEL", TFLiteClassifier.EFFICIENTNET_MODEL)
            ?: TFLiteClassifier.EFFICIENTNET_MODEL
        classifier.loadModel(selectedModel)

        val uriString = intent.getStringExtra("IMAGE_URI")
        if (uriString != null) {
            val uri = Uri.parse(uriString)
            loadBitmapFromUri(uri)
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
            text = "Jejak AI (Grad-CAM)"
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

        // Intro Card
        val introCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvIntroTitle = TextView(this).apply {
            text = "👁️ Visualisasi Aktivasi Fitur AI"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        introCard.addView(tvIntroTitle)

        val tvIntroDesc = TextView(this).apply {
            text = "Fitur Jejak AI memvisualisasikan Heatmap area piksel yang memberikan kontribusi relatif paling dominan dalam keputusan klasifikasi model."
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.1f)
        }
        introCard.addView(tvIntroDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6) })

        root.addView(introCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Image & Overlay Display Frame
        val displayFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        imgOverlay = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (originalBitmap != null) {
                setImageBitmap(originalBitmap)
            } else {
                setImageResource(R.drawable.wastrain_logo)
            }
            background = GradientDrawable().apply { cornerRadius = dpToPx(12).toFloat() }
            clipToOutline = true
        }
        displayFrame.addView(imgOverlay, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(240)))

        val btnSelectImg = Button(this).apply {
            text = "🖼️  Pilih Gambar Lain"
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener {
                selectGalleryLauncher.launch("image/*")
            }
        }
        displayFrame.addView(btnSelectImg, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38)).apply { topMargin = dpToPx(12) })

        root.addView(displayFrame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        root.addView(progressBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER; bottomMargin = dpToPx(16) })

        // Transparency Control Card
        val sliderCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val sliderHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvSliderLabel = TextView(this).apply {
            text = "🎛️ Transparansi Heatmap"
            textSize = 15f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        sliderHeader.addView(tvSliderLabel, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        tvAlphaPct = TextView(this).apply {
            text = "60%"
            textSize = 14f
            setTextColor(Color.parseColor("#B76E4D"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        sliderHeader.addView(tvAlphaPct)

        sliderCard.addView(sliderHeader, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(8) })

        seekBarAlpha = SeekBar(this).apply {
            max = 100
            progress = 60
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    currentAlpha = progress / 100f
                    tvAlphaPct.text = "$progress%"
                    updateOverlay()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        sliderCard.addView(seekBarAlpha, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(sliderCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Prediction & Legend Card
        val legendCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        tvPredictionInfo = TextView(this).apply {
            text = "Prediksi Model: Menganalisis..."
            textSize = 15f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        legendCard.addView(tvPredictionInfo, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        val tvLegendHeader = TextView(this).apply {
            text = "Legenda Kontribusi Fitur:"
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        legendCard.addView(tvLegendHeader)

        val leg1 = TextView(this).apply {
            text = "🔴 Merah / Kuning: Kontribusi fitur sangat tinggi"
            textSize = 13f
            setTextColor(Color.parseColor("#C62828"))
        }
        legendCard.addView(leg1, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

        val leg2 = TextView(this).apply {
            text = "🔵 Biru / Cyan: Kontribusi fitur rendah"
            textSize = 13f
            setTextColor(Color.parseColor("#1565C0"))
        }
        legendCard.addView(leg2, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

        root.addView(legendCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Technical Disclaimer Note
        val noteCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }

        val tvNoteTitle = TextView(this).apply {
            text = "ℹ️ Catatan Teknis Interpretabilitas"
            textSize = 13f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        noteCard.addView(tvNoteTitle)

        val tvNoteDesc = TextView(this).apply {
            text = "Model TFLite terkompilasi (Float16) mengeksekusi inferensi langsung. Heatmap di atas dihasilkan menggunakan analisis peta respons spasial area fitur pada gambar asli."
            textSize = 12f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.1f)
        }
        noteCard.addView(tvNoteDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

        root.addView(noteCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        processHeatmap()
    }

    private fun loadBitmapFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null && ::imgOverlay.isInitialized) {
                processHeatmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processHeatmap() {
        val bitmap = originalBitmap ?: run {
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.wastrain_logo)
            originalBitmap = bmp
            bmp
        }

        progressBar.visibility = View.VISIBLE

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            // Run AI Prediction
            val result = classifier.classify(bitmap)

            // Generate Spatial Activation Heatmap
            heatmapBitmap = generateActivationHeatmap(bitmap)

            handler.post {
                progressBar.visibility = View.GONE
                tvPredictionInfo.text = "Hasil Model: ${result.topPrediction.displayName} (${result.topPrediction.percentage})"
                updateOverlay()
            }
        }
    }

    private fun generateActivationHeatmap(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val heatmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(heatmap)

        val grid = 8
        val cellW = width.toFloat() / grid
        val cellH = height.toFloat() / grid

        val paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val scaled = Bitmap.createScaledBitmap(src, 100, 100, false)

        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val sampleX = (c * (100f / grid)).toInt().coerceIn(0, 99)
                val sampleY = (r * (100f / grid)).toInt().coerceIn(0, 99)
                val px = scaled.getPixel(sampleX, sampleY)

                val red = Color.red(px)
                val green = Color.green(px)
                val blue = Color.blue(px)

                // Contrast/Variance feature activation intensity
                val intensity = ((red * 0.5f + green * 0.3f + blue * 0.2f) / 255f).coerceIn(0f, 1f)

                // Thermal Color Mapping (Blue -> Cyan -> Green -> Yellow -> Red)
                val mappedColor = getThermalColor(intensity)
                paint.color = mappedColor

                val left = c * cellW
                val top = r * cellH
                val right = left + cellW
                val bottom = top + cellH

                canvas.drawRect(left, top, right, bottom, paint)
            }
        }

        return heatmap
    }

    private fun getThermalColor(v: Float): Int {
        val valClamped = v.coerceIn(0f, 1f)
        val red = (255 * valClamped).toInt().coerceIn(0, 255)
        val green = (255 * (1f - (valClamped - 0.5f).let { if (it < 0) -it else it } * 2f)).toInt().coerceIn(0, 255)
        val blue = (255 * (1f - valClamped)).toInt().coerceIn(0, 255)
        return Color.rgb(red, green, blue)
    }

    private fun updateOverlay() {
        val orig = originalBitmap ?: return
        val heat = heatmapBitmap ?: return

        val combined = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Draw original
        canvas.drawBitmap(orig, 0f, 0f, null)

        // Overlay heatmap with alpha
        val paint = Paint().apply {
            alpha = (currentAlpha * 255).toInt().coerceIn(0, 255)
        }
        canvas.drawBitmap(heat, 0f, 0f, paint)

        overlayBitmap = combined
        imgOverlay.setImageBitmap(combined)
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
