package com.example.thisapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@OptIn(androidx.camera.core.ExperimentalGetImage::class)
class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private val firestore = FirebaseFirestore.getInstance()
    private var isMoodDetected = false // Flag untuk mencegah hasil berulang

    // Buffer untuk stabilitas deteksi
    private val smileProbabilities = mutableListOf<Float>()
    private val leftEyeProbabilities = mutableListOf<Float>()
    private val rightEyeProbabilities = mutableListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        // Memeriksa izin kamera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        floatingActionButton.setOnClickListener {
            if (!isMoodDetected) { // Cek apakah mood sudah terdeteksi
                Toast.makeText(this, "Detecting mood...", Toast.LENGTH_SHORT).show()
                captureAndAnalyzeImage()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun captureAndAnalyzeImage() {
        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            if (isMoodDetected) {
                imageProxy.close()
                return@setAnalyzer
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setMinFaceSize(0.1f) // Ukuran minimum wajah
                    .enableTracking() // Melacak wajah untuk stabilitas
                    .build()

                val detector = FaceDetection.getClient(options)

                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face = faces[0]

                            // Validasi ukuran bounding box
                            val boundingBox = face.boundingBox
                            if (boundingBox.width() < 100 || boundingBox.height() < 100) {
                                runOnUiThread {
                                    Toast.makeText(this, "Face too small, move closer to the camera.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                analyzeMood(face)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "No face detected.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FaceDetection", "Face detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    private fun analyzeMood(face: Face) {
        if (isMoodDetected) return // Pastikan hasil hanya muncul sekali

        // Tambahkan probabilitas ke buffer
        addProbabilityToBuffer(smileProbabilities, face.smilingProbability ?: 0.0f)
        addProbabilityToBuffer(leftEyeProbabilities, face.leftEyeOpenProbability ?: 0.0f)
        addProbabilityToBuffer(rightEyeProbabilities, face.rightEyeOpenProbability ?: 0.0f)

        // Hitung rata-rata probabilitas
        val smileProb = calculateAverage(smileProbabilities)
        val leftEyeOpenProb = calculateAverage(leftEyeProbabilities)
        val rightEyeOpenProb = calculateAverage(rightEyeProbabilities)

        val mood = when {
            smileProb >= 0.85 && leftEyeOpenProb >= 0.75 && rightEyeOpenProb >= 0.75 -> "Happy 😄"
            smileProb <= 0.2 && leftEyeOpenProb <= 0.4 && rightEyeOpenProb <= 0.4 -> "Sad 😢"
            smileProb <= 0.2 && leftEyeOpenProb >= 0.8 && rightEyeOpenProb >= 0.8 -> "Angry 😡"
            smileProb <= 0.25 && leftEyeOpenProb <= 0.2 && rightEyeOpenProb <= 0.2 -> "Scared 😨"
            smileProb <= 0.3 && (leftEyeOpenProb >= 0.85 || rightEyeOpenProb >= 0.85) -> "Surprised 😲"
            else -> "Neutral 😐"
        }

        isMoodDetected = true // Set flag bahwa mood sudah terdeteksi
        saveMoodToFirestore(mood)

        // Navigasi ke ResultActivity
        val intent = Intent(this, BerandaActivity::class.java)
        intent.putExtra("MOOD", mood)
        startActivity(intent)
    }

    private fun saveMoodToFirestore(mood: String) {
        val moodData = hashMapOf(
            "mood" to mood,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("moods")
            .add(moodData)
            .addOnSuccessListener {
                Log.d("Firestore", "Mood saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to save mood", e)
            }
    }

    private fun addProbabilityToBuffer(probList: MutableList<Float>, value: Float) {
        if (probList.size >= 5) probList.removeAt(0) // Batasi buffer hingga 5 nilai terakhir
        probList.add(value)
    }

    private fun calculateAverage(probList: List<Float>): Float {
        return if (probList.isNotEmpty()) probList.average().toFloat() else 0.0f
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
