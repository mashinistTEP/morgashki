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

/**
 * Табло с произвольным текстом (например маршрутный указатель на ЭМУ),
 * поверх которого можно вписать свою надпись. rect — положение и размер
 * окна табло на фоне (как у обычных слоёв). maxTextWidthPct — максимальная
 * ширина текста в процентах ширины фона: если введённый текст в выбранном
 * шрифте оказывается шире этого предела, ввод не применяется, чтобы текст
 * не вылезал за рамки табло. presetRoutes — готовые маршруты, показываемые
 * кнопками в диалоге ввода (специфичны для конкретного МВПС, не глобальные).
 */
data class DisplayBoard(
    val rect: LayerRect,
    val maxTextWidthPct: Float,
    val maxChars: Int = 12,
    val presetRoutes: List<String> = emptyList()
)

data class Locomotive(
    val code: String,             // короткий код, напр. "bkg1"
    val displayName: String,      // "БКГ1-010"
    val ownName: String? = null,  //собственное имя, если есть
    val fonRes: Int? = null,      // ресурс картинки фона; null если фона ещё нет
    val fonAspectRatio: Float = 0.75f, // width/height картинки фона, для расчёта разметки без загрузки
    val buttons: List<ToggleButton> = emptyList(),
    val onRoute: Boolean = false, // "на маршруте" — недоступен для выбора
    val group: RailwayGroup,
    val line: RailwayLine? = null, // конкретная дорога внутри компании (МЖД, ОКТ.ЖД...); null = общий состав группы
    val displayBoard: DisplayBoard? = null // табло с вводом своего текста; null = у локомотива нет табло
)

enum class RailwayGroup(val titleRes: Int) {
    BCH(com.mashinist_tep70bs_145.morgashki.R.string.group_bch),
    RZD(com.mashinist_tep70bs_145.morgashki.R.string.group_rzd)
}

/**
 * Конкретная дорога (подгруппа) внутри компании-перевозчика, например
 * МЖД или ОКТ.ЖД внутри РЖД. parentGroup — к какой компании относится,
 * используется чтобы показывать кнопку дороги только в нужной группе.
 */
enum class RailwayLine(val titleRes: Int, val parentGroup: RailwayGroup) {
    KLD_ZD(com.mashinist_tep70bs_145.morgashki.R.string.line_kld_zd, RailwayGroup.RZD),
    MZD(com.mashinist_tep70bs_145.morgashki.R.string.line_mzd, RailwayGroup.RZD),
    // Заполняется скриптом add_railway_line.py, либо вручную по образцу:
    // OCT(com.mashinist_tep70bs_145.morgashki.R.string.line_oct, RailwayGroup.RZD),
}
