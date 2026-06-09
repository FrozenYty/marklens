"""
Generate 200+ sample exam papers for MarkLens OCR testing.
40 students × 5 subjects = 200 papers.
Scores follow a normal distribution (mean ~75%, sd ~12%).

Usage: python generate_papers.py
Output: ./test-papers/*.png
"""
from PIL import Image, ImageDraw, ImageFont
import os
import random

random.seed(42)  # reproducible

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "test-papers")
os.makedirs(OUTPUT_DIR, exist_ok=True)

W, H = 1240, 1754

try:
    FONT_TITLE = ImageFont.truetype("arial.ttf", 54)
    FONT_LABEL = ImageFont.truetype("arial.ttf", 36)
    FONT_VALUE = ImageFont.truetype("arial.ttf", 34)
    FONT_SMALL = ImageFont.truetype("arial.ttf", 26)
    FONT_TABLE = ImageFont.truetype("arial.ttf", 30)
    FONT_BIG = ImageFont.truetype("arialbd.ttf", 40)
except:
    FONT_TITLE = ImageFont.load_default()
    FONT_LABEL = ImageFont.load_default()
    FONT_VALUE = ImageFont.load_default()
    FONT_SMALL = ImageFont.load_default()
    FONT_TABLE = ImageFont.load_default()
    FONT_BIG = ImageFont.load_default()

BLACK = (30, 30, 30)
GREY = (120, 120, 120)
LIGHT = (230, 230, 230)
TEAL = (0, 121, 107)
RED = (180, 40, 40)
WHITE = (255, 255, 255)

# 40 students across 4 classes
STUDENTS = [
    # Class 3-1
    ("Wei Zhang",   "2024001", "Class 3-1"),
    ("Na Li",       "2024002", "Class 3-1"),
    ("Fang Wang",   "2024003", "Class 3-1"),
    ("Yang Liu",    "2024004", "Class 3-1"),
    ("Jie Chen",    "2024005", "Class 3-1"),
    ("Min Zhao",    "2024006", "Class 3-1"),
    ("Lei Huang",   "2024007", "Class 3-1"),
    ("Ting Zhou",   "2024008", "Class 3-1"),
    ("Hao Wu",      "2024009", "Class 3-1"),
    ("Rui Xu",      "2024010", "Class 3-1"),
    # Class 3-2
    ("Yue Sun",     "2024011", "Class 3-2"),
    ("Jun Ma",      "2024012", "Class 3-2"),
    ("Li Zhu",      "2024013", "Class 3-2"),
    ("Bin Hu",      "2024014", "Class 3-2"),
    ("Yan Guo",     "2024015", "Class 3-2"),
    ("Tao Lin",     "2024016", "Class 3-2"),
    ("Xin He",      "2024017", "Class 3-2"),
    ("Fei Gao",     "2024018", "Class 3-2"),
    ("Wen Luo",     "2024019", "Class 3-2"),
    ("Yu Zheng",    "2024020", "Class 3-2"),
    # Class 3-3
    ("Lan Xie",     "2024021", "Class 3-3"),
    ("Dong Han",    "2024022", "Class 3-3"),
    ("Qian Shen",   "2024023", "Class 3-3"),
    ("Hui Deng",    "2024024", "Class 3-3"),
    ("Xiao Peng",   "2024025", "Class 3-3"),
    ("Jing Feng",   "2024026", "Class 3-3"),
    ("Bo Jiang",    "2024027", "Class 3-3"),
    ("Ning Cai",    "2024028", "Class 3-3"),
    ("Lu Tang",     "2024029", "Class 3-3"),
    ("Rong Dong",   "2024030", "Class 3-3"),
    # Class 3-4
    ("Kai Song",    "2024031", "Class 3-4"),
    ("Mei Liang",   "2024032", "Class 3-4"),
    ("Chao Pan",    "2024033", "Class 3-4"),
    ("Xia Ye",     "2024034", "Class 3-4"),
    ("Pei Qin",     "2024035", "Class 3-4"),
    ("Gang Shi",    "2024036", "Class 3-4"),
    ("Yan Bai",     "2024037", "Class 3-4"),
    ("Shan Ren",    "2024038", "Class 3-4"),
    ("Ping Cao",    "2024039", "Class 3-4"),
    ("Zhi Gu",      "2024040", "Class 3-4"),
]

# 5 subjects with different question counts and max scores
SUBJECTS = [
    {"name": "Math",    "questions": 5, "max_per_q": 20, "mean": 15.0, "sd": 3.0},
    {"name": "English", "questions": 5, "max_per_q": 20, "mean": 14.5, "sd": 3.2},
    {"name": "Physics", "questions": 4, "max_per_q": 25, "mean": 18.0, "sd": 4.0},
    {"name": "Chinese", "questions": 6, "max_per_q": 15, "mean": 11.0, "sd": 2.5},
    {"name": "Biology", "questions": 5, "max_per_q": 20, "mean": 14.0, "sd": 3.5},
]


def generate_scores(n_questions, max_per_q, mean, sd):
    """Generate normally-distributed scores clamped to [0, max_per_q]."""
    scores = []
    for _ in range(n_questions):
        s = round(random.gauss(mean, sd))
        s = max(0, min(max_per_q, s))
        scores.append(s)
    return scores


def draw_exam_paper(name, student_id, class_name, subject_info, scores):
    """Generate one exam paper image."""
    subject = subject_info["name"]
    max_per_q = subject_info["max_per_q"]
    total = sum(scores)
    max_total = max_per_q * len(scores)

    img = Image.new("RGB", (W, H), WHITE)
    d = ImageDraw.Draw(img)

    # === HEADER ===
    title = f"{subject} Examination"
    bbox = d.textbbox((0, 0), title, font=FONT_TITLE)
    tw = bbox[2] - bbox[0]
    d.text(((W - tw) // 2, 40), title, fill=BLACK, font=FONT_TITLE)

    sub = "Grade 3 - Semester 2 - 2024"
    bbox2 = d.textbbox((0, 0), sub, font=FONT_SMALL)
    sw = bbox2[2] - bbox2[0]
    d.text(((W - sw) // 2, 105), sub, fill=GREY, font=FONT_SMALL)

    d.line([(60, 145), (W - 60, 145)], fill=TEAL, width=3)

    # === STUDENT INFO ===
    info_y = 180

    d.text((80, info_y), "Name:", fill=BLACK, font=FONT_LABEL)
    d.rectangle([(200, info_y - 4), (480, info_y + 42)], outline=GREY, width=2)
    d.text((210, info_y + 2), name, fill=BLACK, font=FONT_VALUE)

    d.text((520, info_y), "ID:", fill=BLACK, font=FONT_LABEL)
    d.rectangle([(590, info_y - 4), (830, info_y + 42)], outline=GREY, width=2)
    d.text((600, info_y + 2), student_id, fill=BLACK, font=FONT_VALUE)

    d.text((870, info_y), "Class:", fill=BLACK, font=FONT_LABEL)
    d.rectangle([(980, info_y - 4), (1180, info_y + 42)], outline=GREY, width=2)
    d.text((990, info_y + 2), class_name, fill=BLACK, font=FONT_VALUE)

    # === SUBJECT + TOTAL SCORE ===
    row2_y = 280

    d.text((80, row2_y), "Subject:", fill=BLACK, font=FONT_LABEL)
    d.rectangle([(230, row2_y - 4), (480, row2_y + 42)], outline=GREY, width=2)
    d.text((240, row2_y + 2), subject, fill=BLACK, font=FONT_VALUE)

    d.text((700, row2_y), "Total Score:", fill=BLACK, font=FONT_LABEL)
    d.rectangle([(920, row2_y - 8), (1100, row2_y + 48)], outline=TEAL, width=3)
    d.text((940, row2_y - 2), str(total), fill=TEAL, font=FONT_BIG)
    d.text((1020, row2_y + 10), f"/ {max_total}", fill=GREY, font=FONT_SMALL)

    d.line([(60, 355), (W - 60, 355)], fill=LIGHT, width=2)

    # === QUESTION SCORE TABLE ===
    table_y = 390
    col_x = [80, 250, 520, 750, 980]
    col_w = [170, 270, 230, 230, 200]
    row_h = 55
    headers = ["No.", "Question", "Score", "Max", "Result"]

    # Header row
    d.rectangle([(col_x[0], table_y), (col_x[-1] + col_w[-1], table_y + row_h)],
                fill=(240, 240, 240))
    for i, h in enumerate(headers):
        d.text((col_x[i] + 15, table_y + 12), h, fill=BLACK, font=FONT_LABEL)
    d.rectangle([(col_x[0], table_y), (col_x[-1] + col_w[-1], table_y + row_h)],
                outline=GREY, width=2)

    # Data rows
    for qi in range(len(scores)):
        ry = table_y + row_h + qi * row_h
        score = scores[qi]

        if qi % 2 == 0:
            d.rectangle([(col_x[0], ry), (col_x[-1] + col_w[-1], ry + row_h)],
                        fill=(250, 250, 250))
        d.rectangle([(col_x[0], ry), (col_x[-1] + col_w[-1], ry + row_h)],
                    outline=LIGHT, width=1)

        d.text((col_x[0] + 40, ry + 12), str(qi + 1), fill=BLACK, font=FONT_TABLE)
        d.text((col_x[1] + 15, ry + 12), f"Question {qi + 1}", fill=BLACK, font=FONT_TABLE)
        score_color = TEAL if score >= max_per_q * 0.6 else RED
        d.text((col_x[2] + 40, ry + 12), str(score), fill=score_color, font=FONT_TABLE)
        d.text((col_x[3] + 40, ry + 12), str(max_per_q), fill=GREY, font=FONT_TABLE)
        result = "Pass" if score >= max_per_q * 0.6 else "Fail"
        d.text((col_x[4] + 20, ry + 12), result,
               fill=TEAL if result == "Pass" else RED, font=FONT_TABLE)

    # Table borders
    table_bottom = table_y + row_h + len(scores) * row_h
    d.rectangle([(col_x[0], table_y), (col_x[-1] + col_w[-1], table_bottom)],
                outline=GREY, width=2)
    for i in range(1, len(col_x)):
        d.line([(col_x[i], table_y), (col_x[i], table_bottom)], fill=GREY, width=1)

    # === FOOTER ===
    footer_y = H - 100
    d.line([(60, footer_y - 20), (W - 60, footer_y - 20)], fill=LIGHT, width=1)
    d.text((80, footer_y), "Teacher's Signature: ____________", fill=GREY, font=FONT_SMALL)
    d.text((700, footer_y), "Date: 2024-06-01", fill=GREY, font=FONT_SMALL)

    return img


def main():
    # Clean old files
    for f in os.listdir(OUTPUT_DIR):
        if f.endswith(".png"):
            os.remove(os.path.join(OUTPUT_DIR, f))

    count = 0
    for subj in SUBJECTS:
        for i, (name, sid, cls) in enumerate(STUDENTS):
            scores = generate_scores(subj["questions"], subj["max_per_q"], subj["mean"], subj["sd"])
            img = draw_exam_paper(name, sid, cls, subj, scores)
            filename = f"{subj['name'].lower()}_{i+1:02d}_{name.replace(' ', '_')}.png"
            img.save(os.path.join(OUTPUT_DIR, filename))
            count += 1

    print(f"Generated {count} exam papers in {OUTPUT_DIR}")
    print(f"  {len(STUDENTS)} students x {len(SUBJECTS)} subjects")
    print(f"  Subjects: {', '.join(s['name'] for s in SUBJECTS)}")
    print(f"\nTo push to device:")
    print(f"  adb push test-papers/ /sdcard/Pictures/MarkLens-Test/")


if __name__ == "__main__":
    main()
