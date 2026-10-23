package com.example.wastrain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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

data class ModelBenchmarkResult(
    val modelName: String,
    val fileName: String,
    val fileSizeMiB: Double,
    val latencyMs: Float,
    val classificationResult: ClassificationResult
)

class LaboratoriumAiActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnRunBenchmark: Button
    private lateinit var resultsContainer: LinearLayout
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
            text = "Laboratorium AI"
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

        // Explanation Header Card
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
            text = "🔬 Perbandingan Model TFLite"
            textSize = 17f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        introCard.addView(tvIntroTitle)

        val tvIntroDesc = TextView(this).apply {
            text = "Bandingkan performa inferensi, ukuran file, serta hasil prediksi antara EfficientNetV2-B0 Float16 dan MobileNetV3-Large Float16 pada satu foto batik yang sama."
            textSize = 13f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.1f)
        }
        introCard.addView(tvIntroDesc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6) })

        root.addView(introCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Image Selection Frame
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
            text = "🖼️  Ganti Foto Uji"
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

        // Run Benchmark Button
        btnRunBenchmark = Button(this).apply {
            text = "⚡  Jalankan Perbandingan Model"
            textSize = 15f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#704331"))
                cornerRadius = dpToPx(14).toFloat()
            }
            setOnClickListener { runBenchmark() }
        }
        root.addView(btnRunBenchmark, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(52)).apply { bottomMargin = dpToPx(16) })

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        root.addView(progressBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER; bottomMargin = dpToPx(16) })

        // Results Container
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(resultsContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(mainLayout)

        if (selectedBitmap != null) {
            runBenchmark()
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            selectedBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (selectedBitmap != null && ::imgPreview.isInitialized) {
                imgPreview.setImageBitmap(selectedBitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat foto.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runBenchmark() {
        val bitmap = selectedBitmap ?: run {
            // Default sample from logo or notification
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.wastrain_logo)
            selectedBitmap = bmp
            imgPreview.setImageBitmap(bmp)
            bmp
        }

        btnRunBenchmark.isEnabled = false
        progressBar.visibility = View.VISIBLE
        resultsContainer.removeAllViews()

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            val benchResults = mutableListOf<ModelBenchmarkResult>()

            val modelsToTest = listOf(
                Pair("EfficientNetV2-B0 Float16", TFLiteClassifier.EFFICIENTNET_MODEL),
                Pair("MobileNetV3-Large Float16", TFLiteClassifier.MOBILENET_MODEL)
            )

            val classifier = TFLiteClassifier(this)

            for ((displayName, fileName) in modelsToTest) {
                val loaded = classifier.loadModel(fileName)
                if (loaded) {
                    // Get File Size
                    var sizeMiB = 0.0
                    try {
                        val afd = assets.openFd(fileName)
                        sizeMiB = afd.length / (1024.0 * 1024.0)
                        afd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Warm-up run
                    classifier.classify(bitmap)

                    // Measured run
                    val startNanos = SystemClock.elapsedRealtimeNanos()
                    val res = classifier.classify(bitmap)
                    val endNanos = SystemClock.elapsedRealtimeNanos()
                    val latencyMs = (endNanos - startNanos) / 1_000_000.0f

                    benchResults.add(
                        ModelBenchmarkResult(
                            modelName = displayName,
                            fileName = fileName,
                            fileSizeMiB = sizeMiB,
                            latencyMs = latencyMs,
                            classificationResult = res
                        )
                    )
                }
            }

            classifier.close()

            handler.post {
                progressBar.visibility = View.GONE
                btnRunBenchmark.isEnabled = true
                renderBenchmarkUI(benchResults)
            }
        }
    }

    private fun renderBenchmarkUI(results: List<ModelBenchmarkResult>) {
        resultsContainer.removeAllViews()

        if (results.isEmpty()) {
            Toast.makeText(this, "Gagal menguji model TFLite.", Toast.LENGTH_SHORT).show()
            return
        }

        // Side-by-Side Cards
        for (res in results) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(16).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                }
            }

            val tvMName = TextView(this).apply {
                text = "🤖 ${res.modelName}"
                textSize = 17f
                setTextColor(Color.parseColor("#2F1D18"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            card.addView(tvMName)

            // Metrics row (Latency & File Size)
            val metricsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvLatency = TextView(this).apply {
                text = "⚡ Waktu Inferensi: ${String.format("%.1f", res.latencyMs)} ms"
                textSize = 13f
                setTextColor(Color.parseColor("#B76E4D"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            metricsRow.addView(tvLatency, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            val tvSize = TextView(this).apply {
                text = "💾 Ukuran File: ${String.format("%.2f", res.fileSizeMiB)} MiB"
                textSize = 12f
                setTextColor(Color.parseColor("#8A6B5C"))
            }
            metricsRow.addView(tvSize)

            card.addView(metricsRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6); bottomMargin = dpToPx(12) })

            // Predicted Motif
            val topPred = res.classificationResult.topPrediction
            val tvPred = TextView(this).apply {
                text = "Hasil Prediksi: ${topPred.displayName} (${topPred.percentage})"
                textSize = 15f
                setTextColor(Color.parseColor("#2F1D18"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            card.addView(tvPred)

            // Top-3 Predictions
            val tvTop3Header = TextView(this).apply {
                text = "Top-3 Probabilitas:"
                textSize = 13f
                setTextColor(Color.parseColor("#704331"))
            }
            card.addView(tvTop3Header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

            for (p in res.classificationResult.top3Predictions) {
                val pRow = TextView(this).apply {
                    text = " • ${p.displayName} — ${p.percentage}"
                    textSize = 13f
                    setTextColor(Color.parseColor("#704331"))
                }
                card.addView(pRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(2) })
            }

            resultsContainer.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })
        }

        // Evaluation Note
        val noteCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }

        val tvNoteTitle = TextView(this).apply {
            text = "📊 Catatan Evaluasi Penelitian"
            textSize = 14f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        noteCard.addView(tvNoteTitle)

        val tvNoteBody = TextView(this).apply {
            text = "Persentase di atas adalah skor confidence untuk sampel foto ini. Pengukuran akurasi universal (Accuracy, Precision, Recall, F1-Score) dihitung secara terpisah menggunakan test dataset terstandar."
            textSize = 12f
            setTextColor(Color.parseColor("#704331"))
            setLineSpacing(dpToPx(2).toFloat(), 1.1f)
        }
        noteCard.addView(tvNoteBody, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

        resultsContainer.addView(noteCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
}
