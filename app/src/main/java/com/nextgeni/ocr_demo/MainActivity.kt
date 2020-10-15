package com.nextgeni.ocr_demo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.WorkSource
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.wonderkiln.camerakit.*
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var cameraView: CameraView
    private lateinit var textView: TextView
    private lateinit var button: Button
    private val RequestCameraPermissionID = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.surface_view)
        textView = findViewById(R.id.text_view)
        button = findViewById(R.id.btn_capture)

        button.setOnClickListener {
            cameraView.apply {
                button.isEnabled = false
                if (!isStarted) {
                    start()
                }
                Toast.makeText(this@MainActivity, "Processing", Toast.LENGTH_LONG).show()
                captureImage()
                stop()
            }
            Handler().postDelayed({
                cameraView.start()
                button.isEnabled = true
//                textView.text = null
            }, 2000)

        }

        val recognizer = TextRecognition.getClient()

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {

            }

            override fun onEvent(p0: CameraKitEvent?) {
            }

            override fun onImage(image: CameraKitImage?) {

                recognizer.process(InputImage.fromBitmap(Bitmap.createScaledBitmap(image!!.bitmap, cameraView.width, cameraView.height, false), 0))
                        .addOnSuccessListener { visionText ->
                            textView.text = visionText.text
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }

            }

            override fun onError(p0: CameraKitError?) {
            }

        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            RequestCameraPermissionID -> {
                if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return
                    }


                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

}