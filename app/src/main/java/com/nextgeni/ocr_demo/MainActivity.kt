package com.nextgeni.ocr_demo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.wonderkiln.camerakit.*

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
//                if (!isStarted) {
                start()
//                }
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

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()


        val scanner = BarcodeScanning.getClient(options)

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {

            }

            override fun onEvent(p0: CameraKitEvent?) {
            }

            override fun onImage(image: CameraKitImage?) {

                val imageInput = InputImage.fromBitmap(
                    Bitmap.createScaledBitmap(
                        image!!.bitmap,
                        cameraView.width,
                        cameraView.height,
                        false
                    ), 0
                )

                scanner.process(imageInput)
                    .addOnSuccessListener {
//                        for (barcode in it) {
//                            Toast.makeText(this@MainActivity, barcode.rawValue, Toast.LENGTH_SHORT)
//                                .show()
//                        }

                        if (it.isNotEmpty()) {
                            textView.text = it.first().rawValue
                        } else {
                            callTextRecognizer(imageInput)
                        }

                    }.addOnFailureListener { e ->
                        callTextRecognizer(imageInput)
                        Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_LONG)
                            .show()
                        e.printStackTrace()
                    }


            }

            override fun onError(p0: CameraKitError?) {
            }

        })
    }

    private fun callTextRecognizer(imageInput: InputImage) {
        val recognizer = TextRecognition.getClient()

        recognizer.process(imageInput)
            .addOnSuccessListener { visionText ->
                textView.text = visionText.text
                Toast.makeText(this@MainActivity, visionText.text, Toast.LENGTH_SHORT)
                    .show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()

            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            RequestCameraPermissionID -> {
                if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

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