# 식단 관리 앱 — 학생 식당 개인 사용 관리

> 2026 동국대학교 모바일 소프트웨어 학기말 프로젝트 · Jetpack Compose

매일 먹은 음식을 사진과 함께 기록하면, **식품의약품안전처 영양성분 데이터**로 열량과 탄수화물·단백질·지방을 계산해 하루·한 주·한 달의 영양 균형을 보여주는 안드로이드 앱입니다.

- **시연 영상**: _(YouTube 링크 추가 예정)_
- **저장소**: https://github.com/holyash-man/Mobile_Software_Project

<!-- TODO(B): 화면 완성 후 스크린샷 추가 (입력 / 목록 / 상세 / 분석) -->

---

## 1. 주요 기능

### 음식 입력하기
- 식사 날짜·시간 입력. 시간에 따라 끼니 종류(조식·중식·석식·간식/음료)를 자동 추천하고, 직접 바꿀 수 있음
- 음식 사진 입력 (갤러리에서 고른 사진을 앱 내부에 복사해 보관하므로, 앱을 다시 켜거나 원본을 지워도 사진이 유지됨)
- 음식·반찬 검색(식약처 음식 100종)과 여러 개 추가. 검색은 공백을 무시하고 원본 식품명·분류로도 찾음
- 섭취량(g, 음료는 ml) 입력. 1인분 참고량이 기본값으로 들어감
- 입력 화면에서 탄수화물·단백질·지방·열량 **미리보기**

### 식사 보여주기
- 사진과 식사 날짜가 있는 식사 목록 (날짜별로 묶음, 최신순)
- 날짜마다 **열량 합계와 끼니 수** 요약
- 목록을 누르면 입력했던 내용과 음식별·끼니 전체 **영양소 및 열량 섭취량** 표시
- 수정·삭제

### 식사 분석하기
- **최근 1달(30일) 칼로리 총량**과 하루 평균
- 최근 1달 섭취 영양소를 **끼니 종류(조식·중식·석식·간식/음료)별**로 분석. 끼니별 열량 비율과 탄단지 비율 포함
- 최근 1주 보기 전환 (한 주 영양 균형)
- **영양 데이터 출처** 표시

### 시연용 샘플 데이터
- 기록이 비어 있을 때 '샘플 데이터 불러오기'로 최근 30일치 학생식당 식사(약 86끼)를 넣을 수 있음

---

## 2. 설치 및 실행 방법

### 준비물
| 항목 | 버전 |
|---|---|
| Android Studio | 2026.2 이상 (JDK 내장) |
| Android SDK | Platform **37** (compileSdk 37, targetSdk 36, minSdk 26) |
| 기기 | Android 8.0(API 26) 이상 기기 또는 에뮬레이터 |

### 실행
1. 저장소를 받습니다.
   ```bash
   git clone https://github.com/holyash-man/Mobile_Software_Project.git
   ```
2. Android Studio에서 폴더를 열고 Gradle 동기화가 끝날 때까지 기다립니다. SDK 37이 없으면 SDK Manager에서 설치합니다.
3. 실행할 기기를 고르고 ▶ Run 'app' 을 누릅니다.
4. 처음에는 기록이 없으므로 목록 화면의 **샘플 데이터 불러오기**를 누르면 모든 화면을 바로 확인할 수 있습니다.

명령줄에서는 아래와 같이 실행합니다.
```bash
./gradlew assembleDebug                 # APK 빌드
./gradlew installDebug                  # 연결된 기기에 설치
```

---

## 3. 영양 데이터 출처

| 항목 | 내용 |
|---|---|
| 제공 기관 | **식품의약품안전처** |
| 데이터 | 「전국통합식품영양성분정보(음식)표준데이터」 |
| 링크 | https://www.data.go.kr/data/15100070/standard.do |
| 데이터 기준일 | 2026-09-29 |
| 사용 범위 | 원본 19,738행 중 학생식당 메뉴 **100종** |

앱의 분석 화면에도 출처를 표시합니다.

### 100종 선정 방법
- **일반 음식만 사용**: 프랜차이즈 제품(업체명 있음)은 빼고, 업체명이 '해당없음'인 일반 음식만 썼습니다.
- **분류별 구성**: 밥 12, 면·만두 10, 국·탕 10, 찌개 6, 고기반찬 11, 생선·해물 5, 달걀·두부 6, 기타반찬 8, 채소반찬 15, 김치·절임 5, 간식·빵 7, 음료 5
- **같은 음식이 여러 행일 때**: 실측 **분석값**을 우선하고(가정식 > 외식), 없으면 급식 산출값을 썼습니다. 결과는 분석값 79종, 급식 산출값 21종입니다.
- **재현 가능**: 원본 CSV에서 `tools/food-data/build_foods.py` 로 `app/src/main/assets/foods.json` 을 다시 만들 수 있습니다. 선정 목록은 `tools/food-data/selection.csv` 입니다.

자세한 규칙과 한계는 [tools/food-data/README.md](tools/food-data/README.md)에 정리했습니다.

---

## 4. 영양 계산 방식

| 계산 | 방법 |
|---|---|
| 음식 1개 | `100g(ml)당 값 × 섭취량 ÷ 100` |
| 끼니 | 그 끼니에 담긴 음식들의 합 |
| 날짜별 요약 | 그날 끼니들의 열량 합계와 끼니 수 |
| 최근 1달 | 오늘 포함 30일. 하루 평균은 기록이 없는 날도 일수에 포함 |
| 끼니 종류별 | 조식·중식·석식·간식 4가지로 나눠 합계 (기록이 없으면 0) |
| 탄단지 비율 | 탄수화물·단백질 4kcal/g, 지방 9kcal/g 로 환산한 열량 비율 |

- **반올림**: 계산 중에는 반올림하지 않고, 화면에 보일 때만 열량은 정수, 탄단지는 소수 첫째 자리로 반올림합니다. 음식마다 먼저 반올림하면 합계 오차가 쌓이기 때문입니다.
- **음료 단위**: 음료는 100ml 기준이며 1ml ≈ 1g 으로 봅니다.
- **끼니 자동 추천**: 05:00~10:29 조식, 10:30~14:59 중식, 17:00~20:59 석식, 그 외는 간식/음료입니다.

**검증 예시** (실제 식약처 값, 단위 테스트로 확인)

| 음식 | 섭취량 | 열량 | 탄수화물 | 단백질 | 지방 |
|---|---|---|---|---|---|
| 쌀밥 | 210g | 348.6 | 78.393 | 7.056 | 0.672 |
| 김치찌개 | 300g | 183.0 | 8.940 | 11.310 | 11.250 |
| 배추김치 | 40g | 15.2 | 2.596 | 0.792 | 0.172 |
| **합계** | | **546.8 → 547 kcal** | **89.9 g** | **19.2 g** | **12.1 g** |

---

## 5. 테스트

```bash
./gradlew testDebugUnitTest              # 로컬 단위 테스트
./gradlew connectedDebugAndroidTest      # 계측 테스트 (Compose UI · Room, 에뮬레이터 필요)
```

### 로컬 단위 테스트 (89개)
| 영역 | 테스트 클래스 (개수) | 확인 내용 |
|---|---|---|
| **영양 계산** | NutritionCalculatorTest (10), NutritionFormatTest (3), NutritionTest (2) | 섭취량 환산, ml 음료, 잘못된 섭취량 거부, 끼니 합계, **실제 식약처 값 손계산 비교**, 반올림, 탄단지 비율 |
| **분석·요약** | MealAnalyzerTest (6), MealTypeClassifierTest (4) | 날짜별 열량·끼니 수, 최근 30일 경계, 끼니 종류별 합계, 하루 평균, 시간대별 끼니 추천 |
| 음식 데이터 | FoodCatalogParserTest (6), FoodSearchTest (7), LocalFoodRepositoryTest (3), FoodsAssetTest (4) | JSON 검증, 검색 순위, 캐시, **실제 foods.json 100종 품질** (열량 = 탄단지 환산 ±15%) |
| 식사 저장 | MealMappersTest (4), RoomMealRepositoryTest (13) | 시각 변환, 저장·수정·삭제, 입력 검증, 사진 복사·교체·삭제, 일괄 저장 |
| 화면 로직 | MealInputViewModelTest (7), MealListViewModelTest (4), MealDetailViewModelTest (3), AnalysisViewModelTest (4) | 미리보기, 저장·수정, 날짜별 목록, 상세 합계, 1달/1주 분석, 출처 |
| 샘플 데이터 | SampleMealGeneratorTest (7), SampleDataSeederTest (2) | 재현성, 기간, 끼니 분류, 하루 평균 열량 |

### 계측 테스트
| 테스트 | 확인 내용 |
|---|---|
| MealDaoTest (5) | 실제 SQLite에서 저장, 최신순 정렬, 수정, 끼니 삭제 시 음식 함께 삭제, 일괄 저장 |
| InternalPhotoStorageTest (3) | 실제 기기에서 사진을 앱 내부로 복사(원본 삭제 후에도 유지), 삭제, 없는 파일 처리 |
| NavigationTest (1) | 하단 탭 이동 _(Compose UI 테스트, B가 추가 예정)_ |

<!-- TODO(B): Compose UI 테스트 추가 후 표 갱신 -->

---

## 6. 프로젝트 구조

```
app/src/main/java/kr/ac/dongguk/diet/
├── data/
│   ├── food/        식약처 음식 100종 로딩·검색 (LocalFoodRepository)
│   ├── local/       Room DB: 식사 기록 (meals, meal_items)
│   ├── photo/       식사 사진을 앱 내부 저장소에 보관
│   ├── sample/      시연용 샘플 식사 생성
│   └── repository/  Repository 인터페이스 (데이터 ↔ 화면 계약)
├── domain/
│   ├── model/       Food, Meal, Nutrition, MealType ...
│   └── calc/        영양 계산, 끼니 추천, 날짜별·기간 분석
├── ui/              Compose 화면과 화면별 ViewModel
│   ├── input/  list/  detail/  analysis/  navigation/  theme/
│   └── AppViewModelProvider.kt
├── AppContainer.kt  수동 의존성 주입
└── MainActivity.kt
app/src/main/assets/foods.json   음식 100종 (build_foods.py 로 생성)
tools/food-data/                 데이터 선정 목록·생성 스크립트·원본 증빙
tools/ai-log/                    프롬프트 자동 기록 hook
docs/prompt/                     팀원별 AI 사용 로그
```

**기술 스택**: Kotlin 2.4 · Jetpack Compose (Material 3) · Navigation Compose · Room · kotlinx-serialization · Coil · JUnit4 · kotlinx-coroutines-test · Compose UI Test

---

## 7. 역할분담

| 팀원 | 역할 | 담당 |
|---|---|---|
| **신성재** | 데이터·로직 (A) | 식약처 음식 데이터 100종 선정·가공, 음식 Repository, Room 식사 DB·Repository, 영양 계산·분석 로직, 화면별 ViewModel, 시연용 샘플 데이터, 단위 테스트, 프로젝트 초기 설정, README |
| **조정민** | 화면·디자인 (B) | 입력·목록·상세·분석 화면(Compose), 사진 입력, 내비게이션, 데이터 출처 화면, Compose UI 테스트, 디자인, 시연 영상 |

두 역할은 `data/repository/Repositories.kt` 의 인터페이스와 각 화면 ViewModel의 상태(`uiState`)로 연결됩니다.

---

## 8. AI(바이브 코딩) 사용 목록

이 프로젝트는 AI 코딩 도구로 구현했습니다. **구현 중 입력한 프롬프트 원문과 설명**을 팀원별 파일에 남겼습니다.

| 팀원 | 로그 | 사용 도구 |
|---|---|---|
| 신성재 | [docs/prompt/신성재.md](docs/prompt/신성재.md) | Claude Code |
| 조정민 | [docs/prompt/조정민.md](docs/prompt/조정민.md) | _(작성 예정)_ |

- **기록 형식**: 각 항목에 번호, 시각, 단계, 설명(결과·수정한 점), 프롬프트 원문이 있습니다.
- **기록 방법**: Claude Code의 `UserPromptSubmit` hook이 입력한 프롬프트를 자동으로 추가하고, 작업이 끝날 때마다 단계·설명을 채웠습니다. 실패하거나 다시 지시한 프롬프트도 그대로 두었습니다. 설정 방법은 [tools/ai-log/README.md](tools/ai-log/README.md)에 있습니다.

### 신성재 구현 흐름 요약
| 단계 | 프롬프트 | 내용 |
|---|---|---|
| 기획 | #1 | 과제 분석, 역할 분담 검토 |
| 초기 설정 | #2~#3 | Compose 프로젝트 골격, 빌드·테스트 환경 검증 |
| 협업 환경 | #4~#9 | 브랜치·PR, GitHub 계정 연결 |
| 음식 데이터 | #10~#11 | 식약처 데이터 조사·선정·변환 스크립트 |
| 음식 Repository | #13 | JSON 파싱·검색·출처 |
| 영양 계산 | #15 | 환산·끼니 추천·날짜별/기간 분석 |
| 식사 DB | #17 | Room 테이블·DAO·Repository |
| ViewModel | #19 | 입력·목록·상세·분석 화면 로직 |
| 샘플 데이터 | #21 | 시연용 최근 30일 식사 |
| README | #23 | 이 문서 |

<!-- TODO(B): 조정민 구현 흐름 요약 추가 -->
