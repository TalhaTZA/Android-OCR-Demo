package com.nextgeni.ocr_demo

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.WorkSource
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var cameraView: SurfaceView
    private lateinit var textView: TextView
    private lateinit var cameraSource: CameraSource
    private val RequestCameraPermissionID = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.surface_view)
        textView = findViewById(R.id.text_view)

        val textRecognizer = TextRecognizer.Builder(this).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Detector Dependencies are not yet available", Toast.LENGTH_LONG).show()
        } else {
            cameraSource = CameraSource.Builder(this, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0F)
                    .setAutoFocusEnabled(true)
                    .build()

            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                    try {

                        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), RequestCameraPermissionID)

                            return
                        }
                        cameraSource.start(cameraView.holder)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                    cameraSource.stop()
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                }

            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {

                }

                override fun receiveDetections(dectections: Detector.Detections<TextBlock>?) {

                    dectections?.detectedItems?.apply {

                        if (this.size() != 0) {
                            textView.post {
                                val stringBuilder = StringBuilder()

                                this.forEach { key, value ->
                                    stringBuilder.append(value.value)
                                    stringBuilder.append("\n")
                                }

                                textView.text = stringBuilder.toString()

                            }
                        }

                    }


                }

            })

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            RequestCameraPermissionID -> {
                if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return
                    }

                    cameraSource.start(cameraView.holder)

                }
            }
        }

    }
}