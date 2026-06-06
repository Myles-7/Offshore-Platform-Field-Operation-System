package com.offshore.platform.mobile.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.offshore.platform.mobile.OffshoreApp
import java.io.File
import java.io.FileOutputStream

/**
 * Adds watermark overlay to a photo.
 *
 * Watermark content: work order number, timestamp, operator name,
 *  optional location and deviceId.
 */
object WatermarkUtil {

    private const val TEXT_SIZE_SP = 14f
    private const val PADDING_DP = 12f
    private const val ALPHA = 0.7f

    /**
     * Open source bitmap from [uri], apply watermark, save to app internal storage,
     * return the watermarked file.
     */
    fun applyWatermark(
        context: Context,
        sourceUri: Uri,
        workOrderNo: String,
        operatorName: String?,
        location: String? = null,
        deviceId: String? = null
    ): File? {
        return try {
            val sourceBitmap = MediaStoreBitmapDecoder.decode(context, sourceUri) ?: return null
            val marked = overlayWatermark(sourceBitmap, workOrderNo, operatorName, location, deviceId)
            val outputDir = File(context.filesDir, "watermarked")
            outputDir.mkdirs()
            val f = File(outputDir, "wm_${System.currentTimeMillis()}.jpg")
            FileOutputStream(f).use { out ->
                marked.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            marked.recycle()
            sourceBitmap.recycle()
            f
        } catch (e: Exception) {
            null
        }
    }

    private fun overlayWatermark(
        bitmap: Bitmap,
        workOrderNo: String,
        operatorName: String?,
        location: String?,
        deviceId: String?
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = (255 * ALPHA).toInt()
            textSize = spToPx(TEXT_SIZE_SP)
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }

        val density = OffshoreApp.instance.resources.displayMetrics.density
        val pad = (PADDING_DP * density).toInt()
        val lineHeight = (paint.textSize + 4f).toInt()

        // Build watermark lines
        val lines = mutableListOf<String>()
        lines.add("工单: $workOrderNo")
        lines.add("时间: ${DateTimeUtil.nowFormatted()}")
        operatorName?.let { lines.add("拍摄人: $it") }
        location?.let { lines.add("地点: $it") }
        deviceId?.let { lines.add("设备: $it") }

        // Draw at bottom-right
        var y = result.height - pad - (lines.size - 1) * lineHeight
        for (line in lines) {
            val w = paint.measureText(line).toInt()
            canvas.drawText(line, (result.width - w - pad).toFloat(), y.toFloat(), paint)
            y += lineHeight
        }

        return result
    }

    private fun spToPx(sp: Float): Float =
        sp * OffshoreApp.instance.resources.displayMetrics.scaledDensity
}

/** Decodes bitmap from content URI, handling EXIF rotation. */
internal object MediaStoreBitmapDecoder {
    fun decode(context: Context, uri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val opts = BitmapFactory.Options().apply { inSampleSize = 1 }
        return BitmapFactory.decodeStream(inputStream, null, opts)
    }
}
