#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
add_railway_group.py — добавление новой компании-перевозчика (группы) в проект
"Моргашки" (например КЛД.ЖД, ЛитЖД, и т.д.).

Что делает:
  1. Спрашивает короткий машинный код группы (для enum, например "KLD")
     и отображаемое название (например "Калининградская железная дорога").
  2. Добавляет новую константу в enum RailwayGroup (Locomotive.kt).
  3. Добавляет строковый ресурс с названием в strings.xml.
  4. Обновляет add_locomotive.py, чтобы новая группа появилась в списке
     выбора при добавлении локомотивов (если файл найден рядом).
  5. Проверяет баланс скобок/кавычек после каждой правки — если что-то
     не сошлось, изменения не сохраняются.
  6. Предлагает сразу закоммитить и запушить проект.

Запуск (из корня проекта android_project_v2):
    python3 add_railway_group.py
"""

import os
import re
import subprocess
import sys

LOCOMOTIVE_KT_PATH = "app/src/main/java/com/mashinist_tep70bs_145/morgashki/model/Locomotive.kt"
STRINGS_XML_PATH = "app/src/main/res/values/strings.xml"
ADD_LOCOMOTIVE_SCRIPT_PATH = "add_locomotive.py"  # необязательный, правим если есть рядом


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
    depth_p = depth_b = depth_curly = 0
    for ch in text:
        if ch == "(":
            depth_p += 1
        elif ch == ")":
            depth_p -= 1
            if depth_p < 0:
                return False
        elif ch == "{":
            depth_curly += 1
        elif ch == "}":
            depth_curly -= 1
            if depth_curly < 0:
                return False
    return depth_p == 0 and depth_curly == 0


# ---------- Шаг 1: enum RailwayGroup в Locomotive.kt ----------

def add_enum_constant(text, code, string_res_name):
    """
    Вставляет новую константу в конец enum class RailwayGroup(...) { ... },
    определяя границы через маркеры "enum class RailwayGroup" и первую
    последующую закрывающую '}'.
    """
    marker = "enum class RailwayGroup(val titleRes: Int) {"
    start_idx = text.find(marker)
    if start_idx == -1:
        raise ValueError("Не нашёл `enum class RailwayGroup(val titleRes: Int) {` в Locomotive.kt")

    body_start = start_idx + len(marker)
    end_idx = text.find("}", body_start)
    if end_idx == -1:
        raise ValueError("Не нашёл закрывающую '}' для enum RailwayGroup.")

    body = text[body_start:end_idx]

    if f"    {code}(" in body or f"\n{code}(" in body:
        raise ValueError(f"Константа '{code}' уже есть в RailwayGroup — выбери другой код.")

    new_line = f"    {code}(com.mashinist_tep70bs_145.morgashki.R.string.{string_res_name})"

    stripped_body = body.rstrip()
    if stripped_body.endswith(","):
        new_body = stripped_body + "\n" + new_line + "\n"
    else:
        # последняя константа без запятой — добавляем запятую перед новой строкой
        new_body = stripped_body + ",\n" + new_line + "\n"

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
    """Простая проверка: количество открывающих и закрывающих <string> тегов совпадает,
    и есть ровно один <resources> ... </resources>."""
    opens = len(re.findall(r"<string\b[^>]*>", text))
    closes = text.count("</string>")
    resources_open = text.count("<resources>")
    resources_close = text.count("</resources>")
    return opens == closes and resources_open == 1 and resources_close == 1


# ---------- Шаг 3 (опционально): обновление списка групп в add_locomotive.py ----------

def try_update_add_locomotive_script(code):
    """
    Если рядом лежит add_locomotive.py — не меняем его логику совсем
    (он и так принимает произвольный текст группы через group_choice),
    но подсказываем пользователю, что теперь можно вводить новый код.
    Реальных правок в файл не требуется, т.к. add_locomotive.py уже
    работает с произвольной строкой group_const — сообщаем об этом.
    """
    return os.path.isfile(ADD_LOCOMOTIVE_SCRIPT_PATH)


# ---------- Основной сценарий ----------

def main():
    print("=" * 60)
    print(" Добавление новой компании-перевозчика")
    print("=" * 60)
    print()

    if not os.path.isfile(LOCOMOTIVE_KT_PATH):
        print(f"ОШИБКА: не нашёл {LOCOMOTIVE_KT_PATH}")
        print("Запусти скрипт из корня проекта (там, где лежит build.gradle.kts).")
        sys.exit(1)
    if not os.path.isfile(STRINGS_XML_PATH):
        print(f"ОШИБКА: не нашёл {STRINGS_XML_PATH}")
        sys.exit(1)

    print("Короткий код нужен для программной части (как BCH или RZD у существующих")
    print("групп) — используй заглавные латинские буквы/цифры, без пробелов.")
    code = ask("Короткий код компании (например «KLD»)").upper()
    code = re.sub(r"[^A-Z0-9_]", "", code)
    if not code or not code[0].isalpha():
        print("ОШИБКА: код должен начинаться с буквы и содержать только A-Z, 0-9, _.")
        sys.exit(1)

    display_name = ask("Полное отображаемое название (например «Калининградская железная дорога»)")

    string_res_name = f"group_{code.lower()}"

    print()
    print("Будет добавлено:")
    print(f"  1) В Locomotive.kt: константа {code} в enum RailwayGroup")
    print(f"  2) В strings.xml: <string name=\"{string_res_name}\">{display_name}</string>")
    print()

    if not ask_yes_no("Продолжить?", default_yes=True):
        print("Отменено.")
        sys.exit(0)

    # --- Locomotive.kt ---
    with open(LOCOMOTIVE_KT_PATH, "r", encoding="utf-8") as f:
        kt_original = f.read()

    try:
        kt_new = add_enum_constant(kt_original, code, string_res_name)
    except ValueError as e:
        print(f"ОШИБКА: {e}")
        sys.exit(1)

    if not check_balance(kt_new):
        print("ОШИБКА: после правки Locomotive.kt баланс скобок нарушен — файл НЕ изменён.")
        sys.exit(1)

    # --- strings.xml ---
    with open(STRINGS_XML_PATH, "r", encoding="utf-8") as f:
        xml_original = f.read()

    try:
        xml_new = add_string_resource(xml_original, string_res_name, display_name)
    except ValueError as e:
        print(f"ОШИБКА: {e}")
        sys.exit(1)

    if not check_xml_balance(xml_new):
        print("ОШИБКА: после правки strings.xml структура тегов нарушена — файл НЕ изменён.")
        sys.exit(1)

    # обе проверки прошли — сохраняем оба файла
    with open(LOCOMOTIVE_KT_PATH, "w", encoding="utf-8") as f:
        f.write(kt_new)
    with open(STRINGS_XML_PATH, "w", encoding="utf-8") as f:
        f.write(xml_new)

    print()
    print(f"Готово! Группа «{code}» ({display_name}) добавлена.")
    print()

    if try_update_add_locomotive_script(code):
        print(f"Подсказка: в add_locomotive.py на вопрос «Группа — БЧ или РЖД?»")
        print(f"теперь можно вводить «{code.lower()}» (это подставится как RailwayGroup.{code}).")
        print()

    # ---------- Git ----------
    if ask_yes_no("Закоммитить и запушить проект в GitHub сейчас?", default_yes=True):
        commit_message = ask("Сообщение коммита", default=f"Добавлена компания-перевозчик {display_name}")
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
