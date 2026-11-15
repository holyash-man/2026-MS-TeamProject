#!/usr/bin/env python3
"""Claude Code UserPromptSubmit hook: 입력한 프롬프트를 개인 AI 로그 파일에 자동으로 추가한다.

사용법 (각자 .claude/settings.local.json 의 hook command):
    /usr/bin/python3 "$CLAUDE_PROJECT_DIR/tools/ai-log/log-prompt.py" "docs/prompt/<이름>.md"

- 로그 파일 경로는 프로젝트 루트 기준 상대 경로.
- 항목마다 번호·시각·프롬프트 원문을 남기고, '단계/설명' 칸은 나중에 직접 채운다.
- 어떤 오류가 나도 프롬프트 전송을 막지 않도록 항상 exit 0.
"""
import json
import os
import re
import sys
from datetime import datetime


def fence_for(text: str) -> str:
    longest = max((len(m) for m in re.findall(r"`+", text)), default=0)
    return "`" * max(3, longest + 1)


def main() -> None:
    if len(sys.argv) < 2:
        return
    payload = json.load(sys.stdin)
    prompt = (payload.get("prompt") or "").strip()
    if not prompt:
        return

    root = os.environ.get("CLAUDE_PROJECT_DIR") or payload.get("cwd") or os.getcwd()
    log_path = os.path.join(root, sys.argv[1])
    os.makedirs(os.path.dirname(log_path), exist_ok=True)

    existing = ""
    if os.path.exists(log_path):
        with open(log_path, encoding="utf-8") as f:
            existing = f.read()
    number = len(re.findall(r"^### #\d+", existing, flags=re.M)) + 1

    fence = fence_for(prompt)
    entry = (
        f"\n---\n\n"
        f"### #{number} · {datetime.now():%Y-%m-%d %H:%M}\n"
        f"- 단계: \n"
        f"- 설명: \n\n"
        f"{fence}text\n{prompt}\n{fence}\n"
    )
    with open(log_path, "a", encoding="utf-8") as f:
        f.write(entry)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:  # 로그 실패가 작업을 막으면 안 됨
        print(f"[ai-log] 기록 실패: {e}", file=sys.stderr)
    sys.exit(0)
