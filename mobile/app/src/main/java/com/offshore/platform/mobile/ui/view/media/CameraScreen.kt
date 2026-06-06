package com.offshore.platform.mobile.ui.view.media

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.offshore.platform.mobile.util.WatermarkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    workOrderId: Long,
    recordId: Long?,
    workOrderNo: String,
    workLocation: String?,
    onNavigateBack: () -> Unit,
    onPhotoSaved: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(workOrderId) {
        viewModel.init(workOrderId, recordId, workOrderNo, workLocation)
    }
    LaunchedEffect(state.saved) { if (state.saved) onPhotoSaved() }

    // Auto-check permission on first composition
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val hasCamera = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY)
            if (hasCamera) viewModel.setCameraReady()
            else viewModel.setUseSystemCamera()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val hasCamera = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY)
            if (hasCamera) viewModel.setCameraReady()
            else viewModel.setUseSystemCamera()
        } else {
            viewModel.setPermissionDenied()
        }
    }

    // CameraX executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ImageCapture use case ref
    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }
    val captureTrigger = remember { mutableStateOf(0L) }

    // Observe capture trigger
    LaunchedEffect(captureTrigger.value) {
        if (captureTrigger.value > 0) {
            val imgCapture = imageCaptureRef.value
            if (imgCapture != null) {
                viewModel.setCapturing(true)
                val photoFile = File(context.filesDir, "captures").apply { mkdirs() }
                    .let { File(it, "cap_${com.offshore.platform.mobile.util.DateTimeUtil.fileNameTimestamp()}.jpg") }
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imgCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            viewModel.onPhotoFileCaptured(context, photoFile)
                        }
                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraScreen", "Capture failed: ${exception.message}", exception)
                            viewModel.onCaptureFailed("拍照失败: ${exception.message}")
                        }
                    }
                )
            } else {
                // ImageCapture not initialised — fallback
                viewModel.setUseSystemCamera()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (state.cameraReady && state.useCameraX) {
                        IconButton(onClick = {
                            viewModel.toggleFlash()
                            // Update the ImageCapture flash mode
                            imageCaptureRef.value?.flashMode = if (viewModel.uiState.value.flashOn)
                                ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                        }) {
                            Icon(
                                if (state.flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "闪光灯",
                                tint = if (state.flashOn) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.toggleCameraLens() }) {
                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = "切换摄像头")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.permissionDenied -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("需要相机权限才能拍照", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("授予权限")
                        }
                    }
                }
                state.error != null && state.capturedFile == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = {
                            viewModel.retake()
                            viewModel.setUseSystemCamera()
                        }) {
                            Text("切换到系统相机")
                        }
                    }
                }
                state.capturedFile != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("照片已拍摄", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(state.capturedFile?.name ?: "", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = viewModel::retake) { Text("重新拍摄") }
                                Button(onClick = { viewModel.savePhoto() }) { Text("确认保存") }
                            }
                        }
                    }
                }
                state.cameraReady && state.useCameraX -> {
                    // CameraX PreviewView
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProvider.unbindAll()

                                    val cameraSelector = if (state.useFrontCamera)
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    else
                                        CameraSelector.DEFAULT_BACK_CAMERA

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imgCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                        .setFlashMode(if (state.flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                                        .build()

                                    imageCaptureRef.value = imgCapture

                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imgCapture
                                    )
                                } catch (e: Exception) {
                                    Log.e("CameraScreen", "CameraX bind failed: ${e.message}")
                                    viewModel.setUseSystemCamera()
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                    )

                    // Capture button
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("工单: $workOrderNo", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.height(8.dp))
                        FilledIconButton(
                            onClick = {
                                captureTrigger.value = System.currentTimeMillis()
                            },
                            modifier = Modifier.size(72.dp),
                            enabled = !state.isCapturing,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            if (state.isCapturing) {
                                CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            } else {
                                Icon(Icons.Default.CameraAlt, "拍照", Modifier.size(36.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (state.useFrontCamera) "前置摄像头" else "后置摄像头",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
                state.cameraReady && !state.useCameraX -> {
                    // System camera fallback
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("工单: $workOrderNo", style = MaterialTheme.typography.titleMedium)
                        Text("(系统相机模式)", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = {
                            // Use system camera intent
                            val dir = File(context.filesDir, "captures"); dir.mkdirs()
                            val file = File(dir, "cap_${com.offshore.platform.mobile.util.DateTimeUtil.fileNameTimestamp()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

                            // Apply watermark
                            val watermarked = WatermarkUtil.applyWatermark(
                                context, uri, workOrderNo,
                                com.offshore.platform.mobile.util.TokenManager.getRealName()
                                    ?: com.offshore.platform.mobile.util.TokenManager.getUsername(),
                                workLocation, com.offshore.platform.mobile.util.DeviceManager.getDeviceId()
                            )
                            viewModel.onSystemCameraPhoto(watermarked ?: file)
                        }) {
                            Icon(Icons.Default.Camera, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("拍照")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("工单: $workOrderNo", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.Camera, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("打开相机")
                        }
                    }
                }
            }
            if (state.savedOffline) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
                ) {
                    Text("已离线保存，待上传")
                }
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }
}
