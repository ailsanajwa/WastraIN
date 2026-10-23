package com.example.wastrain

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto batik.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        previewView = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        rootLayout.addView(
            previewView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Top Overlay Bar
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            setBackgroundColor(Color.parseColor("#80000000"))
        }

        val btnBack = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }
        topBar.addView(btnBack, LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)))

        val tvTitle = TextView(this).apply {
            text = "Kamera WastraIN"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        topBar.addView(
            tvTitle,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        rootLayout.addView(
            topBar,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
        )

        // Bottom Overlay Bar with Shutter Button
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(32))
            setBackgroundColor(Color.parseColor("#80000000"))
        }

        val tvHint = TextView(this).apply {
            text = "Posisikan motif batik di tengah kamera"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        bottomBar.addView(
            tvHint,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        )

        val btnShutter = Button(this).apply {
            text = "📸  AMBIL FOTO"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.rgb(67, 38, 28))
                cornerRadius = dpToPx(28).toFloat()
            }
            setOnClickListener { takePhoto() }
        }
        bottomBar.addView(
            btnShutter,
            LinearLayout.LayoutParams(
                dpToPx(220),
                dpToPx(56)
            )
        )

        rootLayout.addView(
            bottomBar,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
        )

        setContentView(rootLayout)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Binding kamera gagal", exc)
                Toast.makeText(this, "Gagal membuka kamera.", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "Gagal mengambil foto: ${exc.message}", exc)
                    Toast.makeText(this@CameraActivity, "Gagal mengambil foto.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val intent = Intent(this@CameraActivity, ClassificationResultActivity::class.java).apply {
                        putExtra("IMAGE_URI", savedUri.toString())
                        putExtra("IMAGE_PATH", photoFile.absolutePath)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        )
    }

    private val outputDirectory: File
        get() {
            val mediaDir = externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
