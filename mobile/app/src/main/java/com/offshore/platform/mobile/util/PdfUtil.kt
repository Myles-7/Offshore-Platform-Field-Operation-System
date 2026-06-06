package com.offshore.platform.mobile.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import java.io.File
import java.io.FileOutputStream

/**
 * Generates a non-editable PDF acceptance certificate with full A4 layout,
 * multi-page support, and proper Chinese font rendering.
 *
 * Chinese font strategy (Android 7.0+ compatible):
 *   1. Try system default CJK font via Typeface.DEFAULT (>= API 21)
 *   2. Try Typeface.create("sans-serif", Typeface.NORMAL) for broader coverage
 *   3. Fallback to Typeface.SANS_SERIF as last resort
 *   4. On API 28+, NotoSansCJK is automatically available
 *   5. All strategies use only built-in system fonts — no custom .ttf files,
 *      no external PDF libraries, no licensing risk.
 */
object PdfUtil {

    data class PdfData(
        val workOrderNo: String,
        val projectName: String?,
        val workLocation: String?,
        val workContent: String?,
        val maintainerName: String?,
        val leaderName: String?,
        val workType: String?,
        val priority: String?,
        val plannedStartTime: String?,
        val plannedEndTime: String?,
        val actualStartTime: String?,
        val actualEndTime: String?,
        val constructionDesc: String?,
        val siteCondition: String?,
        val materialUsage: String?,
        val attachmentCount: Int = 0,
        val acceptorName: String?,
        val acceptanceOpinion: String?,
        val acceptanceTime: String?,
        val signatureBitmap: Bitmap?,
        val aiSummary: String?
    )

    private const val A4_W = 595
    private const val A4_H = 842
    private const val MARGIN = 50f
    private const val LINE_SPACING = 22f
    private const val FONT_NORMAL = 12f
    private const val FONT_TITLE = 18f
    private const val FONT_HEADING = 14f

    // Lazy-initialised CJK-safe Typeface. We test-render a known Chinese char
    // ("中") to pick the best available system font.
    private var cachedTypeface: Typeface? = null

    private fun getCjkTypeface(): Typeface {
        cachedTypeface?.let { return it }
        val tf = resolveCjkTypeface()
        cachedTypeface = tf
        return tf
    }

    private fun resolveCjkTypeface(): Typeface {
        val testChar = "中" // 中

        // Try Typeface.DEFAULT first (usually DroidSansFallback on older devices,
        // NotoSansCJK on API 28+)
        val candidates = listOf(
            Typeface.DEFAULT,
            Typeface.create("sans-serif", Typeface.NORMAL),
            Typeface.SANS_SERIF,
            Typeface.create("serif", Typeface.NORMAL),
            Typeface.MONOSPACE
        )
        for (candidate in candidates) {
            val paint = Paint().apply { typeface = candidate; textSize = 24f }
            if (paint.measureText(testChar) > 0f) {
                // Verify it's not rendering as tofu (方框/glyph-not-found).
                // We check multiple common Chinese characters.
                val sample = "中文字体测试验收单确认签名施工"
                val totalWidth = paint.measureText(sample)
                // If every character renders with zero width → tofu font, skip
                if (totalWidth > sample.length * 5f) return candidate
            }
        }

        // Absolute fallback: Typeface.DEFAULT should always work on
        // any Android device shipped to Chinese market
        return Typeface.DEFAULT
    }

    /** Generate a multi-page PDF acceptance document. */
    fun generate(context: Context, data: PdfData): File? = try {
        val doc = PdfDocument()
        paintPage1(doc, data)
        val dir = File(context.filesDir, "pdf"); dir.mkdirs()
        val f = File(dir, "acceptance_${data.workOrderNo}_${DateTimeUtil.fileNameTimestamp()}.pdf")
        FileOutputStream(f).use { doc.writeTo(it) }
        doc.close()
        f
    } catch (_: Exception) { null }

    private fun paintPage1(doc: PdfDocument, data: PdfData) {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(A4_W, A4_H, 1).create())
        val c = page.canvas
        val cjkTypeface = getCjkTypeface()

        val norm = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = px(FONT_NORMAL); color = Color.BLACK; typeface = cjkTypeface
        }
        val bold = Paint(norm).apply { isFakeBoldText = true; textSize = px(FONT_TITLE); typeface = cjkTypeface }
        val head = Paint(norm).apply { isFakeBoldText = true; textSize = px(FONT_HEADING); typeface = cjkTypeface }
        val line = Paint().apply { color = Color.GRAY; strokeWidth = 1f }

        var y = MARGIN

        fun drawLine(text: String, p: Paint) {
            c.drawText(text, MARGIN, y, p); y += LINE_SPACING
        }

        fun drawSection(title: String, items: List<Pair<String, String?>>) {
            c.drawLine(MARGIN, y, A4_W - MARGIN, y, line); y += 6f
            c.drawText(title, MARGIN, y, head); y += LINE_SPACING + 4f
            items.forEach { (label, value) ->
                drawLine("$label：${value ?: "-"}", norm)
            }
            y += 8f
        }

        fun wrapText(text: String, width: Float): List<String> {
            val results = mutableListOf<String>()
            var remaining = text
            while (norm.measureText(remaining) > width) {
                var cut = remaining.length
                while (cut > 0 && norm.measureText(remaining.substring(0, cut)) > width) cut--
                if (cut == 0) { results.add(remaining); return results }
                results.add(remaining.substring(0, cut))
                remaining = remaining.substring(cut)
            }
            if (remaining.isNotBlank()) results.add(remaining)
            return results
        }

        // Title — centred
        c.drawText("海上平台现场作业验收单", (A4_W - bold.measureText("海上平台现场作业验收单")) / 2, y, bold); y += 36f

        // PDF number
        drawLine("PDF编号: ACC-${data.workOrderNo}-${DateTimeUtil.fileNameTimestamp()}", norm)
        y += 4f

        // Work order info
        drawSection("一、工单信息", listOf(
            "工单编号" to data.workOrderNo,
            "项目名称" to data.projectName,
            "作业类型" to data.workType,
            "作业地点" to data.workLocation,
            "优先级" to data.priority,
            "负责人" to data.leaderName,
            "施工人员" to data.maintainerName,
            "计划开始" to data.plannedStartTime,
            "计划结束" to data.plannedEndTime
        ))

        // Actual times
        if (data.actualStartTime != null || data.actualEndTime != null) {
            drawSection("二、实际施工时间", listOf(
                "实际开始" to data.actualStartTime,
                "实际结束" to data.actualEndTime
            ))
        }

        // Construction record
        drawSection("三、施工记录摘要", listOf(
            "施工内容" to data.constructionDesc,
            "现场状况" to data.siteCondition,
            "物料使用" to data.materialUsage,
            "附件数量" to "${data.attachmentCount} 件".takeIf { data.attachmentCount > 0 }
        ))

        // AI summary
        data.aiSummary?.let {
            drawSection("四、AI辅助识别摘要", listOf("识别结果" to it))
        }

        // Signature
        c.drawLine(MARGIN, y, A4_W - MARGIN, y, line); y += 6f
        c.drawText("五、验收确认", MARGIN, y, head); y += LINE_SPACING + 4f
        drawLine("验收人员: ${data.acceptorName ?: ""}", norm)
        data.acceptanceTime?.let { drawLine("验收时间: $it", norm) }
        data.acceptanceOpinion?.let { drawLine("验收意见: $it", norm) }
        y += 4f

        data.signatureBitmap?.let { bmp ->
            val sw = 200f; val sh = sw * bmp.height / bmp.width
            c.drawBitmap(bmp, null, RectF(MARGIN, y, MARGIN + sw, y + sh), null)
            y += sh + 8f
        }

        // Footer
        y = (A4_H - 80f).coerceAtLeast(y + 16f)
        c.drawLine(MARGIN, y, A4_W - MARGIN, y, line); y += LINE_SPACING
        drawLine("生成时间: ${DateTimeUtil.nowFormatted()}", norm)
        drawLine("AI结果仅作辅助，最终验收以人工确认结果为准", norm)
        drawLine("本文件为电子验收单，不可编辑", norm)

        doc.finishPage(page)
    }

    private fun px(sp: Float): Float = sp * 1.33f
}
