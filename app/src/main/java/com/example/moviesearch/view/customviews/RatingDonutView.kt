package com.example.moviesearch.view.customviews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.moviesearch.R

class RatingDonutView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {

    // Овал для рисования сегментов прогресс бара
    private val oval = RectF()

    // Координаты центра View, а также Radius
    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    // Толщина линии прогресса
    private var stroke = 10f

    // Значение прогресса от 0 - 100
    private var animatedProgress: Float = 0f

    // Фактическое значение прогресса
    private var progress = 0

    // Значения размера текста внутри кольца
    private var scaleSize = 60f

    // Краски для наших фигур
    private lateinit var strokePaint: Paint
    private lateinit var digitPaint: Paint
    private lateinit var circlePaint: Paint
    private lateinit var textPaint: Paint

    // Аниматор для прогресса
    private var progressAnimator: ValueAnimator? = null

    init {
        // Получаем атрибуты и устанавливаем их в соответствующие поля
        val a = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.RatingDonutView,
            0,
            0
        )
        try {
            stroke = a.getFloat(R.styleable.RatingDonutView_stroke, stroke)
            progress = a.getInt(R.styleable.RatingDonutView_progress, progress)
            animatedProgress = 0f
        } finally {
            a.recycle()
        }
        // Инициализируем первоначальные краски
        initPaint()
    }

    private fun initPaint() {
        // Краска для колец
        strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            color = getPaintColor(progress)
            strokeCap = Paint.Cap.ROUND // Для скругленных концов
            isAntiAlias = true
        }

        // Краска для цифр
        digitPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
            setShadowLayer(5f, 0f, 0f, Color.DKGRAY)
            textSize = scaleSize
            typeface = Typeface.SANS_SERIF
            color = getPaintColor(progress)
            isAntiAlias = true
        }

        // Краска для текста (отдельная для анимированного значения)
        textPaint = Paint().apply { // инициализация textPaint
            style = Paint.Style.FILL
            strokeWidth = 2f
            textSize = scaleSize
            typeface = Typeface.SANS_SERIF
            color = getPaintColor(progress)
            isAntiAlias = true
        }

        // Краска для заднего фона
        circlePaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.DKGRAY
            isAntiAlias = true
        }
    }

    private fun getPaintColor(progress: Int): Int = when (progress) {
        in 0..25 -> Color.parseColor("#e84258")
        in 26..50 -> Color.parseColor("#fd8060")
        in 51..75 -> Color.parseColor("#fee191")
        else -> Color.parseColor("#b0d8a4")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = if (width > height) {
            height.div(2f)
        } else {
            width.div(2f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val minSide = Math.min(chosenWidth, chosenHeight)
        centerX = minSide.div(2f)
        centerY = minSide.div(2f)

        setMeasuredDimension(minSide, minSide)
    }

    private fun chooseDimension(mode: Int, size: Int) = when (mode) {
        MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
        else -> 300
    }

    override fun onDraw(canvas: Canvas) {
        // Рисуем кольцо и задний фон
        drawRating(canvas)
        // Рисуем цифры
        drawText(canvas)
    }

    private fun drawRating(canvas: Canvas) {
        // Здесь мы можем регулировать размер нашего кольца
        val scale = radius * 0.8f
        // Сохраняем канвас
        canvas.save()
        // Перемещаем нулевые координаты канваса в центр
        canvas.translate(centerX, centerY)
        // Устанавливаем размеры под наш овал
        oval.set(0f - scale, 0f - scale, scale, scale)
        // Рисуем задний фон
        canvas.drawCircle(0f, 0f, radius, circlePaint)
        //  Рисуем "арки" с анимированным прогрессом
        canvas.drawArc(oval, -90f, convertProgressToDegrees(animatedProgress), false, strokePaint)
        // Восстанавливаем канвас
        canvas.restore()
    }

    private fun convertProgressToDegrees(progress: Float): Float = progress * 3.6f

    private fun drawText(canvas: Canvas) {
        // Форматируем текст с анимированным значением
        val message = String.format("%.1f", animatedProgress / 10f)
        // Получаем ширину текста для центрирования
        val textWidth = textPaint.measureText(message)
        val textHeight = textPaint.descent() - textPaint.ascent()

        // Рисуем наш текст по центру
        canvas.drawText(
            message,
            centerX - textWidth / 2,
            centerY + textHeight / 2 - textPaint.descent(),
            textPaint
        )
    }

    fun setProgress(pr: Int?, animate: Boolean = true) {
        // Останавливаем предыдущую анимацию
        progressAnimator?.cancel()

        // Сохраняем новое целевое значение
        progress = (pr?.coerceIn(0, 100) ?:

        // Обновляем цвета красок
        updatePaintColors()) as Int

        if (animate) {
            // Запускаем анимацию
            progressAnimator = ValueAnimator.ofFloat(animatedProgress, progress.toFloat()).apply {
                duration = 1000L // Длительность анимации 1 секунда
                interpolator = DecelerateInterpolator() // Замедление в конце
                addUpdateListener { animation ->
                    animatedProgress = animation.animatedValue as Float
                    invalidate() // Перерисовываем View при каждом обновлении анимации
                }
                start()
            }
        } else {
            // Без анимации - сразу устанавливаем значение
            animatedProgress = progress.toFloat()
            invalidate()
        }
    }

    private fun updatePaintColors() {
        val color = getPaintColor(progress)
        strokePaint.color = color
        digitPaint.color = color
        textPaint.color = color
    }

    // Метод для установки прогресса без анимации (для XML)
    fun setProgress(pr: Int) {
        setProgress(pr, animate = false)
    }

    // Получение текущего прогресса
    fun getProgress(): Int = progress

    // Очистка ресурсов
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
    }
}