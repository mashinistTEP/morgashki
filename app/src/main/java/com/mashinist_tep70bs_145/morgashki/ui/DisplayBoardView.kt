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
 * Табло МВПС с медленной анимацией смены текста "по столбику".
 *
 * Новая надпись появляется медленнее, чем стирается старая.
 * Текущая анимация всегда может быть отменена без мгновенного
 * дописывания целевой строки.
 */
class DisplayBoardView(context: Context) : View(context) {

    companion object {
        // Появление должно быть особенно медленным.
        private const val REVEAL_DURATION_MS = 3600L

        // Стирание быстрее появления, но всё ещё заметно медленное.
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

    var textSizePx: Float = 32f
        set(value) {
            field = value
            paint.textSize = value
            invalidate()
        }

    /**
     * Сжатие букв по горизонтали.
     * Меньше 1f = текст уже.
     */
    var textScaleX: Float = 0.62f
        set(value) {
            field = value
            paint.textScaleX = value
            invalidate()
        }

    /**
     * Горизонтальный сдвиг текста вправо.
     */
    var textOffsetX: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = textSizePx
        textScaleX = 0.62f
    }

    private var currentText = ""
    private var previousText = ""

    private var animator: ValueAnimator? = null
    private var animProgress = 1f

    fun setTextInstant(text: String) {
        animator?.cancel()
        animator = null
        currentText = text
        previousText = text
        animProgress = 1f
        invalidate()
    }

    /**
     * Смена текста с анимацией.
     *
     * Если предыдущая анимация ещё идёт, берём видимую на данный
     * момент часть текста и начинаем новую анимацию от неё.
     */
    fun setTextAnimated(newText: String) {
        if (newText == currentText && animator == null) {
            return
        }

        val visibleNow = getVisibleTextApprox()

        animator?.cancel()
        animator = null

        previousText = visibleNow
        currentText = newText
        animProgress = 0f

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = REVEAL_DURATION_MS + ERASE_DURATION_MS
            interpolator = LinearInterpolator()

            addUpdateListener {
                animProgress = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animator === animation) {
                        animator = null
                        previousText = currentText
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
     * Выключение ЭМУ.
     *
     * Текущая анимация отменяется, после чего стирается именно
     * тот текст, который был реально виден в момент выключения.
     * Поэтому старая целевая строка не дописывается мгновенно.
     */
    fun clearAnimated() {
        val visibleNow = getVisibleTextApprox()

        animator?.cancel()
        animator = null

        if (visibleNow.isEmpty()) {
            setTextInstant("")
            return
        }

        previousText = visibleNow
        currentText = ""
        animProgress = 0f

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ERASE_DURATION_MS
            interpolator = LinearInterpolator()

            addUpdateListener {
                animProgress = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animator === animation) {
                        animator = null
                        previousText = ""
                        currentText = ""
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
     * Получает приблизительно видимую строку в текущий момент.
     * Нужна для корректной отмены анимации.
     */
    private fun getVisibleTextApprox(): String {
        if (animator == null || animProgress >= 1f) {
            return currentText
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

        if (width == 0 || height == 0) {
            return
        }

        val baseline =
            height / 2f -
                (paint.descent() + paint.ascent()) / 2f

        val x = textOffsetX

        if (animProgress >= 1f || previousText == currentText) {
            canvas.drawText(currentText, x, baseline, paint)
            return
        }

        // Отдельный режим выключения ЭМУ: currentText пустой,
        // поэтому стираем предыдущую надпись сразу по шкале ERASE_DURATION_MS.
        if (currentText.isEmpty() && previousText.isNotEmpty()) {
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

        // Старая надпись стирается быстрее.
        val oldEraseFraction =
            ((animProgress - revealEnd) / (1f - revealEnd))
                .coerceIn(0f, 1f)

        // Сначала старая надпись.
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

        // Затем новая надпись.
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
