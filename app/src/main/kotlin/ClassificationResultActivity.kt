package com.example.wastrain

import android.app.ProgressDialog
import android.content.Intent
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.util.concurrent.Executors

class ClassificationResultActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var imagePath: String? = null
    private var loadedBitmap: Bitmap? = null
    private var classificationResult: ClassificationResult? = null

    private lateinit var classifier: TFLiteClassifier
    private lateinit var appDb: AppDatabaseHelper
    private lateinit var btnFavToggle: Button

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        classifier = TFLiteClassifier(this)
        appDb = AppDatabaseHelper(this)

        val uriString = intent.getStringExtra("IMAGE_URI")
        imagePath = intent.getStringExtra("IMAGE_PATH")

        if (uriString != null) {
            imageUri = Uri.parse(uriString)
        }

        val prefs = getSharedPreferences("WastraIN_Prefs", MODE_PRIVATE)
        val selectedModel = prefs.getString("SELECTED_MODEL", TFLiteClassifier.EFFICIENTNET_MODEL)
            ?: TFLiteClassifier.EFFICIENTNET_MODEL

        classifier.loadModel(selectedModel)

        loadBitmap()

        if (loadedBitmap == null) {
            Toast.makeText(this, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        runClassification()
    }

    private fun loadBitmap() {
        try {
            if (imagePath != null) {
                val file = java.io.File(imagePath!!)
                if (file.exists()) {
                    loadedBitmap = BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            if (loadedBitmap == null && imageUri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                loadedBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun runClassification() {
        val bitmap = loadedBitmap ?: return

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Menganalisis motif batik dengan AI...")
            setCancelable(false)
            show()
        }

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            val result = classifier.classify(bitmap)
            classificationResult = result

            handler.post {
                progressDialog.dismiss()
                setupUI(result)
            }
        }
    }

    private fun setupUI(result: ClassificationResult) {
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#FBF5EE"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(24), dpToPx(20), dpToPx(32))
        }

        // Header Title
        val header = TextView(this).apply {
            text = "Hasil Klasifikasi AI"
            textSize = 22f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        )

        // Image Preview Box
        val imgView = ImageView(this).apply {
            setImageBitmap(loadedBitmap)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(16).toFloat()
            }
            clipToOutline = true
        }
        root.addView(
            imgView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(220)
            ).apply {
                bottomMargin = dpToPx(16)
            }
        )

        // Status Banner
        val top = result.topPrediction
        val isLow = result.isLowConfidence

        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            background = GradientDrawable().apply {
                setColor(if (isLow) Color.rgb(255, 243, 224) else Color.rgb(232, 245, 233))
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), if (isLow) Color.rgb(255, 183, 77) else Color.rgb(129, 199, 132))
            }
        }

        val tvStatus = TextView(this).apply {
            text = if (isLow) "⚠️ Tingkat Kepercayaan Rendah (< 50%)" else "✅ Motif Batik Terdeteksi"
            textSize = 14f
            setTextColor(if (isLow) Color.rgb(230, 81, 0) else Color.rgb(46, 125, 50))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        statusCard.addView(tvStatus)

        if (isLow) {
            val tvWarning = TextView(this).apply {
                text = "Akurasi di bawah threshold. Gambar mungkin bukan batik atau memiliki pencahayaan kurang jelas."
                textSize = 12f
                setTextColor(Color.rgb(180, 80, 20))
            }
            statusCard.addView(
                tvWarning,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(4) }
            )
        }

        root.addView(
            statusCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        )

        // Main Result Box
        val resultCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvMotifTitle = TextView(this).apply {
            text = top.displayName
            textSize = 24f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        resultCard.addView(tvMotifTitle)

        val tvConfidence = TextView(this).apply {
            text = "Tingkat Akurasi: ${top.percentage} (Model: ${result.modelName})"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
        }
        resultCard.addView(
            tvConfidence,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(16) }
        )

        val divider1 = View(this).apply {
            setBackgroundColor(Color.parseColor("#E9D8C9"))
        }
        resultCard.addView(divider1, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)))

        val tvTop3Title = TextView(this).apply {
            text = "Top-3 Prediksi Model AI"
            textSize = 15f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        resultCard.addView(
            tvTop3Title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(12); bottomMargin = dpToPx(8) }
        )

        for (item in result.top3Predictions) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvLabel = TextView(this).apply {
                text = item.displayName
                textSize = 14f
                setTextColor(Color.parseColor("#2F1D18"))
            }
            itemLayout.addView(tvLabel, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

            val tvPct = TextView(this).apply {
                text = item.percentage
                textSize = 14f
                setTextColor(Color.parseColor("#704331"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            itemLayout.addView(tvPct)

            resultCard.addView(
                itemLayout,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(4) }
            )

            val pBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = (item.confidence * 100f).toInt()
                progressDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#B76E4D"))
                    cornerRadius = dpToPx(4).toFloat()
                }
            }
            resultCard.addView(
                pBar,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(8)
                ).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(8) }
            )
        }

        root.addView(
            resultCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
        )

        // Advanced AI Tools Row (Jejak AI, Palet Wastra, Laboratorium AI)
        val advancedCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
            }
        }

        val tvAdvTitle = TextView(this).apply {
            text = "✨ Fitur Eksplorasi Lanjutan"
            textSize = 16f
            setTextColor(Color.parseColor("#2F1D18"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        advancedCard.addView(tvAdvTitle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(10) })

        val btnJejak = Button(this).apply {
            text = "👁️  Jejak AI (Visualisasi Grad-CAM)"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener {
                val intent = Intent(this@ClassificationResultActivity, JejakAiActivity::class.java).apply {
                    if (imageUri != null) putExtra("IMAGE_URI", imageUri.toString())
                }
                startActivity(intent)
            }
        }
        advancedCard.addView(btnJejak, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(46)).apply { bottomMargin = dpToPx(8) })

        val btnPalet = Button(this).apply {
            text = "🎨  Palet Wastra (Ekstraksi Warna Dominan)"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener {
                val intent = Intent(this@ClassificationResultActivity, PaletWastraActivity::class.java).apply {
                    if (imageUri != null) putExtra("IMAGE_URI", imageUri.toString())
                }
                startActivity(intent)
            }
        }
        advancedCard.addView(btnPalet, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(46)).apply { bottomMargin = dpToPx(8) })

        val btnLab = Button(this).apply {
            text = "🔬  Laboratorium AI (Perbandingan Model)"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener {
                val intent = Intent(this@ClassificationResultActivity, LaboratoriumAiActivity::class.java).apply {
                    if (imageUri != null) putExtra("IMAGE_URI", imageUri.toString())
                }
                startActivity(intent)
            }
        }
        advancedCard.addView(btnLab, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(46)))

        root.addView(advancedCard, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Motif Info Summary Card
        val motifInfo = MotifDataRepository.getByName(top.labelRaw)
        if (motifInfo != null) {
            val infoCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(14).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                }
            }

            val tvInfoTitle = TextView(this).apply {
                text = "📜 Filosofi ${motifInfo.name}"
                textSize = 16f
                setTextColor(Color.parseColor("#2F1D18"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            infoCard.addView(tvInfoTitle)

            val tvOrigin = TextView(this).apply {
                text = "📍 Asal: ${motifInfo.origin}"
                textSize = 13f
                setTextColor(Color.parseColor("#704331"))
            }
            infoCard.addView(tvOrigin, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4) })

            val tvPhil = TextView(this).apply {
                text = motifInfo.philosophy
                textSize = 13f
                setTextColor(Color.parseColor("#704331"))
            }
            infoCard.addView(tvPhil, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(8) })

            root.addView(
                infoCard,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(20) }
            )
        }

        // Save & Favorite Buttons
        val btnSave = Button(this).apply {
            text = "💾  Simpan ke Riwayat"
            textSize = 15f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#704331"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                if (loadedBitmap != null) {
                    val path = appDb.saveHistoryImage(loadedBitmap!!)
                    appDb.insertHistory(
                        motifId = top.labelRaw,
                        motifName = top.displayName,
                        confidence = top.confidence,
                        percentage = top.percentage,
                        modelName = result.modelName,
                        imagePath = path
                    )
                    Toast.makeText(this@ClassificationResultActivity, "Tersimpan di riwayat!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        root.addView(btnSave, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)).apply { bottomMargin = dpToPx(10) })

        if (motifInfo != null) {
            val isFav = appDb.isFavorite(motifInfo.id)
            btnFavToggle = Button(this).apply {
                text = if (isFav) "❤️  Tersimpan di Favorit" else "🤍  Tambahkan ke Favorit"
                textSize = 15f
                setTextColor(Color.parseColor("#2F1D18"))
                isAllCaps = false
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F1E1D2"))
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#E9D8C9"))
                }
                setOnClickListener {
                    val added = appDb.toggleFavorite(
                        motifId = motifInfo.id,
                        motifName = motifInfo.name,
                        origin = motifInfo.origin,
                        category = motifInfo.category
                    )
                    text = if (added) "❤️  Tersimpan di Favorit" else "🤍  Tambahkan ke Favorit"
                    val msg = if (added) "Ditambahkan ke Favorit!" else "Dihapus dari Favorit"
                    Toast.makeText(this@ClassificationResultActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
            root.addView(btnFavToggle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)).apply { bottomMargin = dpToPx(10) })

            val btnDetail = Button(this).apply {
                text = "📖  Lihat Detail Motif Selengkapnya"
                textSize = 15f
                setTextColor(Color.parseColor("#704331"))
                isAllCaps = false
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(2), Color.parseColor("#704331"))
                }
                setOnClickListener {
                    val intent = Intent(this@ClassificationResultActivity, MotifInfoActivity::class.java).apply {
                        putExtra("MOTIF_ID", motifInfo.id)
                    }
                    startActivity(intent)
                }
            }
            root.addView(btnDetail, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)).apply { bottomMargin = dpToPx(10) })
        }

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnRetake = Button(this).apply {
            text = "🔄  Klasifikasi Ulang"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener { finish() }
        }
        btnRow.addView(btnRetake, LinearLayout.LayoutParams(0, dpToPx(46), 1f).apply { marginEnd = dpToPx(6) })

        val btnShare = Button(this).apply {
            text = "🔗  Bagikan Hasil"
            textSize = 14f
            setTextColor(Color.parseColor("#704331"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(10).toFloat()
            }
            setOnClickListener {
                val shareText = "Saya mengenali motif batik '${top.displayName}' (Akurasi ${top.percentage}) menggunakan WastraIN AI!"
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Hasil WastraIN"))
            }
        }
        btnRow.addView(btnShare, LinearLayout.LayoutParams(0, dpToPx(46), 1f).apply { marginStart = dpToPx(6) })

        root.addView(btnRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        setContentView(scrollView)
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
