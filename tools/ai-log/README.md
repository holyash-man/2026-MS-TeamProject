# AI 사용 로그 자동 기록

과제 채점 기준(AI 사용 로그 20점)에 따라 **구현 중 입력한 프롬프트를 팀원별로** `docs/prompt/<이름>.md` 에 남긴다.
Claude Code를 쓰는 경우 아래 hook을 켜 두면 프롬프트를 입력할 때마다 자동으로 추가된다.

## 설정 (각자 1회)

프로젝트 루트에 `.claude/settings.local.json` 을 만들고(이 파일은 git에 올라가지 않음) 이름 부분만 바꾼다.

```json
{
  "hooks": {
    "UserPromptSubmit": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "/usr/bin/python3 \"$CLAUDE_PROJECT_DIR/tools/ai-log/log-prompt.py\" \"docs/prompt/<이름>.md\""
          }
        ]
      }
    ]
  }
}
```

새 Claude Code 세션부터 적용된다.

## 기록 형식

```
### #12 · 2026-11-20 14:03
- 단계: 영양 계산 로직
- 설명: 100g 기준 환산 함수 작성 요청 → 반올림 오류 발견해서 #13에서 수정

(프롬프트 원문)
```

- 번호·시각·원문은 자동, **단계/설명은 작업 단위가 끝날 때 직접 채운다.**
- 실패하거나 다시 지시한 프롬프트도 지우지 말 것. 구현 과정을 보여주는 근거가 된다.
- "네", "계속" 같은 의미 없는 짧은 응답은 제출 전에 정리해도 된다.
- Claude Code 외의 AI(ChatGPT, Gemini 등)를 쓴 경우 같은 형식으로 직접 추가한다.
