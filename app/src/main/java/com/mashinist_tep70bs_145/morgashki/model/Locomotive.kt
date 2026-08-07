package com.mashinist_tep70bs_145.morgashki.model

/**
 * Позиция и размер слоя на фоне локомотива, в процентах от размеров исходной
 * картинки фона (fon.png). Это позволяет одинаково масштабировать разметку
 * на любом экране: x/y — верхний левый угол слоя, w — ширина в процентах
 * от ширины фона (высота считается из соотношения сторон самой картинки слоя).
 */
data class LayerRect(
    val xPct: Float,
    val yPct: Float,
    val wPct: Float
)

/**
 * Один визуальный слой поверх фона (например: buffer_r, red_buffer_l, projector...).
 * drawableRes — ресурс картинки. rect — её положение/размер.
 */
data class LayerAsset(
    val drawableRes: Int,
    val rect: LayerRect
)

/**
 * Группа тумблеров, которые включаются одной кнопкой одновременно.
 * Нужно для БКГ1/БКГ2, где кнопка "прожектор" включает сразу 2 фонаря
 * (projector_l и projector_r).
 */
data class ToggleButton(
    val id: String,           // уникальный id кнопки внутри локомотива, напр. "projector"
    val labelRes: Int,        // строковый ресурс подписи кнопки
    val layers: List<LayerAsset> // один или несколько слоёв, включаемых этой кнопкой
)

data class Locomotive(
    val code: String,             // короткий код, напр. "bkg1"
    val displayName: String,      // "БКГ1-010"
    val ownName: String? = null,  // "АЛЕКСАНДР ЛАДУТЬКО" (собственное имя, если есть)
    val fonRes: Int? = null,      // ресурс картинки фона; null если фона ещё нет
    val fonAspectRatio: Float = 0.75f, // width/height картинки фона, для расчёта разметки без загрузки
    val buttons: List<ToggleButton> = emptyList(),
    val onRoute: Boolean = false, // "на маршруте" — недоступен для выбора
    val group: RailwayGroup
)

enum class RailwayGroup(val titleRes: Int) {
    BCH(com.mashinist_tep70bs_145.morgashki.R.string.group_bch),
    RZD(com.mashinist_tep70bs_145.morgashki.R.string.group_rzd)
}
