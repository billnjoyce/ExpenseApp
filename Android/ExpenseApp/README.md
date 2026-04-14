# 🤖 ExpenseApp — Android (Jetpack Compose)

> **AI로 4시간에 앱 만들기** 실전 예제 프로젝트  
> Part 3~8의 모든 Android 소스코드를 통합한 완성형 가계부 앱

---

## 🚀 Android Studio에서 열기

```
1. Android Studio 실행
2. File → Open → ExpenseApp 폴더 선택 (settings.gradle.kts가 있는 폴더)
3. Gradle Sync 완료 대기 (자동 실행, 최초 1~3분 소요)
4. 에뮬레이터: Pixel 8 Pro (API 35) 권장
5. ▶ 실행 (Shift+F10)
```

### 요구사항
| 항목 | 버전 |
|------|------|
| Android Studio | Hedgehog 2023.1.1 이상 |
| Kotlin | 2.0 이상 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 35 (Android 15) |
| Gradle | 8.9 |

### 테스트 실행
```bash
./gradlew test
# 또는 Android Studio → 우클릭 ExpenseViewModelTest → Run
```

---

## 🗂 프로젝트 구조

```
ExpenseApp/                              ← 여기를 Android Studio로 열기
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml              ← Version Catalog (의존성 버전 관리)
│   └── wrapper/
│       └── gradle-wrapper.properties
├── gradlew / gradlew.bat
└── app/
    ├── build.gradle.kts               ← 앱 의존성 설정
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/expenseapp/
        │   ├── MainActivity.kt        ← 앱 진입점
        │   ├── model/
        │   │   └── Expense.kt         ← 데이터 모델 + 상수
        │   ├── viewmodel/
        │   │   └── ExpenseViewModel.kt← StateFlow 상태 관리 핵심
        │   ├── storage/
        │   │   └── DataStoreManager.kt← DataStore + Fake (테스트용)
        │   ├── ui/
        │   │   ├── theme/Theme.kt     ← Material3 테마
        │   │   ├── screens/
        │   │   │   ├── ExpenseListScreen.kt ← 메인 화면
        │   │   │   └── StatisticsScreen.kt  ← 통계 화면
        │   │   └── components/
        │   │       └── ExpenseComponents.kt ← 재사용 Composable 모음
        │   ├── navigation/
        │   │   └── AppNavigation.kt   ← NavHost + BottomNav
        │   └── utils/
        │       └── Formatters.kt      ← 포맷팅 확장함수
        └── res/
            ├── values/strings.xml
            ├── values/themes.xml
            └── mipmap-*/ic_launcher.png
```

---

## ✨ 구현 기능

| 기능 | 설명 |
|------|------|
| 지출 추가 | 금액·카테고리·메모, 유효성 검사 + Snackbar 에러 |
| 지출 목록 | LazyColumn + SwipeToDismissBox 삭제 |
| 지출 수정 | 탭 → ModalBottomSheet 수정 |
| 카테고리 필터 | LazyRow FilterChip |
| 실시간 검색 | StateFlow combine 3개 Flow 결합 |
| 실행 취소 | Snackbar + SharedFlow undoEvent |
| 통계 화면 | 카테고리별 LinearProgressIndicator |
| 로컬 저장 | DataStore Preferences + JSON 직렬화 |
| 네비게이션 | NavHost + BottomNavigationBar |
| 단위 테스트 | JUnit + runTest (10개 케이스) |

---

## ⚙️ 문제 해결

### Gradle Sync 실패 시
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

### gradle-wrapper.jar 없음 오류
Android Studio가 자동으로 다운로드합니다.  
수동: `gradle wrapper --gradle-version 8.9` 실행

### minSdk 오류
에뮬레이터 API Level을 26 이상으로 설정하세요.

---

## 📚 관련 챕터

| 파일 | 챕터 |
|------|------|
| Expense.kt | Part 3 · Ch.07 데이터 모델 |
| DataStoreManager.kt | Part 3, 5 · Ch.08, 13 |
| ExpenseViewModel.kt | Part 5 · Ch.12 ViewModel |
| ExpenseListScreen.kt | Part 4 · Ch.10 리스트 화면 |
| ExpenseComponents.kt | Part 4 · Ch.11 입력 화면 |
| AppNavigation.kt | Part 4 · Ch.12 네비게이션 |
| StatisticsScreen.kt | Part 5 · Ch.16 통계 |
| Formatters.kt | Part 5 · Ch.15 포맷팅 |
| ExpenseViewModelTest.kt | Part 6 · Ch.20 테스트 |
