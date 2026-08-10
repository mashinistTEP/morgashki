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

        Locomotive(
            code = "tep70bs_145",
            displayName = "ТЭП70БС-145",
            fonRes = R.drawable.tep70bs_145_fon,
            fonAspectRatio = 675f / 832f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70bs_145_bufer_r, LayerRect(31.950350f, 63.861291f, 7.026614f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70bs_145_bufer_l, LayerRect(63.512284f, 64.157730f, 6.515566f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70bs_145_red_bufer_r, LayerRect(25.736632f, 64.884256f, 5.269344f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70bs_145_red_bufer_l, LayerRect(71.181709f, 64.735245f, 5.421946f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.tep70bs_145_projector, LayerRect(46.228378f, 16.604279f, 9.341362f))
                    )
                )
            )
        ),

        Locomotive(
            code = "er9tm",
            displayName = "ЭР9Тм-801",
            ownName = "ЕВГЕНИЙ ВОЛОДЬКО",
            fonRes = R.drawable.er9tm_fon,
            fonAspectRatio = 628f / 816f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.er9tm_bufer_r, LayerRect(22.642163f, 62.451874f, 6.623946f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.er9tm_bufer_l, LayerRect(68.814237f, 62.239207f, 7.060562f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.er9tm_red_bufer_r, LayerRect(17.094992f, 62.347309f, 5.996259f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.er9tm_red_bufer_l, LayerRect(75.039792f, 62.308379f, 5.646559f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.er9tm_projector, LayerRect(43.995338f, 22.571878f, 10.285409f))
                    )
                )
            )
        ),

        Locomotive(
            code = "bkg2",
            displayName = "БКГ2-002",
            fonRes = R.drawable.bkg2_fon,
            fonAspectRatio = 864f / 1233f,
            group = RailwayGroup.BCH,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg2_bufer_r, LayerRect(25.372316f, 60.230854f, 6.177857f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg2_bufer_l, LayerRect(68.377287f, 60.151262f, 6.225794f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg2_red_bufer_r, LayerRect(16.658784f, 60.199498f, 6.162886f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg2_red_bufer_l, LayerRect(77.071541f, 60.169871f, 6.238400f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.bkg2_projector_l, LayerRect(49.204563f, 7.978029f, 11.535407f)),
                        LayerAsset(R.drawable.bkg2_projector_r, LayerRect(39.190524f, 7.968477f, 11.535407f))
                    )
                )
            )
        ),

        // ---------------- РЖД ----------------

        Locomotive(
            code = "ep2k",
            displayName = "ЭП2К-220",
            fonRes = R.drawable.ep2k_fon,
            fonAspectRatio = 896f / 1167f,
            group = RailwayGroup.RZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_bufer_r, LayerRect(33.454641015245386f, 63.935370896313444f, 7.686396714261276f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_red_bufer_r, LayerRect(26.20693364804883f, 64.4973057412749f, 6.435249359318719f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_bufer_l, LayerRect(65.36832302868848f, 64.20376438813825f, 7.738650300660359f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_red_bufer_l, LayerRect(74.08982528492652f, 64.91480069689955f, 6.160153289361195f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.ep2k_projector, LayerRect(47.35706508970985f, 15.819854692183275f, 11.687893747258867f))
                    )
                )
            )
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
        ),

        Locomotive(
            code = "chs4t_313_rzd",
            displayName = "ЧС4т-313",
            fonRes = R.drawable.chs4t_313_rzd_fon,
            fonAspectRatio = 1171f / 1343f,
            group = RailwayGroup.RZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_313_rzd_bufer_r, LayerRect(22.935969f, 64.016315f, 4.029003f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_313_rzd_bufer_l, LayerRect(73.214967f, 63.852833f, 4.023163f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_313_rzd_red_bufer_r, LayerRect(19.029665f, 64.080951f, 3.999926f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_313_rzd_red_bufer_l, LayerRect(76.959749f, 63.713409f, 3.995651f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.chs4t_313_rzd_projector, LayerRect(46.375658f, 13.132028f, 7.479308f))
                    )
                )
            )
        ),

        Locomotive(
            code = "ep1m",
            displayName = "ЭП1М-397",
            fonRes = R.drawable.ep1m_fon,
            fonAspectRatio = 848f / 1231f,
            group = RailwayGroup.RZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep1m_bufer_r, LayerRect(17.918476f, 54.573470f, 7.353889f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep1m_bufer_l, LayerRect(75.218332f, 54.699889f, 7.055471f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.ep1m_red_bufer_r, LayerRect(17.970608f, 49.705096f, 5.566744f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.ep1m_red_bufer_l, LayerRect(76.740230f, 49.676241f, 5.547720f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.ep1m_projector, LayerRect(43.120530f, 9.213673f, 13.929675f))
                    )
                )
            )
        ),

        Locomotive(
            code = "er2k_428",
            displayName = "ЭР2К-428",
            ownName = "Голубая Стрела",
            fonRes = R.drawable.er2k_428_fon,
            fonAspectRatio = 612f / 766f,
            group = RailwayGroup.RZD,
            line = RailwayLine.KLD_ZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.er2k_428_bufer_r, LayerRect(26.771436f, 60.597071f, 4.840231f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.er2k_428_bufer_l, LayerRect(67.262418f, 60.569008f, 5.064358f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.er2k_428_red_bufer_r, LayerRect(20.695465f, 60.554429f, 5.041824f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.er2k_428_red_bufer_l, LayerRect(73.143267f, 60.474763f, 5.101431f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.er2k_428_projector, LayerRect(45.146737f, 13.968323f, 9.016051f))
                    )
                )
            ),
            // Координаты откалиброваны точно по er2k_428_fon.png (612x766):
            // окно табло x=200..390, y=170..212 px.
            displayBoard = DisplayBoard(
                rect = LayerRect(xPct = 32.6797f, yPct = 22.1932f, wPct = 31.0458f),
                maxTextWidthPct = 29f,
                maxChars = 22,
                presetRoutes = listOf("Светлогорск", "Калининград-Юж.", "Нет Посадки", "В Депо", "Испытания", "Обкатка")
            )
        ),

        Locomotive(
            code = "tgm4a_1687",
            displayName = "ТГМ4а-1687",
            fonRes = R.drawable.tgm4a_1687_fon,
            fonAspectRatio = 672f / 768f,
            group = RailwayGroup.RZD,
            line = RailwayLine.MZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4a_1687_bufer_r, LayerRect(23.757604f, 64.382311f, 7.656970f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4a_1687_bufer_l, LayerRect(71.138538f, 64.319048f, 7.767400f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4a_1687_red_bufer_r, LayerRect(29.598652f, 64.007735f, 7.000071f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4a_1687_red_bufer_l, LayerRect(66.224851f, 64.437840f, 6.829667f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4a_1687_projector, LayerRect(46.845323f, 12.410522f, 8.942812f))
                    )
                )
            )
        ),

        Locomotive(
            code = "tgm4b_0824",
            displayName = "ТГМ4б-0824",
            fonRes = R.drawable.tgm4b_0824_fon,
            fonAspectRatio = 665f / 768f,
            group = RailwayGroup.RZD,
            line = RailwayLine.MZD,
            buttons = listOf(
                ToggleButton(
                    id = "bufer_r",
                    labelRes = R.string.btn_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4b_0824_bufer_r, LayerRect(23.826397f, 69.785288f, 6.217042f))
                    )
                ),
                ToggleButton(
                    id = "bufer_l",
                    labelRes = R.string.btn_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4b_0824_bufer_l, LayerRect(71.594708f, 69.912955f, 5.999337f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_r",
                    labelRes = R.string.btn_red_bufer_r,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4b_0824_red_bufer_r, LayerRect(24.136787f, 64.947555f, 5.333777f))
                    )
                ),
                ToggleButton(
                    id = "red_bufer_l",
                    labelRes = R.string.btn_red_bufer_l,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4b_0824_red_bufer_l, LayerRect(71.440210f, 64.785828f, 5.902509f))
                    )
                ),
                ToggleButton(
                    id = "projector",
                    labelRes = R.string.btn_projector,
                    layers = listOf(
                        LayerAsset(R.drawable.tgm4b_0824_projector, LayerRect(45.607451f, 14.025497f, 10.000000f))
                    )
                )
            )
        )
    )

    fun byCode(code: String): Locomotive? = all.find { it.code == code }
}
