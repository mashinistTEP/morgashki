package com.mashinist_tep70bs_145.morgashki.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.roundToInt

/**
 * ЭМУ с медленной анимацией смены надписи.
 *
 * При смене текста:
 *   1) новая надпись медленно появляется;
 *   2) затем старая быстрее стирается.
 *
 * При выключении ЭМУ используется отдельная анимация стирания:
 * никакой дополнительной фазы появления нет.
 *
 * Если анимация прерывается, новая операция начинается от приблизительно
 * видимой в этот момент строки, поэтому целевой текст не "дописывается"
 * мгновенно.
 */
class DisplayBoardView(context: Context) : View(context) {

    companion object {
        private const val REVEAL_DURATION_MS = 3600L
        private const val ERASE_DURATION_MS = 1800L
    }

    var typeface: Typeface? = null
        set(value) {
            field = value
            paint.typeface = value
            invalidate()
        }

    var textColor: Int = 0xFFC4D65A.toInt()
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    /** Горизонтальное сжатие букв: меньше 1f = уже. */
    var textScaleX: Float = 0.62f
        set(value) {
            field = value
            paint.textScaleX = value
            invalidate()
        }

    /** Сдвиг текста вправо относительно левого края View. */
    var textOffsetX: Float = 0f
        set(value) {
            field = value
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
        textScaleX = 0.62f
    }

    private var currentText = ""
    private var previousText = ""

    /** true — обычная смена, false — специальное стирание при выключении. */
    private var clearMode = false

    private var animator: ValueAnimator? = null
    private var animProgress = 1f

    fun setTextInstant(text: String) {
        animator?.cancel()
        animator = null
        clearMode = false
        currentText = text
        previousText = text
        animProgress = 1f
        invalidate()
    }

    /**
     * Плавная смена надписи.
     */
    fun setTextAnimated(newText: String) {
        if (newText == currentText && animator == null && !clearMode) {
            return
        }

        val visibleNow = getVisibleTextApprox()

        animator?.cancel()
        animator = null
        clearMode = false

        previousText = visibleNow
        currentText = newText
        animProgress = 0f

        startAnimator(REVEAL_DURATION_MS + ERASE_DURATION_MS)
    }

    /**
     * Плавно выключает ЭМУ.
     *
     * Важный момент: при выключении сразу запускается ТОЛЬКО стирание.
     * Поэтому если табло уже полностью отображает текст, оно не исчезает
     * мгновенно и не ждёт фазу появления.
     */
    fun clearAnimated() {
        val visibleNow = getVisibleTextApprox()

        animator?.cancel()
        animator = null

        if (visibleNow.isEmpty()) {
            clearMode = false
            currentText = ""
            previousText = ""
            animProgress = 1f
            invalidate()
            return
        }

        clearMode = true
        previousText = visibleNow
        currentText = ""
        animProgress = 0f

        startAnimator(ERASE_DURATION_MS)
    }

    private fun startAnimator(durationMs: Long) {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()

            addUpdateListener {
                animProgress = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animator === animation) {
                        animator = null
                        if (clearMode) {
                            previousText = ""
                            currentText = ""
                        } else {
                            previousText = currentText
                        }
                        animProgress = 1f
                        invalidate()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    if (animator === animation) {
                        animator = null
                    }
                }
            })

            start()
        }
    }

    /**
     * Возвращает строку, приблизительно соответствующую тому, что реально
     * видно на экране в текущий момент анимации.
     */
    private fun getVisibleTextApprox(): String {
        if (animator == null || animProgress >= 1f) {
            return currentText
        }

        if (clearMode) {
            return previousText.take(
                (previousText.length * (1f - animProgress))
                    .coerceIn(0f, 1f)
                    .let { (previousText.length * it).roundToInt() }
            )
        }

        val total = (REVEAL_DURATION_MS + ERASE_DURATION_MS).toFloat()
        val revealEnd = REVEAL_DURATION_MS / total

        return if (animProgress < revealEnd) {
            val fraction = (animProgress / revealEnd).coerceIn(0f, 1f)
            currentText.take(
                (currentText.length * fraction).roundToInt()
            )
        } else {
            val eraseFraction =
                ((animProgress - revealEnd) / (1f - revealEnd))
                    .coerceIn(0f, 1f)

            previousText.take(
                (previousText.length * (1f - eraseFraction)).roundToInt()
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val baseline =
            height / 2f - (paint.descent() + paint.ascent()) / 2f
        val x = textOffsetX

        // Обычное стабильное состояние.
        if (animProgress >= 1f || previousText == currentText) {
            canvas.drawText(currentText, x, baseline, paint)
            return
        }

        // Выключение ЭМУ: сразу стираем текущую видимую строку.
        if (clearMode) {
            val eraseFraction = animProgress.coerceIn(0f, 1f)
            val oldWidth = paint.measureText(previousText)
            val eraseFromX = x + oldWidth * eraseFraction

            canvas.save()
            canvas.clipRect(
                eraseFromX,
                0f,
                width.toFloat(),
                height.toFloat()
            )
            canvas.drawText(previousText, x, baseline, paint)
            canvas.restore()
            return
        }

        val total = (REVEAL_DURATION_MS + ERASE_DURATION_MS).toFloat()
        val revealEnd = REVEAL_DURATION_MS / total

        // Новая надпись появляется медленно.
        val newRevealFraction =
            (animProgress / revealEnd).coerceIn(0f, 1f)

        // Старая стирается быстрее.
        val oldEraseFraction =
            ((animProgress - revealEnd) / (1f - revealEnd))
                .coerceIn(0f, 1f)

        if (oldEraseFraction < 1f && previousText.isNotEmpty()) {
            val oldWidth = paint.measureText(previousText)
            val eraseFromX = x + oldWidth * oldEraseFraction

            canvas.save()
            canvas.clipRect(
                eraseFromX,
                0f,
                width.toFloat(),
                height.toFloat()
            )
            canvas.drawText(previousText, x, baseline, paint)
            canvas.restore()
        }

        if (newRevealFraction > 0f && currentText.isNotEmpty()) {
            val newWidth = paint.measureText(currentText)
            val revealToX = x + newWidth * newRevealFraction

            canvas.save()
            canvas.clipRect(
                x,
                0f,
                revealToX,
                height.toFloat()
            )
            canvas.drawText(currentText, x, baseline, paint)
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
