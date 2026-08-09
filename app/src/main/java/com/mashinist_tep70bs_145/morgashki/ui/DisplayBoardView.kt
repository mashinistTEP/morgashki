package com.mashinist_tep70bs_145.morgashki.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Табло МВПС с анимацией смены текста "по столбику".
 *
 * Анимация имитирует старое ЖК/светодиодное табло: новая надпись появляется
 * слева направо (столбик за столбиком), а когда появилась примерно половина
 * новой надписи, старая надпись начинает стираться тоже слева направо —
 * так что в момент завершения анимации старый текст полностью стёрт,
 * а новый полностью виден.
 *
 * Реализовано прямым рисованием на Canvas с обрезкой (clipRect), а не через
 * два наложенных TextView, чтобы гарантированно получить контроль над тем,
 * какая часть текста видна в каждый момент анимации.
 */
class DisplayBoardView(context: Context) : View(context) {

    companion object {
        private const val ANIM_DURATION_MS = 650L
    }

    var typeface: Typeface? = null
        set(value) {
            field = value
            paint.typeface = value
            invalidate()
        }

    var textColor: Int = 0xFFE8B33D.toInt()
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    var textSizePx: Float = 32f
        set(value) {
            field = value
            paint.textSize = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = textSizePx
    }

    private var currentText: String = ""
    private var previousText: String = ""
    private var animator: ValueAnimator? = null
    private var animProgress: Float = 1f // 1f = анимация завершена, показывается currentText целиком

    /**
     * Немедленно показывает текст без анимации (используется при первом
     * появлении табло — оно должно быть "погашено", то есть пустым).
     */
    fun setTextInstant(text: String) {
        animator?.cancel()
        currentText = text
        previousText = text
        animProgress = 1f
        invalidate()
    }

    /**
     * Запускает анимацию смены текста "по столбику" с previousText на newText.
     */
    fun setTextAnimated(newText: String) {
        if (newText == currentText) return

        animator?.cancel()
        previousText = currentText
        currentText = newText
        animProgress = 0f

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIM_DURATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener {
                animProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val baseline = height / 2f - (paint.descent() + paint.ascent()) / 2f

        if (animProgress >= 1f || previousText == currentText) {
            // анимация не идёт — просто рисуем текущий текст целиком
            canvas.drawText(currentText, 0f, baseline, paint)
            return
        }

        // Новая надпись "печатается" слева направо в первой половине анимации
        val newTextRevealFraction = (animProgress * 2f).coerceIn(0f, 1f)
        // Старая надпись начинает стираться слева, когда новая раскрылась наполовину
        val oldTextEraseFraction = ((animProgress - 0.5f) * 2f).coerceIn(0f, 1f)

        // сначала рисуем оставшуюся часть старого текста (справа от точки стирания)
        if (oldTextEraseFraction < 1f) {
            val oldWidth = paint.measureText(previousText)
            val eraseFromX = oldWidth * oldTextEraseFraction
            canvas.save()
            canvas.clipRect(eraseFromX, 0f, width.toFloat(), height.toFloat())
            canvas.drawText(previousText, 0f, baseline, paint)
            canvas.restore()
        }

        // затем поверх рисуем открытую часть нового текста (слева от точки раскрытия)
        if (newTextRevealFraction > 0f) {
            val newWidth = paint.measureText(currentText)
            val revealToX = newWidth * newTextRevealFraction
            canvas.save()
            canvas.clipRect(0f, 0f, revealToX, height.toFloat())
            canvas.drawText(currentText, 0f, baseline, paint)
            canvas.restore()
        }
    }
}
