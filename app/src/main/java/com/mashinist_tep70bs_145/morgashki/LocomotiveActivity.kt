package com.mashinist_tep70bs_145.morgashki

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.doOnLayout
import com.mashinist_tep70bs_145.morgashki.model.Locomotive
import com.mashinist_tep70bs_145.morgashki.model.LocomotiveCatalog
import com.mashinist_tep70bs_145.morgashki.model.ToggleButton
import kotlin.math.roundToInt

class LocomotiveActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CODE = "extra_locomotive_code"
        private const val FADE_DURATION_MS = 160L
    }

    private lateinit var locomotive: Locomotive
    private lateinit var fonHolder: FrameLayout
    private lateinit var imgFon: ImageView

    // id кнопки -> список ImageView слоёв, которые она включает
    private val layerViewsByButton = mutableMapOf<String, List<ImageView>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locomotive)

        val code = intent.getStringExtra(EXTRA_CODE)
        val loco = code?.let { LocomotiveCatalog.byCode(it) }
        if (loco == null) {
            finish()
            return
        }
        locomotive = loco

        findViewById<TextView>(R.id.tvTitle).text = locomotive.displayName
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        fonHolder = findViewById(R.id.fonHolder)
        imgFon = findViewById(R.id.imgFon)

        setupFon()
        setupButtons()
    }

    private fun setupFon() {
        val fonRes = locomotive.fonRes
        if (fonRes != null) {
            imgFon.setImageResource(fonRes)
        }

        // Ограничиваем ширину fonHolder так, чтобы соотношение сторон
        // соответствовало исходному фону (аналог aspectRatio в ConstraintLayout,
        // но без лишней зависимости от размеров экрана).
        val stageContainer = findViewById<View>(R.id.stageContainer)
        stageContainer.doOnLayout {
            val containerH = stageContainer.height
            val containerW = stageContainer.width
            val desiredW = (containerH * locomotive.fonAspectRatio).roundToInt()
            val finalW = if (desiredW <= containerW) desiredW else containerW

            val lp = fonHolder.layoutParams
            lp.width = finalW
            lp.height = if (desiredW <= containerW) ViewGroup.LayoutParams.MATCH_PARENT
                        else (containerW / locomotive.fonAspectRatio).roundToInt()
            fonHolder.layoutParams = lp
            fonHolder.requestLayout()

            // после того как реальный размер fonHolder известен — расставляем слои
            fonHolder.doOnLayout { placeLayers() }
        }
    }

    /**
     * Кнопки собраны в три ряда:
     *  1) буфер пр. / буфер л.
     *  2) кр. буфер пр. / кр. буфер л.
     *  3) прожектор (если есть у локомотива)
     * Ряд создаётся только если для него есть хотя бы одна кнопка у этого локомотива.
     */
    private fun setupButtons() {
        val panel = findViewById<LinearLayout>(R.id.buttonsPanel)
        panel.removeAllViews()

        val byId = locomotive.buttons.associateBy { it.id }

        val row1 = listOfNotNull(byId["bufer_r"], byId["bufer_l"])
        val row2 = listOfNotNull(byId["red_bufer_r"], byId["red_bufer_l"])
        val row3 = listOfNotNull(byId["projector"])

        if (row1.isNotEmpty()) panel.addView(buildButtonRow(row1))
        if (row2.isNotEmpty()) panel.addView(buildButtonRow(row2))
        if (row3.isNotEmpty()) panel.addView(buildButtonRow(row3))
    }

    private fun buildButtonRow(buttons: List<ToggleButton>): LinearLayout {
        val density = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (8 * density).toInt() }
        }

        for ((index, button) in buttons.withIndex()) {
            val btnView = CheckedTextView(this).apply {
                text = getString(button.labelRes)
                gravity = Gravity.CENTER
                isFocusable = true
                isClickable = true
                setCheckMarkDrawable(0) // без встроенной галочки
                background = AppCompatResources.getDrawable(context, R.drawable.bg_toggle_button)
                setTextColor(getColorStateList(R.color.toggle_text_selector))
                textSize = 14f
                setPadding((10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt())
            }
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (index > 0) lp.marginStart = (8 * density).toInt()
            btnView.layoutParams = lp

            btnView.setOnClickListener {
                btnView.isChecked = !btnView.isChecked
                setLayerVisible(button.id, btnView.isChecked)
            }

            row.addView(btnView)
        }

        return row
    }

    /**
     * Создаёт ImageView для каждого слоя каждой кнопки (изначально невидимые)
     * и позиционирует их по проценту от размеров fonHolder.
     * Координаты округляются через roundToInt(), а не отбрасыванием дробной
     * части — иначе на некоторых экранах слои систематически съезжают на 1px.
     */
    private fun placeLayers() {
        val holderW = fonHolder.width
        val holderH = fonHolder.height
        if (holderW == 0 || holderH == 0) return

        for (button in locomotive.buttons) {
            val views = button.layers.map { layerAsset ->
                val iv = ImageView(this).apply {
                    setImageResource(layerAsset.drawableRes)
                    scaleType = ImageView.ScaleType.FIT_XY
                    alpha = 0f
                    visibility = View.INVISIBLE
                }

                val widthPx = (holderW * layerAsset.rect.wPct / 100f).roundToInt()

                // соотношение сторон исходного drawable, чтобы высота была верной
                val drawable = AppCompatResources.getDrawable(this, layerAsset.drawableRes)
                val ratio = if (drawable != null && drawable.intrinsicWidth > 0) {
                    drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()
                } else 1f
                val heightPx = (widthPx * ratio).roundToInt()

                val lp = FrameLayout.LayoutParams(widthPx, heightPx, Gravity.TOP or Gravity.START)
                lp.leftMargin = (holderW * layerAsset.rect.xPct / 100f).roundToInt()
                lp.topMargin = (holderH * layerAsset.rect.yPct / 100f).roundToInt()

                fonHolder.addView(iv, lp)
                iv
            }
            layerViewsByButton[button.id] = views
        }
    }

    private fun setLayerVisible(buttonId: String, visible: Boolean) {
        val views = layerViewsByButton[buttonId] ?: return
        for (v in views) {
            v.animate().cancel()
            if (visible) {
                v.visibility = View.VISIBLE
                v.animate().alpha(1f).setDuration(FADE_DURATION_MS).start()
            } else {
                v.animate().alpha(0f).setDuration(FADE_DURATION_MS)
                    .withEndAction { v.visibility = View.INVISIBLE }
                    .start()
            }
        }
    }
}
