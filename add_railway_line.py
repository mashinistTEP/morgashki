#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
add_railway_line.py — добавление новой дороги (подгруппы) внутри компании-
перевозчика в проекте "Моргашки" (например МЖД или ОКТ.ЖД внутри РЖД).

Что делает:
  1. Спрашивает, к какой компании (RailwayGroup) относится новая дорога.
  2. Спрашивает короткий машинный код дороги (для enum, например "MZD")
     и отображаемое название (например "Московская железная дорога").
  3. Добавляет новую константу в enum RailwayLine (Locomotive.kt).
  4. Добавляет строковый ресурс с названием в strings.xml.
  5. Проверяет баланс скобок/тегов после каждой правки — если что-то не
     сошлось, изменения не сохраняются.
  6. Предлагает сразу закоммитить и запушить проект.

После этого дорога появится в приложении сама, как только у какого-нибудь
локомотива в LocomotiveCatalog.kt будет указано `line = RailwayLine.<КОД>`
(это делает отдельный скрипт add_locomotive.py при следующем обновлении,
либо вручную).

Запуск (из корня проекта android_project_v2):
    python3 add_railway_line.py
"""

import os
import re
import subprocess
import sys

LOCOMOTIVE_KT_PATH = "app/src/main/java/com/mashinist_tep70bs_145/morgashki/model/Locomotive.kt"
STRINGS_XML_PATH = "app/src/main/res/values/strings.xml"


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


def check_balance(text):
    depth_p = depth_c = 0
    for ch in text:
        if ch == "(":
            depth_p += 1
        elif ch == ")":
            depth_p -= 1
            if depth_p < 0:
                return False
        elif ch == "{":
            depth_c += 1
        elif ch == "}":
            depth_c -= 1
            if depth_c < 0:
                return False
    return depth_p == 0 and depth_c == 0


# ---------- Поиск доступных групп (RailwayGroup) ----------

def find_available_groups(kt_text):
    """
    Достаёт список констант enum RailwayGroup вместе с их строковыми
    ресурсами названий (для показа пользователю человекочитаемо).
    Возвращает список (код, string_res_name).
    """
    marker = "enum class RailwayGroup(val titleRes: Int) {"
    start_idx = kt_text.find(marker)
    if start_idx == -1:
        raise ValueError("Не нашёл enum RailwayGroup в Locomotive.kt")
    body_start = start_idx + len(marker)
    end_idx = kt_text.find("}", body_start)
    body = kt_text[body_start:end_idx]

    # строки вида:  BCH(com.....R.string.group_bch),
    pattern = re.compile(r"(\w+)\(com\.mashinist_tep70bs_145\.morgashki\.R\.string\.(\w+)\)")
    return pattern.findall(body)


def resolve_group_titles(groups, strings_xml_text):
    """groups: список (код, string_res_name) -> список (код, человекочитаемое название)"""
    result = []
    for code, res_name in groups:
        m = re.search(rf'name="{re.escape(res_name)}">([^<]*)<', strings_xml_text)
        title = m.group(1) if m else res_name
        result.append((code, title))
    return result


# ---------- Шаг 1: enum RailwayLine в Locomotive.kt ----------

def add_line_enum_constant(text, code, string_res_name, parent_group_code):
    marker = "enum class RailwayLine(val titleRes: Int, val parentGroup: RailwayGroup) {"
    start_idx = text.find(marker)
    if start_idx == -1:
        raise ValueError("Не нашёл `enum class RailwayLine(...) {` в Locomotive.kt")

    body_start = start_idx + len(marker)
    end_idx = text.find("}", body_start)
    if end_idx == -1:
        raise ValueError("Не удалось найти закрывающую '}' для enum RailwayLine.")

    body = text[body_start:end_idx]

    # для проверки на дубликат смотрим только на настоящий код, без комментариев
    body_without_comments = "\n".join(
        line for line in body.split("\n") if not line.strip().startswith("//")
    )
    if re.search(rf'\b{re.escape(code)}\s*\(', body_without_comments):
        raise ValueError(f"Константа '{code}' уже есть в RailwayLine — выбери другой код.")

    new_line = (
        f"    {code}(com.mashinist_tep70bs_145.morgashki.R.string.{string_res_name}, "
        f"RailwayGroup.{parent_group_code})"
    )

    # отделяем реальный код от закомментированных строк-подсказок ("// MZD(...")
    real_lines = []
    for line in body.split("\n"):
        stripped = line.strip()
        if stripped.startswith("//") or stripped == "":
            continue
        real_lines.append(line)

    if real_lines:
        # есть хотя бы одна настоящая константа — добавляем после последней,
        # с запятой, сохраняя комментарии как были (в конце, после кода)
        last_real = real_lines[-1]
        last_idx_in_body = body.rfind(last_real)
        before = body[:last_idx_in_body + len(last_real)]
        after = body[last_idx_in_body + len(last_real):]
        before_stripped = before.rstrip()
        if not before_stripped.endswith(","):
            before_stripped += ","
        new_body = before_stripped + "\n" + new_line + "," + after
    else:
        # enum пока пустой (только комментарии-подсказки) — вставляем перед ними
        new_body = "\n" + new_line + "\n" + body.lstrip("\n")

    return text[:body_start] + new_body + text[end_idx:]


# ---------- Шаг 2: strings.xml ----------

def add_string_resource(xml_text, res_name, value):
    if f'name="{res_name}"' in xml_text:
        raise ValueError(f"Строковый ресурс '{res_name}' уже существует в strings.xml.")

    closing_tag = "</resources>"
    idx = xml_text.rfind(closing_tag)
    if idx == -1:
        raise ValueError("Не нашёл закрывающий тег </resources> в strings.xml.")

    new_line = f'    <string name="{res_name}">{escape_xml(value)}</string>\n'
    return xml_text[:idx] + new_line + xml_text[idx:]


def escape_xml(value):
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "\\'")
        .replace('"', "&quot;")
    )


def check_xml_balance(text):
    opens = len(re.findall(r"<string\b[^>]*>", text))
    closes = text.count("</string>")
    resources_open = text.count("<resources>")
    resources_close = text.count("</resources>")
    return opens == closes and resources_open == 1 and resources_close == 1


# ---------- Основной сценарий ----------

def main():
    print("=" * 60)
    print(" Добавление новой дороги (подгруппы) внутри компании")
    print("=" * 60)
    print()

    if not os.path.isfile(LOCOMOTIVE_KT_PATH):
        print(f"ОШИБКА: не нашёл {LOCOMOTIVE_KT_PATH}")
        print("Запусти скрипт из корня проекта (там, где лежит build.gradle.kts).")
        sys.exit(1)
    if not os.path.isfile(STRINGS_XML_PATH):
        print(f"ОШИБКА: не нашёл {STRINGS_XML_PATH}")
        sys.exit(1)

    with open(LOCOMOTIVE_KT_PATH, "r", encoding="utf-8") as f:
        kt_original = f.read()
    with open(STRINGS_XML_PATH, "r", encoding="utf-8") as f:
        xml_original = f.read()

    try:
        groups = find_available_groups(kt_original)
    except ValueError as e:
        print(f"ОШИБКА: {e}")
        sys.exit(1)

    if not groups:
        print("Не нашёл ни одной компании (RailwayGroup) в Locomotive.kt.")
        print("Сначала добавь компанию через add_railway_group.py.")
        sys.exit(1)

    groups_with_titles = resolve_group_titles(groups, xml_original)

    print("Доступные компании-перевозчики:")
    for i, (code, title) in enumerate(groups_with_titles, 1):
        print(f"  {i}) {code} — {title}")
    print()

    while True:
        choice = ask("Выбери номер компании, к которой относится новая дорога")
        if choice.isdigit() and 1 <= int(choice) <= len(groups_with_titles):
            parent_group_code = groups_with_titles[int(choice) - 1][0]
            break
        print("  Введи номер из списка выше.")

    print()
    code = ask("Короткий код дороги (например «MZD» для Московской ЖД)").upper()
    code = re.sub(r"[^A-Z0-9_]", "", code)
    if not code or not code[0].isalpha():
        print("ОШИБКА: код должен начинаться с буквы и содержать только A-Z, 0-9, _.")
        sys.exit(1)

    display_name = ask("Полное название дороги (например «Московская железная дорога»)")

    string_res_name = f"line_{code.lower()}"

    print()
    print("Будет добавлено:")
    print(f"  1) В Locomotive.kt: константа {code} в enum RailwayLine (родитель: {parent_group_code})")
    print(f"  2) В strings.xml: <string name=\"{string_res_name}\">{display_name}</string>")
    print()

    if not ask_yes_no("Продолжить?", default_yes=True):
        print("Отменено.")
        sys.exit(0)

    try:
        kt_new = add_line_enum_constant(kt_original, code, string_res_name, parent_group_code)
    except ValueError as e:
        print(f"ОШИБКА: {e}")
        sys.exit(1)

    if not check_balance(kt_new):
        print("ОШИБКА: после правки Locomotive.kt баланс скобок нарушен — файл НЕ изменён.")
        sys.exit(1)

    try:
        xml_new = add_string_resource(xml_original, string_res_name, display_name)
    except ValueError as e:
        print(f"ОШИБКА: {e}")
        sys.exit(1)

    if not check_xml_balance(xml_new):
        print("ОШИБКА: после правки strings.xml структура тегов нарушена — файл НЕ изменён.")
        sys.exit(1)

    with open(LOCOMOTIVE_KT_PATH, "w", encoding="utf-8") as f:
        f.write(kt_new)
    with open(STRINGS_XML_PATH, "w", encoding="utf-8") as f:
        f.write(xml_new)

    print()
    print(f"Готово! Дорога «{code}» ({display_name}) добавлена внутри {parent_group_code}.")
    print()
    print(f"Подсказка: теперь при добавлении локомотива этой дороги через")
    print(f"add_locomotive.py укажи для него line = RailwayLine.{code} — этот шаг")
    print(f"пока делается вручную в LocomotiveCatalog.kt после генерации блока локомотива.")
    print()

    if ask_yes_no("Закоммитить и запушить проект в GitHub сейчас?", default_yes=True):
        commit_message = ask("Сообщение коммита", default=f"Добавлена дорога {display_name}")
        run_git(["git", "add", "."])
        run_git(["git", "commit", "-m", commit_message])
        run_git(["git", "push", "origin", "main"])
        print()
        print("Готово! Проверь вкладку Actions на GitHub.")
    else:
        print("Хорошо, изменения остались только локально.")


def run_git(args):
    print(f"\n$ {' '.join(args)}")
    result = subprocess.run(args)
    if result.returncode != 0:
        print(f"\nКоманда завершилась с ошибкой (код {result.returncode}).")
        sys.exit(result.returncode)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nПрервано пользователем.")
        sys.exit(1)
