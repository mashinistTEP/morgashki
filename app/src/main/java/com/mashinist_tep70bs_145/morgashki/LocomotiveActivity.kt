package com.mashinist_tep70bs_145.morgashki

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import com.mashinist_tep70bs_145.morgashki.model.Locomotive
import com.mashinist_tep70bs_145.morgashki.model.LocomotiveCatalog
import com.mashinist_tep70bs_145.morgashki.model.ToggleButton
import com.mashinist_tep70bs_145.morgashki.ui.DisplayBoardView
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

    private var displayBoardView: DisplayBoardView? = null
    private var emuTypeface: Typeface? = null

    // включено ли табло тумблером "ЭМУ" — пока false, кнопка "табло" неактивна
    private var emuBoardEnabled: Boolean = false
    private var emuToggleView: CheckedTextView? = null

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

        if (locomotive.displayBoard != null) {
            emuTypeface = ResourcesCompat.getFont(this, R.font.emu_display)
        }

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
            fonHolder.doOnLayout {
                placeLayers()
                placeDisplayBoard()
            }
        }
    }

    /**
     * Кнопки собраны в ряды:
     *  1) буфер пр. / буфер л.
     *  2) кр. буфер пр. / кр. буфер л.
     *  3) прожектор (если есть у локомотива)
     *  4) ЭМУ (вкл/выкл табло) + табло (открыть ввод текста) — если есть у локомотива,
     *     вдвое ниже остальных кнопок
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

        if (locomotive.displayBoard != null) {
            panel.addView(buildDisplayBoardControlsRow())
        }
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
     * Ряд из двух маленьких кнопок: "ЭМУ" (тумблер вкл/выкл табло) слева
     * и "табло" (открывает диалог ввода текста) справа. Высота вдвое меньше
     * обычных кнопок буферов/прожектора за счёт уменьшенного padding и
     * размера текста. Кнопка "табло" работает только если "ЭМУ" включено.
     */
    private fun buildDisplayBoardControlsRow(): LinearLayout {
        val density = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (8 * density).toInt() }
        }

        fun smallButtonPadding(v: View) {
            v.setPadding((10 * density).toInt(), (5 * density).toInt(), (10 * density).toInt(), (5 * density).toInt())
        }

        val emuBtn = CheckedTextView(this).apply {
            text = getString(R.string.btn_emu_power)
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            setCheckMarkDrawable(0)
            background = AppCompatResources.getDrawable(context, R.drawable.bg_toggle_button)
            setTextColor(getColorStateList(R.color.toggle_text_selector))
            textSize = 11f
            smallButtonPadding(this)
        }
        val emuLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        emuBtn.layoutParams = emuLp
        emuBtn.setOnClickListener {
            emuBoardEnabled = !emuBoardEnabled
            emuBtn.isChecked = emuBoardEnabled
            if (!emuBoardEnabled) {
                // при выключении ЭМУ табло гаснет
                displayBoardView?.clearAnimated()
            }
        }
        emuToggleView = emuBtn

        val boardBtn = TextView(this).apply {
            text = getString(R.string.btn_display_board)
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            background = AppCompatResources.getDrawable(context, R.drawable.bg_toggle_button)
            setTextColor(getColorStateList(R.color.toggle_text_selector))
            textSize = 11f
            smallButtonPadding(this)
        }
        val boardLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        boardLp.marginStart = (8 * density).toInt()
        boardBtn.layoutParams = boardLp
        boardBtn.setOnClickListener {
            if (emuBoardEnabled) {
                showDisplayBoardDialog()
            }
        }

        row.addView(emuBtn)
        row.addView(boardBtn)
        return row
    }

    private fun showDisplayBoardDialog() {
        val board = locomotive.displayBoard ?: return
        val density = resources.displayMetrics.density

        val input = EditText(this).apply {
            hint = getString(R.string.display_board_hint)
            filters = arrayOf(InputFilter.LengthFilter(board.maxChars))
            setSingleLine(true)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * density).toInt(), (12 * density).toInt(), (20 * density).toInt(), 0)
        }

        // кнопки готовых маршрутов сверху (специфичны для конкретного МВПС,
        // задаются в locomotive.displayBoard.presetRoutes, не глобально)
        if (board.presetRoutes.isNotEmpty()) {
            // Заготовки идут вертикально, по две кнопки в каждом ряду.
            // Так длинные названия маршрутов не сжимаются в один длинный горизонтальный ряд.
            val rows = board.presetRoutes.chunked(2)

            for ((rowIndex, rowRoutes) in rows.withIndex()) {
                val presetsRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                for ((columnIndex, route) in rowRoutes.withIndex()) {
                    val presetBtn = TextView(this).apply {
                        text = route
                        gravity = Gravity.CENTER
                        isFocusable = true
                        isClickable = true
                        background = AppCompatResources.getDrawable(context, R.drawable.bg_toggle_button)
                        setTextColor(getColorStateList(R.color.toggle_text_selector))
                        textSize = 13f
                        setPadding(
                            (8 * density).toInt(),
                            (10 * density).toInt(),
                            (8 * density).toInt(),
                            (10 * density).toInt()
                        )
                        minHeight = (52 * density).toInt()
                    }

                    val lp = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                    if (columnIndex > 0) {
                        lp.marginStart = (8 * density).toInt()
                    }

                    // Если в последнем ряду осталась одна заготовка,
                    // оставляем ей половину ширины, как у остальных кнопок.
                    presetBtn.layoutParams = lp

                    presetBtn.setOnClickListener {
                        input.setText(route)
                        input.setSelection(input.text.length)
                    }

                    presetsRow.addView(presetBtn)
                }

                container.addView(presetsRow)

                if (rowIndex < rows.lastIndex) {
                    val rowSpacer = View(this)
                    rowSpacer.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (8 * density).toInt()
                    )
                    container.addView(rowSpacer)
                }
            }

            val spacer = View(this)
            spacer.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (12 * density).toInt()
            )
            container.addView(spacer)
        }

        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle(R.string.display_board_dialog_title)
            .setView(container)
            .setPositiveButton(R.string.display_board_apply) { _, _ ->
                applyDisplayBoardText(input.text?.toString().orEmpty(), board.maxTextWidthPct)
            }
            .setNegativeButton(R.string.display_board_cancel, null)
            .show()
    }

    /**
     * Проверяет, помещается ли текст в реальную ширину окна табло (в px
     * текущего экрана) с учётом настоящего шрифта табло, и только если
     * помещается — применяет его с анимацией смены. Если текст шире окна,
     * показывает Toast и НЕ меняет текущий текст на табло.
     */
    private fun applyDisplayBoardText(text: String, maxWidthPct: Float) {
        val holderW = fonHolder.width
        val board = displayBoardView ?: return
        if (holderW == 0) return
        val maxWidthPx = holderW * maxWidthPct / 100f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = emuTypeface
            textSize = board.textSizePx
        }
        val measuredWidth = paint.measureText(text)

        if (measuredWidth > maxWidthPx) {
            Toast.makeText(this, R.string.display_board_too_long, Toast.LENGTH_SHORT).show()
            return
        }

        board.setTextAnimated(text)
    }

    /**
     * Создаёт DisplayBoardView для табло (изначально погашено — пустой текст)
     * и позиционирует его по проценту от размеров fonHolder — аналогично
     * обычным слоям буферов.
     */
    private fun placeDisplayBoard() {
        val board = locomotive.displayBoard ?: return
        val holderW = fonHolder.width
        val holderH = fonHolder.height
        if (holderW == 0 || holderH == 0) return

        val widthPx = (holderW * board.rect.wPct / 100f).roundToInt()
        // реальное соотношение высоты к ширине окна табло (откалибровано по фото)
        val heightPx = (widthPx * 0.221f).roundToInt()

        val boardView = DisplayBoardView(this).apply {
            typeface = emuTypeface
            textColor = resources.getColor(R.color.display_board_text, theme)
            textSizePx = heightPx * 0.48f
            setPadding((widthPx * 0.06f).roundToInt(), 0, 0, 0)
            setTextInstant("") // табло погашено при входе на экран
        }

        val lp = FrameLayout.LayoutParams(widthPx, heightPx, Gravity.TOP or Gravity.START)
        lp.leftMargin = (holderW * board.rect.xPct / 100f).roundToInt()
        lp.topMargin = (holderH * board.rect.yPct / 100f).roundToInt()

        fonHolder.addView(boardView, lp)
        displayBoardView = boardView
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
