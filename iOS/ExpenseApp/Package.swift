// swift-tools-version: 5.9
// Firebase iOS SDK — Swift Package Manager 의존성 설정
// Xcode: File → Add Package Dependencies → 아래 URL 입력

/*
 Package URL:
   https://github.com/firebase/firebase-ios-sdk

 추가할 라이브러리 (체크):
   ✅ FirebaseAuth
   ✅ FirebaseFirestore
   ✅ FirebaseFirestoreSwift

 Google Sign-In URL:
   https://github.com/google/GoogleSignIn-iOS

 추가할 라이브러리:
   ✅ GoogleSignIn
   ✅ GoogleSignInSwift

 Info.plist에 추가:
   <key>GIDClientID</key>
   <string>$(YOUR_REVERSED_CLIENT_ID)</string>

   <key>CFBundleURLTypes</key>
   <array>
     <dict>
       <key>CFBundleURLSchemes</key>
       <array>
         <string>$(YOUR_REVERSED_CLIENT_ID)</string>
       </array>
     </dict>
   </array>

 GoogleService-Info.plist:
   Firebase Console → 프로젝트 설정 → iOS 앱 → 구성 파일 다운로드
   ExpenseApp 타겟에 추가 (Copy if needed 체크)
*/

import PackageDescription

// 이 파일은 SPM 설정 참고용입니다.
// Xcode에서 직접 Package Dependencies를 추가하세요.
let package = Package(
    name: "ExpenseApp",
    platforms: [.iOS(.v17)],
    dependencies: [
        .package(
            url: "https://github.com/firebase/firebase-ios-sdk",
            from: "11.0.0"
        ),
        .package(
            url: "https://github.com/google/GoogleSignIn-iOS",
            from: "8.0.0"
        ),
    ],
    targets: [
        .target(
            name: "ExpenseApp",
            dependencies: [
                .product(name: "FirebaseAuth",           package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore",      package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestoreSwift", package: "firebase-ios-sdk"),
                .product(name: "GoogleSignIn",           package: "GoogleSignIn-iOS"),
                .product(name: "GoogleSignInSwift",      package: "GoogleSignIn-iOS"),
            ]
        )
    ]
)
