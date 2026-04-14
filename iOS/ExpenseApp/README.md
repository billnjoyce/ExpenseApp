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
