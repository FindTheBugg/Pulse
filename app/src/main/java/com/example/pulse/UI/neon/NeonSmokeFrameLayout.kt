package com.example.pulse.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class NeonSmokeFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private fun dp(value: Float): Float = value * density

    private var effectEnabled = false
    private var effectAlpha = 0f
    private var effectBitmap: Bitmap? = null
    private var fadeAnimator: ValueAnimator? = null
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    init {
        setWillNotDraw(false)
    }

    // Включить или выключить эффект (вызывается из адаптера)
    fun setEffectEnabled(enabled: Boolean) {
        if (effectEnabled == enabled) return
        effectEnabled = enabled

        fadeAnimator?.cancel()
        if (enabled) {
            fadeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 350
                addUpdateListener {
                    effectAlpha = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            effectAlpha = 0f
            invalidate()
        }
    }

    // Размер изменился: старая картинка не подходит, нарисуем новую при следующей отрисовке
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        effectBitmap?.recycle()
        effectBitmap = null
    }

    override fun onDetachedFromWindow() {
        fadeAnimator?.cancel()
        effectAlpha = if (effectEnabled) 1f else 0f
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!effectEnabled) return

        val bitmap = effectBitmap ?: buildEffectBitmap()?.also { effectBitmap = it } ?: return
        bitmapPaint.alpha = (effectAlpha * 255).toInt()
        canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
    }

    // Рисуем эффект один раз в картинку размером с эту ячейку
    private fun buildEffectBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val inset = dp(12f)
        val rect = RectF(inset, inset, width - inset, height - inset)
        if (rect.width() <= 0f || rect.height() <= 0f) return bitmap

        val radius = min(dp(12f), rect.height() / 2f)
        val outline = Path().apply {
            addRoundRect(rect, radius, radius, Path.Direction.CW)
        }

        drawSmoke(canvas, outline)   // сначала дым (он под линией)
        drawCore(canvas, outline)    // потом свечение и яркая линия сверху
        return bitmap
    }

    // Светящаяся линия: несколько слоёв от широкого мягкого к тонкому острому
    private fun drawCore(canvas: Canvas, outline: Path) {
        canvas.drawPath(outline, strokePaint(dp(6f), dp(10f), 70))   // широкий ореол
        canvas.drawPath(outline, strokePaint(dp(3.5f), dp(4f), 150)) // среднее свечение
        canvas.drawPath(outline, strokePaint(dp(2f), dp(1.5f), 255)) // внутреннее свечение
        canvas.drawPath(outline, strokePaint(dp(1.1f), 0f, 255))     // острое белое ядро
    }

    // Дым: сотни тонких волнистых нитей вдоль линии
    private fun drawSmoke(canvas: Canvas, outline: Path) {
        val measure = PathMeasure(outline, true)
        val perimeter = measure.length
        if (perimeter <= 0f) return

        val random = Random(2417) // фиксированное число: дым всегда одинаковый
        val count = (perimeter / dp(4f)).toInt().coerceAtLeast(20)
        val strands = List(count) { createStrand(measure, perimeter, random) }

        val cloudPaint = strokePaint(dp(5f), dp(8f), 14)
        val midPaint = strokePaint(dp(1.8f), dp(3f), 30)
        val detailPaint = strokePaint(dp(0.6f), 0f, 70)

        // Слой 1: облачка (широкие, размытые, едва заметные)
        strands.forEachIndexed { i, path ->
            cloudPaint.alpha = 8 + (i * 37) % 14   // у каждой нити своя яркость
            canvas.drawPath(path, cloudPaint)
        }
        // Слой 2: средние волокна
        strands.forEachIndexed { i, path ->
            midPaint.alpha = 16 + (i * 53) % 28
            canvas.drawPath(path, midPaint)
        }
        // Слой 3: тонкие чёткие прожилки (не у всех нитей)
        strands.forEachIndexed { i, path ->
            if (i % 2 == 0) {
                detailPaint.alpha = 35 + (i * 29) % 50
                canvas.drawPath(path, detailPaint)
            }
        }
    }

    // Одна нить дыма: идёт вдоль линии и качается в стороны по волне
    private fun createStrand(measure: PathMeasure, perimeter: Float, random: Random): Path {
        val length = randomIn(random, dp(30f), dp(110f))
        val start = random.nextFloat() * perimeter
        // Сумма двух случайных чисел: большинство нитей близко к линии, а далёких мало
        val baseOffset = (random.nextFloat() + random.nextFloat() - 1f) * dp(6f)
        val amplitude = randomIn(random, dp(2f), dp(5f))
        val k = (2 * PI).toFloat() / randomIn(random, dp(14f), dp(40f))
        val phase1 = random.nextFloat() * (2 * PI).toFloat()
        val phase2 = random.nextFloat() * (2 * PI).toFloat()

        val steps = (length / dp(3f)).toInt().coerceAtLeast(4)
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val path = Path()

        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val travelled = start + t * length
            // Точка на линии и направление движения в ней
            measure.getPosTan(travelled % perimeter, pos, tan)

            // Концы нити сходят на нет, середина колышется сильнее
            val envelope = sin(PI * t).toFloat()
            val wave = sin(k * travelled + phase1) + 0.5f * sin(2.3f * k * travelled + phase2)
            val offset = baseOffset + envelope * amplitude * wave

            // Сдвиг перпендикулярно линии (нормаль = направление, повёрнутое на 90°)
            val x = pos[0] - tan[1] * offset
            val y = pos[1] + tan[0] * offset
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        return path
    }

    private fun randomIn(random: Random, from: Float, to: Float): Float =
        from + random.nextFloat() * (to - from)

    private fun strokePaint(width: Float, blur: Float, alpha: Int) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = width
            this.alpha = alpha
            if (blur > 0f) maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
        }
}