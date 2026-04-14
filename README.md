# AI로 4시간에 앱 만들기 (ExpenseApp)
'AI로 4시간에 앱 만들기' 개발 서적과 관련한 각 플랫폼별 소스 파일 저장소입니다. 많은 관심과 부탁드리오며 소스 코드에 대한 의견을 언제든 환영합니다. 많은 의견도 부탁드리겠습니다. 감사합니다.

- E-Mail : ibillkim@gmail.com
- 각종 이슈 및 의견도 본 GitHub의 이슈로 등록주셔도 언제든 환경하겠습니다.


# 📱 ExpenseApp — iOS (SwiftUI)

> **AI로 4시간에 앱 만들기** 실전 예제 프로젝트  
> Part 3~8의 모든 iOS 소스코드를 통합한 완성형 가계부 앱

---

## 🚀 Xcode에서 열기

```
1. ExpenseApp.xcodeproj 파일을 더블클릭
2. Xcode 상단 시뮬레이터 선택 → iPhone 16 Pro 권장
3. ▶ 실행 (Cmd+R)
```

### 요구사항
| 항목 | 버전 |
|------|------|
| Xcode | 15.0 이상 |
| iOS | 17.0 이상 |
| Swift | 5.9 이상 |
| macOS | Ventura (13.0) 이상 |

### 테스트 실행
```
Product → Test (Cmd+U)
```

---

## 🗂 프로젝트 구조

```
ExpenseApp/
├── ExpenseApp.xcodeproj/          ← Xcode 프로젝트 파일 (여기 더블클릭)
├── ExpenseApp/
│   ├── ExpenseApp.swift           ← @main 진입점 + TabView
│   ├── Models/
│   │   └── Expense.swift          ← 데이터 모델 + 상수
│   ├── ViewModels/
│   │   └── ExpenseViewModel.swift ← @Observable 상태 관리 핵심
│   ├── Views/
│   │   ├── ExpenseListView.swift  ← 메인 리스트 화면
│   │   ├── ExpenseComponents.swift← Row·Filter·Add·Edit 컴포넌트
│   │   └── StatisticsView.swift   ← 통계 화면
│   ├── Storage/
│   │   └── ExpenseStorage.swift   ← Protocol + UserDefaults + Fake
│   ├── Extensions/
│   │   └── Formatters.swift       ← Int/Date/Double 포맷팅
│   └── Resources/
│       └── Assets.xcassets        ← 앱 아이콘
└── ExpenseAppTests/
    └── ExpenseViewModelTests.swift ← XCTest 단위 테스트 (9개)
```

---

## ✨ 구현 기능

| 기능 | 설명 |
|------|------|
| 지출 추가 | 금액·카테고리·메모 입력, 실시간 유효성 검사 |
| 지출 목록 | LazyVStack + 스와이프 삭제 |
| 지출 수정 | 탭 → Sheet 수정 |
| 카테고리 필터 | 수평 스크롤 Chip 필터 |
| 실시간 검색 | `.searchable()` 수식어 |
| 실행 취소 | 삭제 후 4초 내 Undo 토스트 |
| 통계 화면 | 카테고리별 ProgressView 게이지 |
| 로컬 저장 | UserDefaults JSON 직렬화 |
| 단위 테스트 | XCTest, Given-When-Then 패턴 |

---

## 🏛 아키텍처

```
View (SwiftUI)
    ↕ @Observable 자동 관찰
ViewModel (@MainActor @Observable)
    ↕ Protocol 추상화
Storage (UserDefaultsStorage / InMemoryStorage)
```

---

## 📚 관련 챕터

| 파일 | 챕터 |
|------|------|
| Expense.swift | Part 3 · Ch.07 데이터 모델 |
| ExpenseStorage.swift | Part 3, 5 · Ch.08, 13 |
| ExpenseViewModel.swift | Part 5 · Ch.12 ViewModel |
| ExpenseListView.swift | Part 4 · Ch.09 리스트 화면 |
| ExpenseComponents.swift | Part 4 · Ch.11 입력 화면 |
| StatisticsView.swift | Part 5 · Ch.16 통계 |
| Formatters.swift | Part 5 · Ch.15 포맷팅 |
| ExpenseViewModelTests.swift | Part 6 · Ch.20 테스트 |


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
