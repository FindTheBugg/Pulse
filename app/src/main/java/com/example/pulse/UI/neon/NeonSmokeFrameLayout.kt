//
//package com.example.pulse.ui.views
//
//import android.content.Context
//import android.graphics.BlurMaskFilter
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Path
//import android.graphics.RectF
//import android.util.AttributeSet
//import android.view.View
//import android.widget.FrameLayout
//import kotlin.math.max
//import kotlin.random.Random
//
//class NeonSmokeFrameLayout @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : FrameLayout(context, attrs, defStyleAttr) {
//
//    private val density = resources.displayMetrics.density
//
//    private fun dp(value: Float): Float = value * density
//
//    private var effectEnabled = false
//
//    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeWidth = dp(0.9f)
//    }
//
//    private val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeWidth = dp(4f)
//        alpha = 20
//        maskFilter = BlurMaskFilter(dp(12f), BlurMaskFilter.Blur.NORMAL)
//    }
//
//    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeWidth = dp(2f)
//        alpha = 65
//        maskFilter = BlurMaskFilter(dp(6f), BlurMaskFilter.Blur.NORMAL)
//    }
//
//    private val smokeCloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeCap = Paint.Cap.ROUND
//        strokeJoin = Paint.Join.ROUND
//        strokeWidth = dp(5f)
//        alpha = 18
//        maskFilter = BlurMaskFilter(dp(9f), BlurMaskFilter.Blur.NORMAL)
//    }
//
//
//    private val smokeMidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeCap = Paint.Cap.ROUND
//        strokeJoin = Paint.Join.ROUND
//        strokeWidth = dp(2f)
//        alpha = 35
//        maskFilter = BlurMaskFilter(dp(4f), BlurMaskFilter.Blur.NORMAL)
//    }
//
//
//    private val smokeDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        strokeCap = Paint.Cap.ROUND
//        strokeJoin = Paint.Join.ROUND
//        strokeWidth = dp(0.65f)
//        alpha = 35
//    }
//
//    private val frameRect = RectF()
//    private val clipPath = Path()
//
//    init {
//        setWillNotDraw(false)
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//
//        clipChildren = false
//        clipToPadding = false
//    }
//
//    fun setEffectEnabled(enabled: Boolean) {
//        if (effectEnabled == enabled) return
//
//        effectEnabled = enabled
//        invalidate()
//    }
//
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        if (!effectEnabled) return
//
//        val w = width.toFloat()
//        val h = height.toFloat()
//        if (w <= dp(12f) || h <= dp(12f)) return
//
//        val inset = dp(4f)
//        val radius = dp(12f)
//
//        frameRect.set(
//            inset,
//            inset,
//            w - inset,
//            h - inset
//        )
//
//        drawRoundedSmoke(canvas, frameRect, radius)
//
//        canvas.drawRoundRect(
//            frameRect, radius, radius, outerGlowPaint
//        )
//        canvas.drawRoundRect(
//            frameRect, radius, radius, glowPaint
//        )
//
//        canvas.drawRoundRect(
//            frameRect, radius, radius, framePaint
//        )
//    }
//
//
//    private fun drawRoundedSmoke(
//        canvas: Canvas,
//        rect: RectF,
//        radius: Float
//    ) {
//        val random = kotlin.random.Random(2417)
//
//        val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.WHITE
//            style = Paint.Style.STROKE
//            strokeCap = Paint.Cap.ROUND
//            strokeJoin = Paint.Join.ROUND
//            strokeWidth = dp(5f)
//            alpha = 15
//            maskFilter = BlurMaskFilter(
//                dp(10f),
//                BlurMaskFilter.Blur.NORMAL
//            )
//        }
//
//        val midPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.WHITE
//            style = Paint.Style.STROKE
//            strokeCap = Paint.Cap.ROUND
//            strokeJoin = Paint.Join.ROUND
//            strokeWidth = dp(2f)
//            alpha = 24
//            maskFilter = BlurMaskFilter(
//                dp(4f),
//                BlurMaskFilter.Blur.NORMAL
//            )
//        }
//
//        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.WHITE
//            style = Paint.Style.STROKE
//            strokeCap = Paint.Cap.ROUND
//            strokeJoin = Paint.Join.ROUND
//            strokeWidth = dp(0.7f)
//            alpha = 24
//        }
//
//        val base = RectF(
//            rect.left - dp(2f),
//            rect.top - dp(2f),
//            rect.right + dp(2f),
//            rect.bottom + dp(2f)
//        )
//
//        val baseRadius = radius + dp(2f)
//
//        val sideLength = floatArrayOf(
//            base.width() - 2 * baseRadius,
//            base.height() - 2 * baseRadius,
//            base.width() - 2 * baseRadius,
//            base.height() - 2 * baseRadius
//        )
//
//        val paths = mutableListOf<Path>()
//
//        for (side in 0..3) {
//            val count = (sideLength[side] / dp(5f))
//                .toInt()
//                .coerceAtLeast(8)
//
//            repeat(count) {
//                val t = random.nextFloat()
//                val offset = dp(
//                    random.nextInt(-9, 10).toFloat()
//                )
//                val length = dp(
//                    random.nextInt(12, 36).toFloat()
//                )
//                val amplitude = dp(
//                    random.nextInt(3, 12).toFloat()
//                )
//
//                val path = Path()
//
//                fun point(position: Float, normal: Float): Pair<Float, Float> {
//                    return when (side) {
//                        0 -> Pair(
//                            base.left + baseRadius + position,
//                            base.top + normal
//                        )
//
//                        1 -> Pair(
//                            base.right + normal,
//                            base.top + baseRadius + position
//                        )
//
//                        2 -> Pair(
//                            base.right - baseRadius - position,
//                            base.bottom + normal
//                        )
//
//                        else -> Pair(
//                            base.left + normal,
//                            base.bottom - baseRadius - position
//                        )
//                    }
//                }
//
//                val start = t * sideLength[side]
//                val p0 = point(start, offset)
//                path.moveTo(p0.first, p0.second)
//
//                val segments = 3
//                val segmentLength = length / segments
//                var current = start
//
//                repeat(segments) {
//                    val p1 = point(
//                        current + segmentLength * 0.3f,
//                        offset + (random.nextFloat() * 2f - 1f) * amplitude
//                    )
//                    val p2 = point(
//                        current + segmentLength * 0.7f,
//                        offset + (random.nextFloat() * 2f - 1f) * amplitude
//                    )
//                    val p3 = point(
//                        current + segmentLength,
//                        offset + (random.nextFloat() * 2f - 1f) * amplitude
//                    )
//
//                    path.cubicTo(
//                        p1.first, p1.second,
//                        p2.first, p2.second,
//                        p3.first, p3.second
//                    )
//
//                    current += segmentLength
//                }
//
//                paths.add(path)
//            }
//        }
//
//        val corners = listOf(
//            Pair(base.left + baseRadius, base.top + baseRadius),
//            Pair(base.right - baseRadius, base.top + baseRadius),
//            Pair(base.right - baseRadius, base.bottom - baseRadius),
//            Pair(base.left + baseRadius, base.bottom - baseRadius)
//        )
//
//        corners.forEach { (cx, cy) ->
//            repeat(12) {
//                val path = Path()
//                val startAngle = random.nextFloat() * 90f
//                val direction = if (random.nextBoolean()) 1f else -1f
//                val r = baseRadius + dp(
//                    random.nextInt(-5, 10).toFloat()
//                )
//
//                val angle = Math.toRadians(startAngle.toDouble())
//                path.moveTo(
//                    cx + kotlin.math.cos(angle).toFloat() * r,
//                    cy + kotlin.math.sin(angle).toFloat() * r
//                )
//
//                val a1 = Math.toRadians(
//                    (startAngle + direction * 25f).toDouble()
//                )
//                val a2 = Math.toRadians(
//                    (startAngle + direction * 55f).toDouble()
//                )
//                val a3 = Math.toRadians(
//                    (startAngle + direction * 75f).toDouble()
//                )
//
//                path.cubicTo(
//                    cx + kotlin.math.cos(a1).toFloat() * (r + dp(5f)),
//                    cy + kotlin.math.sin(a1).toFloat() * (r + dp(5f)),
//                    cx + kotlin.math.cos(a2).toFloat() * (r - dp(4f)),
//                    cy + kotlin.math.sin(a2).toFloat() * (r - dp(4f)),
//                    cx + kotlin.math.cos(a3).toFloat() * r,
//                    cy + kotlin.math.sin(a3).toFloat() * r
//                )
//
//                paths.add(path)
//            }
//        }
//
//        paths.forEach { canvas.drawPath(it, cloudPaint) }
//        paths.forEach { canvas.drawPath(it, midPaint) }
//        paths.forEachIndexed { index, path ->
//            if (index % 3 == 0) {
//                canvas.drawPath(path, detailPaint)
//            }
//        }
//    }
//
//
//    private fun drawSmoke(canvas: Canvas, rect: RectF) {
//        /*
//         * Четыре стороны рамки:
//         * 0 — верхняя,
//         * 1 — правая,
//         * 2 — нижняя,
//         * 3 — левая.
//         *
//         * Seed фиксирован: дым статический и не меняет
//         * форму при каждом invalidate().
//         */
//        val random = Random(2417)
//
//        val wisps = mutableListOf<SmokeWisp>()
//
//        val edgeLengths = floatArrayOf(
//            rect.width(),
//            rect.height(),
//            rect.width(),
//            rect.height()
//        )
//
//        for (side in 0..3) {
//            val edgeLength = edgeLengths[side]
//
//            val count = max(
//                12,
//                (edgeLength / dp(8f)).toInt()
//            )
//
//            repeat(count) {
//                val t = random.nextFloat() * edgeLength
//                val length = dp(
//                    random.nextInt(18, 58).toFloat()
//                )
//
//                val offset = dp(
//                    random.nextInt(-9, 10).toFloat()
//                )
//
//                wisps.add(
//                    createWisp(
//                        rect = rect,
//                        side = side,
//                        start = t,
//                        length = length,
//                        offset = offset,
//                        amplitude = dp(
//                            random.nextInt(3, 13).toFloat()
//                        ),
//                        segments = random.nextInt(2, 5),
//                        random = random
//                    )
//                )
//            }
//        }
//
//        for (corner in 0..3) {
//            repeat(12) {
//                val side = if (random.nextBoolean()) {
//                    corner
//                } else {
//                    (corner + 3) % 4
//                }
//
//                val edgeLength = edgeLengths[side]
//
//                val start = when (corner) {
//                    0 -> if (side == 0) {
//                        random.nextFloat() * rect.width() * 0.35f
//                    } else {
//                        rect.height() * (
//                                0.65f + random.nextFloat() * 0.35f
//                                )
//                    }
//
//                    1 -> if (side == 0) {
//                        rect.width() * (
//                                0.65f + random.nextFloat() * 0.35f
//                                )
//                    } else {
//                        random.nextFloat() * rect.height() * 0.35f
//                    }
//
//                    2 -> if (side == 2) {
//                        rect.width() * (
//                                0.65f + random.nextFloat() * 0.35f
//                                )
//                    } else {
//                        rect.height() * (
//                                0.65f + random.nextFloat() * 0.35f
//                                )
//                    }
//
//                    else -> if (side == 2) {
//                        random.nextFloat() * rect.width() * 0.35f
//                    } else {
//                        random.nextFloat() * rect.height() * 0.35f
//                    }
//                }.coerceIn(0f, edgeLength)
//
//                wisps.add(
//                    createWisp(
//                        rect = rect,
//                        side = side,
//                        start = start,
//                        length = dp(
//                            random.nextInt(12, 38).toFloat()
//                        ),
//                        offset = dp(
//                            random.nextInt(-7, 8).toFloat()
//                        ),
//                        amplitude = dp(
//                            random.nextInt(4, 15).toFloat()
//                        ),
//                        segments = random.nextInt(2, 4),
//                        random = random
//                    )
//                )
//            }
//        }
//
//        wisps.forEach { wisp ->
//            smokeCloudPaint.alpha = wisp.cloudAlpha
//            smokeCloudPaint.strokeWidth = wisp.cloudWidth
//            canvas.drawPath(wisp.path, smokeCloudPaint)
//        }
//
//        wisps.forEach { wisp ->
//            smokeMidPaint.alpha = wisp.midAlpha
//            smokeMidPaint.strokeWidth = wisp.midWidth
//            canvas.drawPath(wisp.path, smokeMidPaint)
//        }
//
//        wisps.forEach { wisp ->
//            if (wisp.drawDetail) {
//                smokeDetailPaint.alpha = wisp.detailAlpha
//                smokeDetailPaint.strokeWidth = wisp.detailWidth
//                canvas.drawPath(wisp.path, smokeDetailPaint)
//            }
//        }
//
//        smokeCloudPaint.alpha = 18
//        smokeCloudPaint.strokeWidth = dp(5f)
//
//        smokeMidPaint.alpha = 35
//        smokeMidPaint.strokeWidth = dp(2f)
//
//        smokeDetailPaint.alpha = 35
//        smokeDetailPaint.strokeWidth = dp(0.65f)
//    }
//
//    private data class SmokeWisp(
//        val path: Path,
//        val cloudAlpha: Int,
//        val midAlpha: Int,
//        val detailAlpha: Int,
//        val cloudWidth: Float,
//        val midWidth: Float,
//        val detailWidth: Float,
//        val drawDetail: Boolean
//    )
//
//    private fun createWisp(
//        rect: RectF,
//        side: Int,
//        start: Float,
//        length: Float,
//        offset: Float,
//        amplitude: Float,
//        segments: Int,
//        random: Random
//    ): SmokeWisp {
//        val path = Path()
//
//        fun point(t: Float, n: Float): Pair<Float, Float> {
//            return when (side) {
//                0 -> Pair(rect.left + t, rect.top + n)
//
//                1 -> Pair(rect.right + n, rect.top + t)
//
//                2 -> Pair(rect.right - t, rect.bottom + n)
//
//                else -> Pair(rect.left + n, rect.bottom - t)
//            }
//        }
//
//        fun normalOffset(): Float {
//            return offset +
//                    (random.nextFloat() * 2f - 1f) * amplitude
//        }
//
//        val startPoint = point(start, normalOffset())
//        path.moveTo(startPoint.first, startPoint.second)
//
//        val segmentLength = length / segments
//        var currentT = start
//
//        repeat(segments) {
//            val nextT = currentT + segmentLength
//
//            val p1 = point(
//                currentT + segmentLength * 0.3f,
//                normalOffset()
//            )
//
//            val p2 = point(
//                currentT + segmentLength * 0.7f,
//                normalOffset()
//            )
//
//            val p3 = point(nextT, normalOffset())
//
//            path.cubicTo(
//                p1.first, p1.second,
//                p2.first, p2.second,
//                p3.first, p3.second
//            )
//
//            currentT = nextT
//        }
//
//        return SmokeWisp(
//            path = path,
//            cloudAlpha = random.nextInt(5, 18),
//            midAlpha = random.nextInt(12, 35),
//            detailAlpha = random.nextInt(8, 28),
//            cloudWidth = dp(
//                random.nextInt(3, 8).toFloat()
//            ),
//            midWidth = dp(
//                random.nextInt(1, 4).toFloat()
//            ),
//            detailWidth = dp(
//                random.nextInt(1, 2).toFloat()
//            ),
//            drawDetail = random.nextInt(100) < 65
//        )
//    }
//}
