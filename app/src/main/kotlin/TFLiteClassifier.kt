package com.example.wastrain

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer

data class PredictionItem(
    val labelRaw: String,
    val displayName: String,
    val confidence: Float,
    val percentage: String
)

data class ClassificationResult(
    val topPrediction: PredictionItem,
    val top3Predictions: List<PredictionItem>,
    val isLowConfidence: Boolean,
    val modelName: String,
    val errorMessage: String? = null
)

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var inputWidth = 224
    private var inputHeight = 224
    private var currentModelFileName = EFFICIENTNET_MODEL

    companion object {
        const val EFFICIENTNET_MODEL = "efficientnetv2b0_float16.tflite"
        const val MOBILENET_MODEL = "mobilenetv3large_float16.tflite"
        const val CONFIDENCE_THRESHOLD = 0.50f
    }

    fun loadModel(modelFileName: String = EFFICIENTNET_MODEL): Boolean {
        try {
            close()
            currentModelFileName = modelFileName
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)

            // Read labels
            labels = loadLabels()

            // Inspect input shape
            val inputTensor = interpreter?.getInputTensor(0)
            if (inputTensor != null) {
                val shape = inputTensor.shape()
                if (shape.size >= 3) {
                    inputHeight = shape[1]
                    inputWidth = shape[2]
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun loadLabels(): List<String> {
        val labelList = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("labels.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.trim().isNotEmpty()) {
                    labelList.add(line.trim())
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return labelList
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (interpreter == null) {
            val loaded = loadModel(currentModelFileName)
            if (!loaded) {
                val dummyPrediction = PredictionItem("unknown", "Model Tidak Terbaca", 0f, "0%")
                return ClassificationResult(
                    topPrediction = dummyPrediction,
                    top3Predictions = listOf(dummyPrediction),
                    isLowConfidence = true,
                    modelName = currentModelFileName,
                    errorMessage = "Gagal memuat model TensorFlow Lite dari assets."
                )
            }
        }

        val interp = interpreter!!

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val numClasses = if (labels.isNotEmpty()) labels.size else 20
        val outputArray = Array(1) { FloatArray(numClasses) }

        interp.run(tensorImage.buffer, outputArray)

        val probabilities = outputArray[0]

        val predictions = mutableListOf<PredictionItem>()
        for (i in probabilities.indices) {
            val rawLabel = if (i < labels.size) labels[i] else "Class $i"
            val info = MotifDataRepository.getByName(rawLabel)
            val displayName = info?.name ?: rawLabel.replace("-", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            val conf = probabilities[i]
            val pct = String.format("%.1f%%", conf * 100f)
            predictions.add(PredictionItem(rawLabel, displayName, conf, pct))
        }

        predictions.sortByDescending { it.confidence }

        val top1 = predictions.firstOrNull() ?: PredictionItem("unknown", "Tidak Diketahui", 0f, "0%")
        val top3 = predictions.take(3)
        val isLow = top1.confidence < CONFIDENCE_THRESHOLD

        return ClassificationResult(
            topPrediction = top1,
            top3Predictions = top3,
            isLowConfidence = isLow,
            modelName = if (currentModelFileName.contains("efficientnet", ignoreCase = true)) "EfficientNetV2-B0" else "MobileNetV3-Large"
        )
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
