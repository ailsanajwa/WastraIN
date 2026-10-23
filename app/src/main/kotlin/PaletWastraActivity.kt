package com.example.wastrain

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

data class ExtractedColor(
    val colorInt: Int,
    val hexString: String,
    val percentage: Float
)

class PaletWastraActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var paletteContainer: LinearLayout
    private lateinit var combinationContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var selectedBitmap: Bitmap? = null
    private var selectedImageUri: Uri? = null

    private val selectGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            loadBitmapFromUri(uri)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriString = intent.getStringExtra("IMAGE_URI")
        if (uriString != null) {
            selectedImageUri = Uri.parse(uriString)
            loadBitmapFromUri(selectedImageUri!!)
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
            text = "Palet Wastra"
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
            text = "🎨 Ekstraksi Warna Dominan Batik"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        introCard.addView(tvIntroTitle)

        val tvIntroDesc = TextView(this).apply {
            text = "Fitur ini mengekstrak 5 warna dominan dari kain batik Anda menggunakan algoritma K-Means Clustering untuk mendapatkan kode warna HEX dan inspirasi kombinasi desain."
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.1f)
        }
        introCard.addView(tvIntroDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6) })

        root.addView(introCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Image Preview & Select
        val imageFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        imgPreview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (selectedBitmap != null) {
                setImageBitmap(selectedBitmap)
            } else {
                setImageResource(R.drawable.wastrain_logo)
            }
            background = GradientDrawable().apply { cornerRadius = dpToPx(12).toFloat() }
            clipToOutline = true
        }
        imageFrame.addView(imgPreview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(180)))

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
        imageFrame.addView(btnSelectImg, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(38)).apply { topMargin = dpToPx(12) })

        root.addView(imageFrame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        root.addView(progressBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER; bottomMargin = dpToPx(16) })

        // Palette Container
        paletteContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(paletteContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Combination Container
        combinationContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(combinationContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        extractColors()
    }

    private fun loadBitmapFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            selectedBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (selectedBitmap != null && ::imgPreview.isInitialized) {
                imgPreview.setImageBitmap(selectedBitmap)
                extractColors()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractColors() {
        val bitmap = selectedBitmap ?: run {
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.wastrain_logo)
            selectedBitmap = bmp
            imgPreview.setImageBitmap(bmp)
            bmp
        }

        progressBar.visibility = View.VISIBLE
        paletteContainer.removeAllViews()
        combinationContainer.removeAllViews()

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            val colors = kMeansColorExtraction(bitmap, 5)

            handler.post {
                progressBar.visibility = View.GONE
                renderPaletteUI(colors)
            }
        }
    }

    private fun kMeansColorExtraction(originalBitmap: Bitmap, k: Int): List<ExtractedColor> {
        val scaled = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
        val width = scaled.width
        val height = scaled.height

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val validPixels = mutableListOf<Int>()
        for (p in pixels) {
            if (Color.alpha(p) > 128) {
                validPixels.add(p)
            }
        }

        if (validPixels.isEmpty()) return emptyList()

        val centroids = mutableListOf<FloatArray>()
        val step = validPixels.size / k
        for (i in 0 until k) {
            val p = validPixels[(i * step).coerceIn(0, validPixels.size - 1)]
            centroids.add(floatArrayOf(Color.red(p).toFloat(), Color.green(p).toFloat(), Color.blue(p).toFloat()))
        }

        val assignments = IntArray(validPixels.size)
        val iterations = 8

        for (iter in 0 until iterations) {
            for (pIdx in validPixels.indices) {
                val p = validPixels[pIdx]
                val r = Color.red(p).toDouble()
                val g = Color.green(p).toDouble()
                val b = Color.blue(p).toDouble()

                var minDist = Double.MAX_VALUE
                var bestC = 0
                for (cIdx in 0 until k) {
                    val c = centroids[cIdx]
                    val dist = sqrt((r - c[0]).pow(2.0) + (g - c[1]).pow(2.0) + (b - c[2]).pow(2.0))
                    if (dist < minDist) {
                        minDist = dist
                        bestC = cIdx
                    }
                }
                assignments[pIdx] = bestC
            }

            val sumR = FloatArray(k)
            val sumG = FloatArray(k)
            val sumB = FloatArray(k)
            val counts = IntArray(k)

            for (pIdx in validPixels.indices) {
                val cluster = assignments[pIdx]
                val p = validPixels[pIdx]
                sumR[cluster] = sumR[cluster] + Color.red(p)
                sumG[cluster] = sumG[cluster] + Color.green(p)
                sumB[cluster] = sumB[cluster] + Color.blue(p)
                counts[cluster] = counts[cluster] + 1
            }

            for (cIdx in 0 until k) {
                if (counts[cIdx] > 0) {
                    centroids[cIdx][0] = sumR[cIdx] / counts[cIdx]
                    centroids[cIdx][1] = sumG[cIdx] / counts[cIdx]
                    centroids[cIdx][2] = sumB[cIdx] / counts[cIdx]
                }
            }
        }

        val resultList = mutableListOf<ExtractedColor>()
        val totalValid = validPixels.size.toFloat()

        val countsMap = IntArray(k)
        for (a in assignments) {
            countsMap[a] = countsMap[a] + 1
        }

        val indicesSorted = (0 until k).sortedByDescending { countsMap[it] }

        for (cIdx in indicesSorted) {
            val count = countsMap[cIdx]
            if (count > 0) {
                val c = centroids[cIdx]
                val r = c[0].toInt().coerceIn(0, 255)
                val g = c[1].toInt().coerceIn(0, 255)
                val b = c[2].toInt().coerceIn(0, 255)
                val colorInt = Color.rgb(r, g, b)
                val hex = String.format("#%02X%02X%02X", r, g, b)
                val pct = (count / totalValid) * 100f

                resultList.add(ExtractedColor(colorInt, hex, pct))
            }
        }

        return resultList
    }

    private fun renderPaletteUI(colors: List<ExtractedColor>) {
        paletteContainer.removeAllViews()
        combinationContainer.removeAllViews()

        if (colors.isEmpty()) {
            Toast.makeText(this, "Gagal mengekstrak warna.", Toast.LENGTH_SHORT).show()
            return
        }

        val tvSection = TextView(this).apply {
            text = "5 Warna Dominan Batik"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        paletteContainer.addView(tvSection, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        for (item in colors) {
            val swatchCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                }
            }

            val colorBox = View(this).apply {
                background = GradientDrawable().apply {
                    setColor(item.colorInt)
                    cornerRadius = dpToPx(8).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#D0C0B0"))
                }
            }
            swatchCard.addView(colorBox, LinearLayout.LayoutParams(dpToPx(44), dpToPx(44)))

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(12), 0, 0, 0)
            }

            val tvHex = TextView(this).apply {
                text = item.hexString
                textSize = 16f
                setTextColor(Color.parseColor("#2F1D18"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            infoLayout.addView(tvHex)

            val tvPct = TextView(this).apply {
                text = "Proporsi: ${String.format("%.1f%%", item.percentage)}"
                textSize = 12f
                setTextColor(Color.parseColor("#704331"))
            }
            infoLayout.addView(tvPct, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })

            swatchCard.addView(infoLayout, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            val btnCopy = Button(this).apply {
                text = "📋 Salin"
                textSize = 12f
                setTextColor(Color.parseColor("#704331"))
                isAllCaps = false
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F1E1D2"))
                    cornerRadius = dpToPx(8).toFloat()
                }
                setOnClickListener {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("HEX Code", item.hexString)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this@PaletWastraActivity, "Kode HEX ${item.hexString} tersalin!", Toast.LENGTH_SHORT).show()
                }
            }
            swatchCard.addView(btnCopy, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36)))

            paletteContainer.addView(swatchCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(8) })
        }

        val comboCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvComboHeader = TextView(this).apply {
            text = "💡 Inspirasi Kombinasi Desain Wastra"
            textSize = 16f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        comboCard.addView(tvComboHeader)

        val tvComboDesc = TextView(this).apply {
            text = "Palet warna alami dari kain batik ini dapat dipadukan untuk desain pakaian formal, aksesoris, maupun dekorasi interior."
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
        }
        comboCard.addView(tvComboDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(12) })

        val paletteStrip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = colors.size.toFloat()
        }

        for (c in colors) {
            val stripItem = View(this).apply {
                setBackgroundColor(c.colorInt)
            }
            paletteStrip.addView(stripItem, LinearLayout.LayoutParams(0, dpToPx(24), 1f))
        }

        comboCard.addView(paletteStrip, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        combinationContainer.addView(comboCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
}
