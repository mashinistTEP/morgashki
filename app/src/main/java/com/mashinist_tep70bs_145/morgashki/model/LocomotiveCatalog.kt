package com.mashinist_tep70bs_145.morgashki.model

import com.mashinist_tep70bs_145.morgashki.R

/**
 * Единый каталог локомотивов игры.
 *
 * Координаты слоёв (LayerRect) заданы в процентах от размеров фона (fon.png)
 * и получены через HTML-калибратор — при добавлении нового локомотива или
 * пересчёте позиций достаточно поправить цифры здесь.
 */
object LocomotiveCatalog {

    val all: List<Locomotive> = listOf(

        // ---------------- БЧ ----------------

        Locomotive(
            code = "bkg1",
            displayName = "БКГ1-010",
            fonRes = R.drawable.bkg1_fon,
            fonAspectRatio = 864f / 1233f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg1_bufer_r, LayerRect(25.372316f, 60.230854f, 6.177857f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg1_red_bufer_r, LayerRect(16.658784f, 60.199498f, 6.162886f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg1_bufer_l, LayerRect(68.377287f, 60.151261f, 6.225794f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg1_red_bufer_l, LayerRect(77.071541f, 60.169871f, 6.238400f))
                    )
                ),
                // У БКГ1/БКГ2 два прожекторных фонаря включаются одним тумблером.
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg1_projector_l, LayerRect(49.204563f, 7.978029f, 11.535407f)),
                        LayerAsset(R.drawable.bkg1_projector_r, LayerRect(39.190523f, 7.968476f, 11.535407f))
                    )
                )
            )
        ),

        Locomotive(
            code = "chs4t",
            displayName = "ЧС4т-600",
            ownName = "АЛЕКСАНДР ЛАДУТЬКО",
            fonRes = R.drawable.chs4t_fon,
            fonAspectRatio = 912f / 1169f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_bufer_r, LayerRect(22.776236f, 62.558101f, 4.881735f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_red_bufer_r, LayerRect(16.561494f, 62.552847f, 4.872909f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_bufer_l, LayerRect(72.622663f, 62.605110f, 4.908724f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_red_bufer_l, LayerRect(78.893443f, 62.602112f, 4.893141f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_projector, LayerRect(47.049759f, 15.798217f, 6.339024f))
                    )
                )
            )
        ),

        Locomotive(
            code = "tep70",
            displayName = "ТЭП70-0212",
            fonRes = R.drawable.tep70_fon,
            fonAspectRatio = 864f / 1226f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70_bufer_r, LayerRect(31.423888f, 59.278028f, 6.432091f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70_red_bufer_r, LayerRect(23.227669f, 59.335914f, 6.358718f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70_bufer_l, LayerRect(62.108643f, 59.255331f, 6.419886f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70_red_bufer_l, LayerRect(70.373717f, 59.315410f, 6.352424f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70_projector, LayerRect(44.564123f, 41.944091f, 10.601877f))
                    )
                )
            )
        ),

        // ВЛ80с — только буферы+прожектор готовы, фона ещё нет => "на маршруте"
        Locomotive(
            code = "vl80s",
            displayName = "ВЛ80С",
            fonRes = null,
            group = RailwayGroup.BCH,
            onRoute = true
        ),

        // М62 — фона ещё нет => "на маршруте"
        Locomotive(
            code = "m62",
            displayName = "М62",
            fonRes = null,
            group = RailwayGroup.BCH,
            onRoute = true
        ),

        Locomotive(
            code = "dr1a",
            displayName = "ДР1А-269",
            fonRes = R.drawable.dr1a_fon,
            fonAspectRatio = 864f / 1226f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.dr1a_bufer_r, LayerRect(21.946196f, 57.844229f, 5.848371f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.dr1a_red_bufer_r, LayerRect(13.905296f, 57.263541f, 9.440906f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.dr1a_bufer_l, LayerRect(71.524805f, 57.774021f, 5.818592f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.dr1a_red_bufer_l, LayerRect(76.546412f, 57.327423f, 8.577746f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.dr1a_projector, LayerRect(45.119348f, 14.112053f, 9.426422f))
                    )
                )
            )
        ),

        // ЭР9Т — фон есть (fon_old), но по факту не откалиброван => "на маршруте" до готовности
        Locomotive(
            code = "er9t",
            displayName = "ЭР9Тм-801",
            ownName = "ЕВГЕНИЙ ВОЛОДЬКО",
            fonRes = R.drawable.er9t_fon,
            fonAspectRatio = 1212f / 1298f,
            group = RailwayGroup.BCH,
            onRoute = true
        ),

        // ---------------- РЖД ----------------

        Locomotive(
            code = "ep2k",
            displayName = "ЭП2К-220",
            fonRes = R.drawable.ep2k_fon,
            fonAspectRatio = 864f / 1226f,
            group = RailwayGroup.RZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_bufer_r, LayerRect(22.7762364975204f, 62.55810153377395f, 4.881735536318989f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_red_bufer_r, LayerRect(16.56149417705038f, 62.55284743400804f, 4.872909266889965f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_bufer_l, LayerRect(78.89344399147603f, 62.60211238739051f, 4.893141268424499f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_red_bufer_l, LayerRect(72.62266379190073f, 62.60511093645423f, 4.90872493446377f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_projector, LayerRect(47.04975910464576f, 15.798217921152201f, 6.339024824454506f))
                    )
                )
            )
        ),
        
        Locomotive(
            code = "chs4t_rzd",
            displayName = "ЧС4т-313",
            fonRes = R.drawable.chs4t_rzd_313_fon,
            fonAspectRatio = 1212f / 1298f,
            group = RailwayGroup.RZD,
            onRoute = true
        ),
        
        Locomotive(
            code = "es1p",
            displayName = "ЭС1П-013",
            fonRes = R.drawable.es1p_fon,
            fonAspectRatio = 652f / 745f,
            group = RailwayGroup.RZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.es1p_bufer_r, LayerRect(21.635091f, 59.257737f, 4.880624f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.es1p_bufer_l, LayerRect(73.856270f, 59.195549f, 4.924065f))
                    )
                )
            )
        )
    )

    fun byCode(code: String): Locomotive? = all.find { it.code == code }
}
