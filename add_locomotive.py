#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
add_locomotive.py — интерактивное добавление нового локомотива/МВПС в проект "Моргашки".

Что делает:
  1. Спрашивает название, короткий код, именной ли локомотив (и если да — имя),
     группу (БЧ/РЖД), блок координат из калибратора (JSON), наличие раздельного
     прожектора (л./пр.) или одного общего.
  2. Сам вычисляет fonAspectRatio из реального PNG-файла фона (через встроенный
     разбор заголовка PNG — без внешних зависимостей типа Pillow).
  3. Проверяет, что все нужные PNG-файлы (fon, bufer_l/r, red_bufer_l/r,
     projector[_l/_r]) реально лежат в drawable-nodpi — не даёт продолжить,
     если чего-то не хватает.
  4. Генерирует корректный блок Locomotive(...) и аккуратно вставляет его
     в LocomotiveCatalog.kt в конец нужной группы (БЧ или РЖД).
  5. Проверяет баланс скобок файла после вставки — если он нарушен, изменения
     не сохраняются, чтобы не сломать сборку.
  6. Предлагает сразу закоммитить и запушить весь проект в GitHub.

Запуск (из корня проекта android_project_v2):
    python3 add_locomotive.py
"""

import json
import os
import re
import subprocess
import sys

# ---------- Настройки путей (относительно корня проекта) ----------

CATALOG_PATH = "app/src/main/java/com/mashinist_tep70bs_145/morgashki/model/LocomotiveCatalog.kt"
DRAWABLE_DIR = "app/src/main/res/drawable-nodpi"


# ---------- Утилиты ввода ----------

def ask(prompt, default=None):
    suffix = f" [{default}]" if default is not None else ""
    while True:
        answer = input(f"{prompt}{suffix}: ").strip()
        if answer:
            return answer
        if default is not None:
            return default
        print("  Это поле обязательно, попробуй ещё раз.")


def ask_yes_no(prompt, default_yes=True):
    hint = "Д/н" if default_yes else "д/Н"
    while True:
        answer = input(f"{prompt} ({hint}): ").strip().lower()
        if not answer:
            return default_yes
        if answer in ("д", "да", "y", "yes"):
            return True
        if answer in ("н", "нет", "n", "no"):
            return False
        print("  Ответь 'да' или 'нет'.")


def ask_multiline_json(prompt):
    print(f"{prompt}")
    print("  Вставь блок координат из калибратора (JSON вида \"код\": {{ ... }}).")
    print("  Когда закончишь — введи пустую строку и нажми Enter.")
    lines = []
    while True:
        line = input()
        if line.strip() == "" and lines:
            break
        lines.append(line)
    raw = "\n".join(lines)
    return raw


# ---------- Разбор PNG без внешних библиотек ----------

def read_png_size(path):
    """Читает ширину/высоту PNG прямо из заголовка файла (IHDR-чанк)."""
    with open(path, "rb") as f:
        header = f.read(33)
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"Файл {path} не похож на корректный PNG")
    # IHDR: длина(4) + 'IHDR'(4) + width(4) + height(4) ...
    width = int.from_bytes(header[16:20], "big")
    height = int.from_bytes(header[20:24], "big")
    return width, height


# ---------- Работа с JSON калибратора ----------

def parse_calibrator_block(raw_text, code):
    """
    Пользователь может вставить либо чистый JSON-объект слоёв, либо
    целиком фрагмент вида "код": { ... } — обрабатываем оба случая.
    """
    text = raw_text.strip()
    # если пользователь скопировал вместе с "код": { ... }, обернём в { } и распарсим целиком
    if not text.startswith("{"):
        text = "{" + text
    if not text.rstrip().endswith("}"):
        text = text + "}"

    # Пытаемся распарсить как  {"код": {...}}  или как  {...} (сразу слои)
    try:
        parsed = json.loads(text)
    except json.JSONDecodeError as e:
        raise ValueError(f"Не удалось разобрать JSON: {e}\n\nПроверь, что скопировал блок полностью и корректно.")

    if code in parsed:
        return parsed[code]
    # если ключ верхнего уровня не совпал с code (например, вставили только словарь слоёв)
    if len(parsed) == 1:
        return next(iter(parsed.values()))
    # либо это уже сами слои (bufer_r, bufer_l, ...)
    if "bufer_r" in parsed or "bufer_l" in parsed:
        return parsed
    raise ValueError(
        f"Не нашёл в JSON ключ '{code}'. Проверь, что код locomotives совпадает "
        f"с тем, что был в калибраторе."
    )


# ---------- Генерация кода ----------

def fmt_float(x):
    return f"{float(x):.6f}f"


def build_layer_asset(drawable_name, layer):
    return (
        f"LayerAsset(R.drawable.{drawable_name}, "
        f"LayerRect({fmt_float(layer['xPct'])}, {fmt_float(layer['yPct'])}, {fmt_float(layer['wPct'])}))"
    )


def build_toggle_button(button_id, label_res, drawable_layers, indent="                "):
    """
    drawable_layers — список (drawable_name, layer_dict), обычно один элемент,
    но для прожектора БКГ-подобных локомотивов может быть два (_l и _r).
    """
    layer_lines = ",\n".join(
        f"{indent}        {build_layer_asset(name, layer)}" for name, layer in drawable_layers
    )
    return (
        f"{indent}ToggleButton(\n"
        f"{indent}    id = \"{button_id}\",\n"
        f"{indent}    labelRes = R.string.{label_res},\n"
        f"{indent}    layers = listOf(\n"
        f"{layer_lines}\n"
        f"{indent}    )\n"
        f"{indent})"
    )


def build_locomotive_block(code, display_name, own_name, group_const,
                            has_fon, fon_aspect_ratio,
                            buttons_data, on_route=False):
    """
    buttons_data: список кортежей (button_id, label_res, [(drawable_name, layer), ...])
    в нужном порядке вывода.
    """
    indent = "        "
    lines = [f"{indent}Locomotive("]
    lines.append(f"{indent}    code = \"{code}\",")
    lines.append(f"{indent}    displayName = \"{display_name}\",")
    if own_name:
        lines.append(f"{indent}    ownName = \"{own_name}\",")
    if has_fon:
        lines.append(f"{indent}    fonRes = R.drawable.{code}_fon,")
        lines.append(f"{indent}    fonAspectRatio = {fon_aspect_ratio[0]}f / {fon_aspect_ratio[1]}f,")
    else:
        lines.append(f"{indent}    fonRes = null,")
    lines.append(f"{indent}    group = RailwayGroup.{group_const},")
    if on_route:
        lines.append(f"{indent}    onRoute = true,")

    if buttons_data and not on_route:
        button_indent = indent + "        "
        button_blocks = [
            build_toggle_button(bid, label, layers, indent=button_indent)
            for bid, label, layers in buttons_data
        ]
        lines.append(f"{indent}    buttons = listOf(")
        lines.append(",\n".join(f"{b}" for b in button_blocks))
        lines.append(f"{indent}    )")
    else:
        # убираем висячую запятую у group/onRoute, если buttons не добавляем
        lines[-1] = lines[-1].rstrip(",")

    lines.append(f"{indent})")
    return "\n".join(lines)


# ---------- Проверка файлов на диске ----------

def check_required_files(code, has_red_buffers, has_projector, projector_separate, has_fon):
    required = []
    if has_fon:
        required.append(f"{code}_fon.png")
    required.append(f"{code}_bufer_l.png")
    required.append(f"{code}_bufer_r.png")
    if has_red_buffers:
        required.append(f"{code}_red_bufer_l.png")
        required.append(f"{code}_red_bufer_r.png")
    if has_projector:
        if projector_separate:
            required.append(f"{code}_projector_l.png")
            required.append(f"{code}_projector_r.png")
        else:
            required.append(f"{code}_projector.png")

    missing = []
    for fname in required:
        full_path = os.path.join(DRAWABLE_DIR, fname)
        if not os.path.isfile(full_path):
            missing.append(fname)
    return required, missing


# ---------- Вставка в LocomotiveCatalog.kt ----------

def find_top_level_locomotive_blocks(list_body):
    """
    Находит все top-level блоки `Locomotive( ... )` внутри тела списка,
    возвращая список (start, end) индексов — end указывает на индекс СРАЗУ
    ПОСЛЕ закрывающей скобки блока. Работает через точный подсчёт баланса
    скобок начиная от каждого найденного "Locomotive(".
    """
    blocks = []
    search_pos = 0
    while True:
        idx = list_body.find("Locomotive(", search_pos)
        if idx == -1:
            break
        open_paren_idx = idx + len("Locomotive") # индекс символа '('
        depth = 0
        i = open_paren_idx
        while i < len(list_body):
            if list_body[i] == "(":
                depth += 1
            elif list_body[i] == ")":
                depth -= 1
                if depth == 0:
                    i += 1
                    break
            i += 1
        else:
            raise ValueError("Не удалось найти конец блока Locomotive(...) — баланс скобок не сошёлся.")
        blocks.append((idx, i))
        search_pos = i
    return blocks


def insert_locomotive_block(catalog_text, group_const, new_block):
    """
    Вставляет new_block в конец группы group_const (BCH или RZD).
    Ищет по маркеру `group = RailwayGroup.<X>,` внутри каждого блока Locomotive(...).
    """
    # Находим начало списка `val all: List<Locomotive> = listOf(`
    list_start_marker = "val all: List<Locomotive> = listOf("
    list_start_idx = catalog_text.find(list_start_marker)
    if list_start_idx == -1:
        raise ValueError("Не нашёл `val all: List<Locomotive> = listOf(` в файле каталога.")

    body_start = list_start_idx + len(list_start_marker)

    # Находим соответствующую закрывающую скобку списка listOf(...) по балансу
    depth = 1
    i = body_start
    while depth > 0:
        if catalog_text[i] == "(":
            depth += 1
        elif catalog_text[i] == ")":
            depth -= 1
        i += 1
        if i >= len(catalog_text):
            raise ValueError("Не удалось найти конец списка locomotives (баланс скобок не сошёлся).")
    list_end_idx = i - 1  # индекс закрывающей ')' самого listOf(

    list_body = catalog_text[body_start:list_end_idx]

    blocks = find_top_level_locomotive_blocks(list_body)

    if not blocks:
        raise ValueError("Не нашёл ни одного блока Locomotive(...) в списке — проверь структуру файла.")

    group_marker = f"group = RailwayGroup.{group_const}"

    # находим последний блок, относящийся к нужной группе
    last_index_of_group = None
    for idx, (s, e) in enumerate(blocks):
        block_text = list_body[s:e]
        if group_marker in block_text:
            last_index_of_group = idx

    if last_index_of_group is None:
        # такой группы в файле ещё нет — добавляем новый блок в самый конец списка
        last_block_end = blocks[-1][1] if blocks else 0
        prefix = list_body[:last_block_end]
        suffix = list_body[last_block_end:]
        new_prefix = prefix.rstrip()
        if not new_prefix.endswith(","):
            new_prefix += ","
        new_body = new_prefix + "\n\n" + new_block + "\n" + suffix
    else:
        s, e = blocks[last_index_of_group]
        prefix = list_body[:e]
        suffix = list_body[e:]
        stripped_suffix = suffix.lstrip()
        if stripped_suffix.startswith(","):
            # запятая после блока уже есть в файле — переносим её в конец prefix,
            # чтобы порядок остался "...er9t),\n\nLocomotive(new)...", а не наоборот
            comma_pos_in_suffix = suffix.find(",")
            prefix = prefix + suffix[:comma_pos_in_suffix + 1]
            suffix = suffix[comma_pos_in_suffix + 1:]
        else:
            prefix = prefix + ","
        new_body = prefix + "\n\n" + new_block + suffix

    new_catalog_text = (
        catalog_text[:body_start] + new_body + catalog_text[list_end_idx:]
    )
    return new_catalog_text


def check_balance(text):
    depth_p = 0
    depth_b = 0
    for ch in text:
        if ch == "(":
            depth_p += 1
        elif ch == ")":
            depth_p -= 1
            if depth_p < 0:
                return False
        elif ch == "{":
            depth_b += 1
        elif ch == "}":
            depth_b -= 1
            if depth_b < 0:
                return False
    return depth_p == 0 and depth_b == 0


# ---------- Основной сценарий ----------

def main():
    print("=" * 60)
    print(" Добавление нового локомотива в проект «Моргашки»")
    print("=" * 60)
    print()

    if not os.path.isfile(CATALOG_PATH):
        print(f"ОШИБКА: не нашёл {CATALOG_PATH}")
        print("Запусти скрипт из корня проекта (там, где лежит build.gradle.kts).")
        sys.exit(1)

    display_name = ask("Название локомотива (например «ЭП2К-220»)")
    code = ask("Короткий код (латиницей, для имён файлов, например «ep2k»)").lower()
    code = re.sub(r"[^a-z0-9_]", "", code)
    if not code:
        print("ОШИБКА: код получился пустым после очистки — используй только латиницу/цифры/подчёркивания.")
        sys.exit(1)

    is_named = ask_yes_no("Локомотив именной?", default_yes=False)
    own_name = None
    if is_named:
        own_name = ask("Введи собственное имя (например «ЕВГЕНИЙ ВОЛОДЬКО»)").upper()

    print()
    group_choice = ask("Группа — БЧ или РЖД? (введи bch/rzd)", default="bch").lower()
    group_const = "RZD" if group_choice in ("rzd", "ржд") else "BCH"

    print()
    on_route = ask_yes_no("Поставить локомотив «на маршруте» (недоступен для выбора)?", default_yes=False)

    has_fon = True
    fon_aspect_ratio = None
    has_red_buffers = True
    has_projector = False
    projector_separate = False

    if not on_route:
        has_fon = ask_yes_no("Фон (картинка локомотива) уже готов?", default_yes=True)
        has_red_buffers = ask_yes_no("Есть красные буферы (л./пр.)?", default_yes=True)
        has_projector = ask_yes_no("Есть прожектор?", default_yes=True)
        if has_projector:
            projector_separate = ask_yes_no(
                "Прожектор двойной (два отдельных фонаря, включаются одной кнопкой, как у БКГ1)?",
                default_yes=False
            )

        print()
        required, missing = check_required_files(code, has_red_buffers, has_projector, projector_separate, has_fon)
        print("Ожидаемые файлы в drawable-nodpi:")
        for fname in required:
            mark = "OK " if fname not in missing else "НЕТ"
            print(f"  [{mark}] {fname}")

        if missing:
            print()
            print("Не хватает файлов выше. Скопируй их в", DRAWABLE_DIR, "и запусти скрипт заново.")
            sys.exit(1)

        if has_fon:
            fon_path = os.path.join(DRAWABLE_DIR, f"{code}_fon.png")
            try:
                w, h = read_png_size(fon_path)
            except Exception as e:
                print(f"ОШИБКА при чтении {fon_path}: {e}")
                sys.exit(1)
            fon_aspect_ratio = (w, h)
            print(f"\nРазмер фона: {w} x {h} px  →  fonAspectRatio = {w}f / {h}f")

        print()
        raw_json = ask_multiline_json("Теперь вставь блок координат из калибратора:")
        try:
            layers_data = parse_calibrator_block(raw_json, code)
        except ValueError as e:
            print(f"\nОШИБКА: {e}")
            sys.exit(1)

    # ---------- Собираем buttons_data ----------
    buttons_data = []
    if not on_route:
        def layer_entry(key, drawable_suffix):
            if key not in layers_data:
                print(f"ОШИБКА: в JSON нет ключа '{key}', а он ожидался.")
                sys.exit(1)
            return (f"{code}_{drawable_suffix}", layers_data[key])

        buttons_data.append(("bufer_r", "btn_bufer_r", [layer_entry("bufer_r", "bufer_r")]))
        buttons_data.append(("bufer_l", "btn_bufer_l", [layer_entry("bufer_l", "bufer_l")]))

        if has_red_buffers:
            buttons_data.append(("red_bufer_r", "btn_red_bufer_r", [layer_entry("red_bufer_r", "red_bufer_r")]))
            buttons_data.append(("red_bufer_l", "btn_red_bufer_l", [layer_entry("red_bufer_l", "red_bufer_l")]))

        if has_projector:
            if projector_separate:
                proj_layers = [
                    layer_entry("projector_l", "projector_l"),
                    layer_entry("projector_r", "projector_r"),
                ]
            else:
                proj_layers = [layer_entry("projector", "projector")]
            buttons_data.append(("projector", "btn_projector", proj_layers))

    new_block = build_locomotive_block(
        code=code,
        display_name=display_name,
        own_name=own_name,
        group_const=group_const,
        has_fon=has_fon if not on_route else False,
        fon_aspect_ratio=fon_aspect_ratio,
        buttons_data=buttons_data,
        on_route=on_route,
    )

    print()
    print("-" * 60)
    print("Сгенерированный блок:")
    print("-" * 60)
    print(new_block)
    print("-" * 60)
    print()

    if not ask_yes_no("Вставить этот блок в LocomotiveCatalog.kt?", default_yes=True):
        print("Отменено, ничего не изменено.")
        sys.exit(0)

    with open(CATALOG_PATH, "r", encoding="utf-8") as f:
        original_text = f.read()

    try:
        new_text = insert_locomotive_block(original_text, group_const, new_block)
    except ValueError as e:
        print(f"ОШИБКА при вставке: {e}")
        sys.exit(1)

    if not check_balance(new_text):
        print("ОШИБКА: после вставки баланс скобок нарушен — файл НЕ изменён.")
        print("Пришли этот вывод разработчику для разбора.")
        sys.exit(1)

    with open(CATALOG_PATH, "w", encoding="utf-8") as f:
        f.write(new_text)

    print(f"Готово! Локомотив «{display_name}» добавлен в {CATALOG_PATH}")
    print()

    # ---------- Git ----------
    if ask_yes_no("Закоммитить и запушить проект в GitHub сейчас?", default_yes=True):
        commit_message = ask("Сообщение коммита", default=f"Добавлен локомотив {display_name}")
        run_git(["git", "add", "."])
        run_git(["git", "commit", "-m", commit_message])
        run_git(["git", "push", "origin", "main"])
        print()
        print("Готово! Проверь вкладку Actions на GitHub — сборка должна запуститься автоматически.")
    else:
        print("Хорошо, изменения остались только локально. Когда будешь готов — запушь вручную.")


def run_git(args):
    print(f"\n$ {' '.join(args)}")
    result = subprocess.run(args)
    if result.returncode != 0:
        print(f"\nКоманда завершилась с ошибкой (код {result.returncode}).")
        print("Посмотри вывод выше — возможно, потребуется вмешаться вручную (например ввести токен).")
        sys.exit(result.returncode)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nПрервано пользователем.")
        sys.exit(1)
