# 🤖 ExpenseApp — Android (Jetpack Compose + Firebase)

> **AI로 4시간에 앱 만들기** 실전 예제 · Firebase 인증 + Firestore 클라우드 동기화 완성본

## 🗂 신규/변경 파일

| 파일 | 역할 |
|------|------|
| `app/google-services.json` | ⚠️ Firebase Console에서 교체 필요 |
| `auth/AuthManager.kt` | FirebaseAuth + Google Sign-In Flow |
| `firebase/FirestoreManager.kt` | Firestore CRUD + 실시간 callbackFlow |
| `ui/screens/LoginScreen.kt` | Google 로그인 UI |
| `ui/screens/ProfileScreen.kt` | 프로필 + 로그아웃 |
| `viewmodel/ExpenseViewModel.kt` | Firestore/DataStore 자동 분기 |
| `navigation/AppNavigation.kt` | 프로필 탭 추가 |
| `MainActivity.kt` | AuthState 분기 |

## 🔥 Firebase 설정

1. Firebase Console → Android 앱 등록 (패키지: `com.example.expenseapp`)
2. `google-services.json` → `app/` 폴더 교체
3. Authentication → Google 로그인 활성화
4. Firestore → 데이터베이스 만들기
5. `MainActivity.kt`의 `WEB_CLIENT_ID`를 실제 값으로 교체

## 🚀 실행

```
Android Studio → File → Open → ExpenseApp 폴더
Gradle Sync → Shift+F10
```

## Firestore 보안 규칙

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/expenses/{expenseId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
