# 📱 ExpenseApp — iOS (SwiftUI + Firebase)

> **AI로 4시간에 앱 만들기** 실전 예제 · Firebase 인증 + Firestore 클라우드 동기화 완성본

## 🗂 프로젝트 구조

```
ExpenseApp/
├── ExpenseApp.xcodeproj/
├── ExpenseApp/
│   ├── ExpenseApp.swift              # @main + Firebase.configure()
│   ├── GoogleService-Info.plist      # ⚠️ Firebase Console에서 교체 필요
│   ├── Auth/
│   │   ├── AuthManager.swift         # Google/Apple Sign-In + Auth 상태 관찰
│   │   └── LoginView.swift           # RootView / LoginView / ProfileView
│   ├── Firebase/
│   │   └── FirestoreManager.swift    # CRUD + 실시간 SnapshotListener
│   ├── Models/Expense.swift
│   ├── ViewModels/ExpenseViewModel.swift
│   ├── Views/ (ExpenseListView, ExpenseComponents, StatisticsView)
│   ├── Storage/ExpenseStorage.swift
│   └── Extensions/Formatters.swift
└── ExpenseAppTests/ExpenseViewModelTests.swift
```

## 🔥 Firebase 설정

1. **Firebase Console** → iOS 앱 등록 → `GoogleService-Info.plist` 다운로드 → 프로젝트 루트에 교체
2. **Authentication** → Google, Apple 로그인 활성화
3. **Firestore Database** → 데이터베이스 만들기 (프로덕션 모드)
4. **SPM 의존성 추가**
   - `https://github.com/firebase/firebase-ios-sdk` → FirebaseAuth, FirebaseFirestore, FirebaseFirestoreSwift
   - `https://github.com/google/GoogleSignIn-iOS` → GoogleSignIn, GoogleSignInSwift
5. **Info.plist**에 REVERSED_CLIENT_ID URL Scheme 추가

## 🚀 실행

```
ExpenseApp.xcodeproj 더블클릭 → Firebase 설정 완료 → Cmd+R
요구사항: Xcode 15+, iOS 17+
```

## ✨ Firebase 추가 기능

| 기능 | 설명 |
|------|------|
| Google 로그인 | GIDSignIn + FirebaseAuth 연동 |
| Apple 로그인 | ASAuthorization + nonce 보안 처리 |
| Firestore 동기화 | 실시간 SnapshotListener (멀티 디바이스) |
| 낙관적 업데이트 | UI 즉시 반영 → 실패 시 자동 롤백 |
| 오프라인 캐싱 | Firestore 내장 (자동) |
