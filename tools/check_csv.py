#!/usr/bin/env python3
"""CSV validation helper for admin vehicle catalog uploads."""

import argparse
import csv
import json
import sys
from collections import Counter
from pathlib import Path

REQUIRED_COLUMNS = {
    "oem_code",
    "model_name",
    "price",
    "body_type",
    "fuel_type",
    "release_year",
}

OPTIONAL_COLUMNS = {
    "trim",
    "efficiency",
    "seats",
    "drivetrain",
    "features",
    "media_assets",
}

NUMERIC_INT_FIELDS = {
    "price": {"min": 0},
    "seats": {"min": 0},
    "release_year": {"min": 1900, "max": 2100},
}

NUMERIC_FLOAT_FIELDS = {
    "efficiency": {"min": 0.0},
}

JSON_FIELDS = {"features", "media_assets"}

IDENTITY_FIELDS = ("oem_code", "model_name", "trim")


def parse_args():
    parser = argparse.ArgumentParser(description="Validate vehicle catalog CSV files before upload.")
    parser.add_argument("csv_path", type=Path, help="Path to the CSV file to validate")
    parser.add_argument(
        "--allow-empty-trim",
        action="store_true",
        help="Treat missing trim as acceptable in duplicate detection (default: still allowed but warned)",
    )
    return parser.parse_args()


def load_rows(path: Path):
    try:
        with path.open("r", encoding="utf-8-sig", newline="") as fh:
            reader = csv.DictReader(fh)
            headers = reader.fieldnames or []
            rows = list(reader)
    except FileNotFoundError:
        raise SystemExit(f"ERROR: File not found → {path}")
    except UnicodeDecodeError as exc:
        raise SystemExit(f"ERROR: Failed to decode file as UTF-8 → {exc}")
    return headers, rows


def normalize_value(value):
    if value is None:
        return ""
    return value.strip()


def validate_headers(headers, errors):
    missing = REQUIRED_COLUMNS - set(headers)
    if missing:
        errors.append(f"필수 컬럼 누락: {', '.join(sorted(missing))}")
    unknown = set(headers) - (REQUIRED_COLUMNS | OPTIONAL_COLUMNS)
    if unknown:
        extras = ", ".join(sorted(unknown))
        errors.append(f"정의되지 않은 컬럼이 존재합니다: {extras}")


def parse_int(field, value, config, row_num, errors):
    try:
        number = int(value)
    except ValueError:
        errors.append(f"{row_num}행 {field} 값이 정수가 아닙니다: '{value}'")
        return None
    if "min" in config and number < config["min"]:
        errors.append(f"{row_num}행 {field} 값이 {config['min']} 이상이어야 합니다: {number}")
    if "max" in config and number > config["max"]:
        errors.append(f"{row_num}행 {field} 값이 {config['max']} 이하이어야 합니다: {number}")
    return number


def parse_float(field, value, config, row_num, errors):
    try:
        number = float(value)
    except ValueError:
        errors.append(f"{row_num}행 {field} 값이 숫자가 아닙니다: '{value}'")
        return None
    if "min" in config and number < config["min"]:
        errors.append(f"{row_num}행 {field} 값이 {config['min']} 이상이어야 합니다: {number}")
    if "max" in config and number > config["max"]:
        errors.append(f"{row_num}행 {field} 값이 {config['max']} 이하이어야 합니다: {number}")
    return number


def validate_json(field, value, row_num, errors):
    try:
        json.loads(value)
    except json.JSONDecodeError as exc:
        errors.append(f"{row_num}행 {field} JSON 구문 오류: {exc}")


def validate_rows(rows, args, errors, warnings):
    identity_counter = Counter()

    for idx, raw_row in enumerate(rows, start=2):  # header is row 1
        row = {k: normalize_value(v) for k, v in raw_row.items()}

        for column in REQUIRED_COLUMNS:
            if not row.get(column):
                errors.append(f"{idx}행 {column} 값이 비어 있습니다.")

        for field, config in NUMERIC_INT_FIELDS.items():
            if field in row and row[field]:
                parse_int(field, row[field], config, idx, errors)

        for field, config in NUMERIC_FLOAT_FIELDS.items():
            if field in row and row[field]:
                parse_float(field, row[field], config, idx, errors)

        for field in JSON_FIELDS:
            if row.get(field):
                validate_json(field, row[field], idx, errors)

        identity = tuple(row.get(col, "") for col in IDENTITY_FIELDS)
        identity_counter[identity] += 1

        if not args.allow_empty_trim and not row.get("trim"):
            warnings.append(f"{idx}행 trim 값이 비어 있습니다. 중복 판정 시 빈 문자열로 취급됩니다.")

    duplicates = [key for key, count in identity_counter.items() if count > 1]
    for oem, model, trim in duplicates:
        human_trim = trim or "<빈 값>"
        errors.append(f"중복 항목 발견: (oem_code={oem}, model_name={model}, trim={human_trim})")


def main():
    args = parse_args()
    headers, rows = load_rows(args.csv_path)

    errors: list[str] = []
    warnings: list[str] = []

    if not headers:
        errors.append("CSV 헤더를 찾을 수 없습니다.")
    else:
        validate_headers(headers, errors)

    if rows:
        validate_rows(rows, args, errors, warnings)
    else:
        warnings.append("데이터 행이 없습니다.")

    print(f"검증 대상 파일: {args.csv_path}")
    print(f"총 행 수(헤더 제외): {len(rows)}")

    if warnings:
        print("\n경고:")
        for msg in warnings:
            print(f"  - {msg}")

    if errors:
        print("\n오류:")
        for msg in errors:
            print(f"  - {msg}")
        print(f"\n검증 결과: 실패 (오류 {len(errors)}건)")
        return 1

    print("\n검증 결과: 통과")
    return 0


if __name__ == "__main__":
    sys.exit(main())
