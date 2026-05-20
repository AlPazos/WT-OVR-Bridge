#!/usr/bin/env python3
"""
GesBate PDF → CSV converter for WT-OVR-Bridge.
Reads competition bracket PDFs exported from GesBate and generates:
  athletes.csv, categories.csv, matches.csv

Usage:
  python3 gesbate2csv.py                   # scans ~/Descargas/*.pdf
  python3 gesbate2csv.py file1.pdf ...     # specific files

Output CSVs are placed in the current directory.
Then copy them to src/main/resources/ and hit GET /admin/reload.
"""

import math
import re
import subprocess
import sys
import csv
import os
from pathlib import Path

# ── Configuration ─────────────────────────────────────────────────────────────
VIDEO_QUOTA = 1
MATCH_VICTORY_CRITERIA = "BESTOF3"
FLAG = "ESP"

CATEGORY_DEFAULTS = dict(
    body_level=26, head_level=5,
    rounds=3, round_min=2, round_sec=0,
    kyeshi_min=1, kyeshi_sec=0,
    golden_point="false", golden_min=1, golden_sec=0,
    max_diff=12, max_gamjeoms=5,
)

# ── Regex patterns ─────────────────────────────────────────────────────────────
RE_CATEGORY = re.compile(
    r'^(HOME|MULLER)\s+(SUB-\d+|CADETE|XUNIOR|SENIOR|JUNIOR)\s+(P\d+)\s*$',
    re.IGNORECASE,
)
RE_ATHLETE = re.compile(
    r'^([A-ZÁÉÍÓÚÑÜ][A-ZÁÉÍÓÚÑÜ\s]+),\s*'
    r'([A-ZÁÉÍÓÚÑÜ][A-ZÁÉÍÓÚÑÜa-záéíóúñü\s]+?)'
    r'(?:\s*\(cs\s*(\d+)\))?\s*$'
)
RE_TAPIZ = re.compile(r'^TAPIZ\s+(\d+)\s*$')
RE_WEIGHT = re.compile(r'^([-+]?\d+)\s*kg\s*$')
RE_MATCH_NUM = re.compile(r'^\[?\s*\d+\s*\]?\s*$')
RE_CHAMPIONSHIP = re.compile(r'^(CAMPEONATO|CPTO|COPA|OPEN)\b', re.IGNORECASE)
RE_DATE = re.compile(r'^\d{1,2}\s+de\s+\w+\s+de\s+\d{4}\s*$', re.IGNORECASE)
RE_SKIP = re.compile(
    r'^(CAMPE[ÓO]N|SUBCAMPE[ÓO]N|3[ºo°]\s*CLASIFICADO|QF|SF|Final|'
    r'<\s*MA[ÑN]ANA\s*>|<\s*TARDE\s*>|GesBate|xl\.ingenieria|'
    r'PETO|POTENCIA|\d+ª\s*rnd|\d+ª\d+ª|QFSF|SFQF)\b',
    re.IGNORECASE,
)
RE_COMPETITORS = re.compile(r'^\d+ competidores?$', re.IGNORECASE)


def _skip(line: str) -> bool:
    if not line:
        return True
    if RE_MATCH_NUM.match(line):
        return True
    if RE_DATE.match(line):
        return True
    if RE_SKIP.search(line):
        return True
    if RE_COMPETITORS.match(line):
        return True
    return False


# ── PDF parsing ────────────────────────────────────────────────────────────────

def extract_text(pdf: Path) -> str:
    result = subprocess.run(
        ['pdftotext', str(pdf), '-'],
        capture_output=True, text=True, encoding='utf-8',
    )
    if result.returncode != 0:
        print(f"  [!] pdftotext error for {pdf}: {result.stderr.strip()}", file=sys.stderr)
    return result.stdout


def parse_pdf(text: str) -> list[dict]:
    """Parse PDF text into bracket blocks (one per weight category)."""
    blocks = []
    championship = ''
    block = None
    pending = None  # athlete waiting for club line

    for raw in text.splitlines():
        line = raw.strip()
        if not line:
            continue

        if RE_CHAMPIONSHIP.match(line):
            championship = line
            continue

        m = RE_CATEGORY.match(line)
        if m:
            if block is not None:
                if pending:
                    block['athletes'].append(pending)
                    pending = None
                blocks.append(block)
            block = {
                'championship': championship,
                'gender': 'MALE' if m.group(1).upper() == 'HOME' else 'FEMALE',
                'age_group': m.group(2).upper(),
                'weight_code': m.group(3).upper(),
                'weight_label': None,
                'mat': 1,
                'athletes': [],
            }
            pending = None
            continue

        if block is None:
            continue

        m = RE_TAPIZ.match(line)
        if m:
            block['mat'] = int(m.group(1))
            continue

        m = RE_WEIGHT.match(line)
        if m and block['weight_label'] is None:
            block['weight_label'] = m.group(1) + ' kg'
            continue

        m = RE_ATHLETE.match(line)
        if m:
            if pending:
                block['athletes'].append(pending)
            pending = {
                'family': m.group(1).strip(),
                'given': m.group(2).strip(),
                'seed': int(m.group(3)) if m.group(3) else 0,
                'club': '',
            }
            continue

        if pending is not None:
            if not _skip(line) and not RE_CATEGORY.match(line):
                pending['club'] = line
                block['athletes'].append(pending)
                pending = None
            else:
                if _skip(line):
                    block['athletes'].append(pending)
                    pending = None

    if block:
        if pending:
            block['athletes'].append(pending)
        blocks.append(block)

    return blocks


# ── Bracket generation ─────────────────────────────────────────────────────────

def _next_power_of_2(n: int) -> int:
    if n <= 1:
        return 1
    p = 1
    while p < n:
        p <<= 1
    return p


def _seeding_positions(bracket_size: int) -> list[int]:
    """
    Return 0-indexed bracket slot positions for seeds 1, 2, 3, 4, 5, ...
    Seeds 1 and 2 land on opposite halves → can only meet in the final.
    Seeds 3 and 4 are placed in the quarters of seeds 2 and 1 respectively.
    """
    TABLE = {
        1:  [0],
        2:  [0, 1],
        4:  [0, 3, 2, 1],
        8:  [0, 7, 4, 3, 2, 5, 6, 1],
        16: [0, 15, 8, 7, 4, 11, 12, 3, 2, 13, 10, 5, 6, 9, 14, 1],
        32: [0, 31, 16, 15, 8, 23, 24, 7, 4, 27, 20, 11, 12, 19, 28, 3,
             2, 29, 18, 13, 10, 21, 26, 5, 6, 25, 22, 9, 14, 17, 30, 1],
    }
    if bracket_size in TABLE:
        return TABLE[bracket_size]
    # Fallback for unusual sizes: sequential
    return list(range(bracket_size))


def _round_phase(total_rounds: int, round_idx: int) -> str:
    remaining = total_rounds - round_idx
    return {1: 'F', 2: 'SF', 3: 'QF'}.get(remaining, f'R{round_idx + 1}')


def _build_slots(athletes: list[dict], bracket_size: int) -> list:
    """Place athletes into bracket slots. Returns list of (athlete_dict | None)."""
    slots = [None] * bracket_size
    positions = _seeding_positions(bracket_size)
    seeded = sorted([a for a in athletes if a['seed'] > 0], key=lambda a: a['seed'])
    unseeded = [a for a in athletes if a['seed'] == 0]
    ordered = seeded + unseeded
    for i, ath in enumerate(ordered):
        if i < len(positions):
            slots[positions[i]] = ath
    return slots


def generate_matches(block: dict, prefix: str, athlete_ids: dict) -> list[dict]:
    """
    Generate match records from a bracket block.
    prefix: unique string prefix for match numbers (e.g. 'M_S21_P1')
    athlete_ids: maps athlete_key → ovrInternalId
    """
    athletes = block['athletes']
    n = len(athletes)
    if n <= 1:
        return []

    bracket_size = _next_power_of_2(n)
    total_rounds = int(math.log2(bracket_size)) if bracket_size > 1 else 1
    slots = _build_slots(athletes, bracket_size)

    # Build all rounds as nested lists.
    # Each element is either (match_id, entity_left, entity_right) or None (pure bye).
    # entity = athlete dict (known) | match_id string (TBD winner) | None (bye)

    counter = [1]

    def next_id() -> str:
        mid = f'{prefix}_{counter[0]:02d}'
        counter[0] += 1
        return mid

    round_data: list[list] = []   # per-round list of (mid|None, left, right)
    current_level = list(slots)

    for _r in range(total_rounds):
        level = []
        next_level = []
        for i in range(0, len(current_level), 2):
            left = current_level[i]
            right = current_level[i + 1] if i + 1 < len(current_level) else None

            if left is None and right is None:
                level.append((None, None, None))
                next_level.append(None)
            elif left is None:
                level.append((None, None, right))
                next_level.append(right)
            elif right is None:
                level.append((None, left, None))
                next_level.append(left)
            else:
                mid = next_id()
                level.append((mid, left, right))
                next_level.append(mid)

        round_data.append(level)
        current_level = next_level

    # Convert to match rows
    matches = []
    for r_idx, level in enumerate(round_data):
        phase = _round_phase(total_rounds, r_idx)
        for m_idx, (mid, left, right) in enumerate(level):
            if mid is None:
                continue  # bye, skip

            # Next match in the following round
            next_mid = None
            next_color = None
            if r_idx + 1 < len(round_data):
                next_pair_idx = m_idx // 2
                next_entry = round_data[r_idx + 1][next_pair_idx]
                if next_entry[0] is not None:
                    next_mid = next_entry[0]
                    next_color = 'BLUE' if m_idx % 2 == 0 else 'RED'

            def resolve(entity) -> str:
                if entity is None:
                    return ''
                if isinstance(entity, dict):
                    return athlete_ids.get(_ath_key(entity), '')
                return ''  # match ID placeholder → athlete unknown at start

            matches.append({
                'matchNumber': mid,
                'mat': block['mat'],
                'phase': phase,
                'categoryName': _category_name(block),
                'categoryGender': block['gender'],
                'categorySubCategory': block['weight_code'],
                'blueOvrId': resolve(left),
                'redOvrId': resolve(right),
                'videoQuota': VIDEO_QUOTA,
                'matchVictoryCriteria': MATCH_VICTORY_CRITERIA,
                'nextMatchNumber': next_mid or '',
                'nextMatchColor': next_color or '',
            })

    return matches


# ── Helpers ───────────────────────────────────────────────────────────────────

def _ath_key(a: dict) -> tuple:
    return (a['family'], a['given'])


def _category_name(block: dict) -> str:
    ag = block['age_group']
    if block['weight_label']:
        return f"{ag} {block['weight_label']}"
    return f"{ag} {block['weight_code']}"


def _match_prefix(block: dict) -> str:
    gender = 'M' if block['gender'] == 'MALE' else 'F'
    ag = block['age_group'].replace('-', '')
    return f"{gender}_{ag}_{block['weight_code']}"


def _scoreboard_name(a: dict) -> str:
    """Generate short scoreboard name: FAMILY, G."""
    initial = a['given'][0].upper() if a['given'] else '?'
    return f"{a['family']}, {initial}."


def ask_weight(block: dict, known_weights: dict) -> str:
    """Prompt user for the weight label if not found in PDF."""
    key = (block['gender'], block['age_group'], block['weight_code'])
    if key in known_weights:
        return known_weights[key]
    gender_label = 'MASCULINO' if block['gender'] == 'MALE' else 'FEMENINO'
    prompt = (
        f"  [{gender_label}] {block['age_group']} {block['weight_code']} "
        f"({block['championship']}) — ¿peso? (ej: -68 kg, 80 kg): "
    )
    val = input(prompt).strip()
    if val and not val.endswith(' kg'):
        val = val + ' kg'
    known_weights[key] = val
    return val


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    if len(sys.argv) > 1:
        pdf_files = [Path(p) for p in sys.argv[1:]]
    else:
        dl = Path.home() / 'Descargas'
        pdf_files = sorted(dl.glob('*.pdf'))

    if not pdf_files:
        print("No se encontraron PDFs.", file=sys.stderr)
        sys.exit(1)

    print(f"Procesando {len(pdf_files)} PDF(s)...")

    all_blocks: list[dict] = []
    for pdf in pdf_files:
        print(f"  → {pdf.name}")
        text = extract_text(pdf)
        blocks = parse_pdf(text)
        print(f"     {len(blocks)} categorías encontradas")
        all_blocks.extend(blocks)

    if not all_blocks:
        print("No se encontraron categorías en los PDFs.", file=sys.stderr)
        sys.exit(1)

    # ── Ask for missing weights ────────────────────────────────────────────────
    print("\nRevisando pesos de categorías...")
    known_weights: dict = {}
    for block in all_blocks:
        if block['weight_label'] is None:
            block['weight_label'] = ask_weight(block, known_weights)

    # ── Build athletes ─────────────────────────────────────────────────────────
    athletes_seen: dict = {}  # ath_key → row dict
    ath_id_counter = [1]

    def get_or_create_athlete(a: dict) -> str:
        key = _ath_key(a)
        if key in athletes_seen:
            return athletes_seen[key]['ovrInternalId']
        gender_prefix = 'M' if a.get('gender', 'MALE') == 'MALE' else 'F'
        ath_id = f'ATH{gender_prefix}{ath_id_counter[0]:04d}'
        ath_id_counter[0] += 1
        athletes_seen[key] = {
            'ovrInternalId': ath_id,
            'scoreboardName': _scoreboard_name(a),
            'givenName': a['given'],
            'familyName': a['family'],
            'flagAbbreviation': FLAG,
            'rank': 0,
            'seed': a['seed'],
            'gender': a.get('gender', 'MALE'),
        }
        return ath_id

    # ── Build categories ───────────────────────────────────────────────────────
    categories_seen: dict = {}  # (name, gender, subcat) → row dict

    def get_or_create_category(block: dict) -> None:
        name = _category_name(block)
        key = (name, block['gender'], block['weight_code'])
        if key not in categories_seen:
            d = CATEGORY_DEFAULTS
            categories_seen[key] = {
                'name': name,
                'gender': block['gender'],
                'subCategory': block['weight_code'],
                'bodyLevel': d['body_level'],
                'headLevel': d['head_level'],
                'rounds': d['rounds'],
                'roundMinutes': d['round_min'],
                'roundSeconds': d['round_sec'],
                'kyeShiMinutes': d['kyeshi_min'],
                'kyeShiSeconds': d['kyeshi_sec'],
                'goldenPoint': d['golden_point'],
                'goldenMinutes': d['golden_min'],
                'goldenSeconds': d['golden_sec'],
                'maxDiff': d['max_diff'],
                'maxGamJeoms': d['max_gamjeoms'],
            }

    # ── Generate data ──────────────────────────────────────────────────────────
    all_matches = []

    for block in all_blocks:
        gender = block['gender']
        for a in block['athletes']:
            a['gender'] = gender
            get_or_create_athlete(a)
        get_or_create_category(block)

        athlete_ids = {_ath_key(a): get_or_create_athlete(a) for a in block['athletes']}
        prefix = _match_prefix(block)
        matches = generate_matches(block, prefix, athlete_ids)
        all_matches.extend(matches)

    # ── Write CSVs ─────────────────────────────────────────────────────────────
    _write_athletes(list(athletes_seen.values()))
    _write_categories(list(categories_seen.values()))
    _write_matches(all_matches)

    print(f"\nListo:")
    print(f"  athletes.csv   → {len(athletes_seen)} atletas")
    print(f"  categories.csv → {len(categories_seen)} categorías")
    print(f"  matches.csv    → {len(all_matches)} combates")
    print("\nCopia los CSV a src/main/resources/ y llama a GET /admin/reload")


def _write_athletes(rows: list[dict]):
    headers = ['ovrInternalId', 'scoreboardName', 'givenName', 'familyName',
               'flagAbbreviation', 'rank', 'seed', 'gender']
    _write_csv('athletes.csv', headers,
               [[r[h] for h in headers] for r in rows])


def _write_categories(rows: list[dict]):
    headers = ['name', 'gender', 'subCategory', 'bodyLevel', 'headLevel',
               'rounds', 'roundMinutes', 'roundSeconds',
               'kyeShiMinutes', 'kyeShiSeconds', 'goldenPoint',
               'goldenMinutes', 'goldenSeconds', 'maxDiff', 'maxGamJeoms']
    _write_csv('categories.csv', headers,
               [[r[h] for h in headers] for r in rows])


def _write_matches(rows: list[dict]):
    headers = ['matchNumber', 'mat', 'phase', 'categoryName', 'categoryGender',
               'categorySubCategory', 'blueOvrId', 'redOvrId',
               'videoQuota', 'matchVictoryCriteria', 'nextMatchNumber', 'nextMatchColor']
    _write_csv('matches.csv', headers,
               [[r[h] for h in headers] for r in rows])


def _write_csv(filename: str, headers: list[str], rows: list[list]):
    with open(filename, 'w', newline='', encoding='utf-8') as f:
        w = csv.writer(f)
        w.writerow(headers)
        w.writerows(rows)
    print(f"  Escrito: {filename}")


if __name__ == '__main__':
    main()
