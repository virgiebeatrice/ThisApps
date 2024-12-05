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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var isMoodDetected = false // Flag untuk mencegah hasil berulang
    private var coroutineScope = CoroutineScope(Dispatchers.Main) // Untuk penundaan proses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)

        // Memeriksa izin kamera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
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
                .also { analyzer ->
                    analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

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
    private fun processImage(imageProxy: ImageProxy) {
        if (isMoodDetected) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val detector = FaceDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Tambahkan penundaan sebelum analisis mood selesai
                        coroutineScope.launch {
                            delay(100)
                            analyzeMood(faces[0])
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

    private fun analyzeMood(face: Face) {
        if (isMoodDetected) return

        val smileProb = face.smilingProbability ?: 0.0f
        val leftEyeOpenProb = face.leftEyeOpenProbability ?: 0.0f
        val rightEyeOpenProb = face.rightEyeOpenProbability ?: 0.0f

        val mood = when {
            smileProb >= 0.85 && leftEyeOpenProb >= 0.75 && rightEyeOpenProb >= 0.75 -> "Happy 😄"
            smileProb <= 0.2 && leftEyeOpenProb <= 0.4 && rightEyeOpenProb <= 0.4 -> "Sad 😢"
            smileProb <= 0.2 && leftEyeOpenProb >= 0.8 && rightEyeOpenProb >= 0.8 -> "Angry 😡"
            smileProb <= 0.25 && leftEyeOpenProb <= 0.2 && rightEyeOpenProb <= 0.2 -> "Scared 😨"
            smileProb <= 0.3 && (leftEyeOpenProb >= 0.85 || rightEyeOpenProb >= 0.85) -> "Surprised 😲"
            else -> "Neutral 😐"
        }

        isMoodDetected = true
        stopCamera() // Hentikan deteksi kamera

        // Simpan ke Firestore dan navigasi
        saveDetectedMoodToFirestore(mood) { success ->
            if (success) {
                val intent = Intent(this, BerandaActivity::class.java)
                intent.putExtra("MOOD", mood)
                startActivity(intent)
                finish() // Akhiri MainActivity
            } else {
                Toast.makeText(this, "Failed to save mood. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDetectedMoodToFirestore(mood: String, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val moodData = hashMapOf(
                "mood" to mood,
                "lastUpdated" to System.currentTimeMillis() // Menambahkan timestamp untuk update terakhir
            )

            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .set(moodData, SetOptions.merge()) // Gunakan opsi merge
                .addOnSuccessListener {
                    Log.d("MainActivity", "Mood successfully saved!")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Failed to save mood: ${e.message}")
                    callback(false)
                }
        } else {
            Log.e("MainActivity", "User ID not found. Failed to save mood.")
            callback(false)
        }
    }

    private fun stopCamera() {
        imageAnalyzer?.clearAnalyzer() // Hentikan analyzer
        cameraExecutor.shutdown() // Hentikan executor
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
        coroutineScope.cancel() // Hentikan coroutine jika activity dihancurkan
        stopCamera()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
