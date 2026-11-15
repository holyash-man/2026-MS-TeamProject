#!/usr/bin/env python3
"""식약처 음식 영양성분 원본 CSV에서 앱용 음식 100개를 골라 foods.json 을 만든다.

사용법 (프로젝트 루트에서):
    python3 tools/food-data/build_foods.py <원본 CSV 경로>

원본: 식품의약품안전처 '전국통합식품영양성분정보(음식)표준데이터'
      https://www.data.go.kr/data/15100070/standard.do  (CSV, CP949 인코딩)

입력
    tools/food-data/selection.csv   원본식품명,표시명,분류  (선정 목록, 직접 관리)
출력
    app/src/main/assets/foods.json        앱이 읽는 음식 데이터
    tools/food-data/foods_selected_raw.csv 선택된 원본 행 (원본 컬럼 그대로, 출처 증빙용)

선택 규칙
    1. 업체명이 '해당없음'인 일반 음식만 사용 (프랜차이즈 제품 제외)
    2. 같은 식품명이 여러 행이면 식품기원 우선순위가 높은 행을 고른다:
       실측 분석값(가정식 > 외식) → 급식 산출값(산업체 > 중고등 > 초등) → 외식 산출값
    3. 그래도 여러 행이면 식품코드가 가장 작은 행 (결과가 항상 같도록)
"""
import csv
import io
import json
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SELECTION = os.path.join(ROOT, "tools", "food-data", "selection.csv")
OUT_JSON = os.path.join(ROOT, "app", "src", "main", "assets", "foods.json")
OUT_RAW = os.path.join(ROOT, "tools", "food-data", "foods_selected_raw.csv")

SOURCE_URL = "https://www.data.go.kr/data/15100070/standard.do"

ORIGIN_PRIORITY = [
    "가정식(분석 함량)",
    "외식(분석함량)",
    "산업체급식(재료량 기반 산출 함량)",
    "중고등학교급식(재료량 기반 산출함량)",
    "초등학교급식(재료량 기반 산출 함량)",
    "외식(재료량 기반 산출함량)",
]

COL_KCAL = "에너지(kcal)"
COL_CARBS = "탄수화물(g)"
COL_PROTEIN = "단백질(g)"
COL_FAT = "지방(g)"


def read_raw(path):
    data = open(path, "rb").read()
    for enc in ("utf-8-sig", "cp949"):
        try:
            text = data.decode(enc)
            break
        except UnicodeDecodeError:
            continue
    else:
        raise SystemExit("원본 CSV 인코딩을 알 수 없음 (UTF-8/CP949 아님)")
    reader = csv.DictReader(io.StringIO(text))
    return reader.fieldnames, list(reader)


def read_selection():
    with open(SELECTION, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def number(value, field, name):
    value = (value or "").strip()
    if value == "":
        raise ValueError(f"'{name}' 의 {field} 값이 비어 있음")
    return round(float(value), 2)


def origin_rank(row):
    origin = row["식품기원명"]
    return ORIGIN_PRIORITY.index(origin) if origin in ORIGIN_PRIORITY else len(ORIGIN_PRIORITY)


SERVING_ORIGIN = "산업체급식(재료량 기반 산출 함량)"


def weight(row):
    """'식품중량' (예: '136.40ml', '400g') → 숫자. 없거나 0이면 None."""
    raw = (row.get("식품중량") or "").strip().lower()
    try:
        value = float(raw.rstrip("gml").strip())
    except ValueError:
        return None
    return round(value, 1) if value > 0 else None


def serving_size(candidates, picked):
    """기본 섭취량 참고값.
    1순위: 같은 이름의 산업체급식(성인 단체급식) 1인분 중량
    2순위: 선택된 행의 식품중량 (단, 분석 시료 단위인 100g/100ml 은 1인분이 아니므로 제외)
    """
    for r in candidates:
        if r["식품기원명"] == SERVING_ORIGIN and weight(r):
            return weight(r)
    w = weight(picked)
    return w if w and w != 100 else None


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    fieldnames, rows = read_raw(sys.argv[1])
    generic = [r for r in rows if r["업체명"] == "해당없음"]
    by_name = {}
    for r in generic:
        by_name.setdefault(r["식품명"], []).append(r)

    selection = read_selection()
    problems = []
    seen_display = set()
    foods, picked_rows = [], []

    for i, sel in enumerate(selection, start=1):
        name, display, category = sel["원본식품명"], sel["표시명"], sel["분류"]
        if display in seen_display:
            problems.append(f"표시명 중복: {display}")
        seen_display.add(display)
        candidates = by_name.get(name)
        if not candidates:
            problems.append(f"원본에 없음: {name}")
            continue
        row = sorted(candidates, key=lambda r: (origin_rank(r), r["식품코드"]))[0]
        basis = row["영양성분함량기준량"].strip().lower()
        if basis not in ("100g", "100ml"):
            problems.append(f"기준량이 100g/100ml 아님: {name} ({basis})")
            continue
        try:
            nutrition = {
                "kcal": number(row[COL_KCAL], COL_KCAL, name),
                "carbs": number(row[COL_CARBS], COL_CARBS, name),
                "protein": number(row[COL_PROTEIN], COL_PROTEIN, name),
                "fat": number(row[COL_FAT], COL_FAT, name),
            }
        except ValueError as e:
            problems.append(str(e))
            continue
        foods.append({
            "id": i,
            "code": row["식품코드"],
            "name": display,
            "sourceName": name,
            "category": category,
            # 급식 산출 데이터는 고형 음식도 기준량을 100ml 로 표기하므로 단위는 분류로 정한다
            "unit": "ml" if category == "음료" else "g",
            "per100": nutrition,
            "servingSize": serving_size(candidates, row),
            "origin": row["식품기원명"],
        })
        picked_rows.append(row)

    if problems:
        print("문제 발견 — foods.json 을 만들지 않음:", file=sys.stderr)
        for p in problems:
            print("  -", p, file=sys.stderr)
        sys.exit(1)

    ref_dates = sorted({r["데이터기준일자"] for r in picked_rows})
    output = {
        "source": {
            "provider": "식품의약품안전처",
            "dataset": "전국통합식품영양성분정보(음식)표준데이터",
            "url": SOURCE_URL,
            "referenceDate": ref_dates[-1],
            "note": "영양성분은 100g(음료는 100ml) 기준. 급식 산출값의 100ml 표기는 100g 으로 간주하고, "
                    "음료는 1ml ≈ 1g 으로 환산한다. servingSize 는 산업체급식 1인분 중량(참고값).",
        },
        "foods": foods,
    }
    os.makedirs(os.path.dirname(OUT_JSON), exist_ok=True)
    with open(OUT_JSON, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
        f.write("\n")
    with open(OUT_RAW, "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fieldnames)
        w.writeheader()
        w.writerows(picked_rows)

    print(f"foods.json: {len(foods)}개 (기준일 {output['source']['referenceDate']})")


if __name__ == "__main__":
    main()
