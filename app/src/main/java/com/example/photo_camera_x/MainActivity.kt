package com.example.photo_camera_x

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var controller: LifecycleCameraController
    private lateinit var bottomSheet: View
    private lateinit var recyclerPhotos: RecyclerView
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var switchCameraButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var takePhotoButton: ImageButton



    private val CAMERA_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()

        setupRecyclerView()


        checkPermissions()


       /* previewView = findViewById(R.id.previewView)
        recyclerPhotos = findViewById(R.id.recyclerPhotos)
        bottomSheet = findViewById(R.id.bottomSheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
*/


        }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        recyclerPhotos = findViewById(R.id.recyclerPhotos)
        bottomSheet = findViewById(R.id.bottomSheet)
        switchCameraButton = findViewById(R.id.SwitchCamera)
        galleryButton = findViewById(R.id.Gallery)
        takePhotoButton = findViewById(R.id.btnTakePhoto)


        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 0
            state = BottomSheetBehavior.STATE_HIDDEN
        }

      /*  if (hasRequiredPermissions()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, 0)
        }*/


        }

    private fun setupRecyclerView() {
        val adapter = PhotoAdapter()
        recyclerPhotos.adapter = adapter
        recyclerPhotos.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bitmaps.collect { adapter.submitList(it) }
            }
        }





       /* val adapter = PhotoAdapter()
        recyclerPhotos.adapter = adapter

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bitmaps.collect { adapter.submitList(it) }

            }
        }*/

        }

    private fun checkPermissions() {
        if (hasRequiredPermissions()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, 0)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && hasRequiredPermissions()) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        try {
            controller = LifecycleCameraController(this).apply {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
            }
            previewView.controller = controller
            controller.bindToLifecycle(this)
            setupButtonListeners()
        } catch (e: Exception) {
            Log.e("CameraX", "Ошибка при инициализации камеры", e)
        }

       /* findViewById<ImageButton>(R.id.SwitchCamera).setOnClickListener {
            controller.cameraSelector =
                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else CameraSelector.DEFAULT_BACK_CAMERA
        }

        findViewById<ImageButton>(R.id.Gallery).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        findViewById<ImageButton>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto(controller, viewModel::onTakePhoto)
        }*/

    }

    private fun setupButtonListeners() {
        findViewById<ImageButton>(R.id.SwitchCamera).setOnClickListener {
            controller.cameraSelector = if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        }

        findViewById<ImageButton>(R.id.Gallery).setOnClickListener {
            bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_HIDDEN
                else -> BottomSheetBehavior.STATE_EXPANDED
            }
        }

        findViewById<ImageButton>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto(controller, viewModel::onTakePhoto)
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    onPhotoTaken(rotatedBitmap)
                }


                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }

            }
        )

    }
    private fun hasRequiredPermissions(): Boolean {
        return CAMERA_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }


}
