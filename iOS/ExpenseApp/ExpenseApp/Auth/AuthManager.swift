import Foundation
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import AuthenticationServices
import CryptoKit

// MARK: - 인증 상태
enum AuthState {
    case loading
    case signedOut
    case signedIn(User)
}

// MARK: - AuthManager
@MainActor
@Observable
final class AuthManager: NSObject {

    private(set) var authState: AuthState = .loading
    private var nonce: String?            // Apple Sign-In nonce
    private var authStateHandle: AuthStateDidChangeListenerHandle?

    var currentUser: User? {
        if case .signedIn(let user) = authState { return user }
        return nil
    }

    var isSignedIn: Bool { currentUser != nil }

    // MARK: - 초기화
    override init() {
        super.init()
        observeAuthState()
    }

    deinit {
        if let handle = authStateHandle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }

    // MARK: - 인증 상태 관찰
    private func observeAuthState() {
        authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                if let user {
                    self?.authState = .signedIn(user)
                } else {
                    self?.authState = .signedOut
                }
            }
        }
    }

    // MARK: - Google Sign-In
    func signInWithGoogle() async throws {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            throw AuthError.missingClientID
        }
        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else {
            throw AuthError.missingRootViewController
        }

        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: rootVC)
        guard let idToken = result.user.idToken?.tokenString else {
            throw AuthError.missingToken
        }

        let credential = GoogleAuthProvider.credential(
            withIDToken:     idToken,
            accessToken:     result.user.accessToken.tokenString
        )
        try await Auth.auth().signIn(with: credential)
    }

    // MARK: - Apple Sign-In (Step 1: Request)
    func signInWithApple() {
        let nonce = randomNonceString()
        self.nonce = nonce

        let provider = ASAuthorizationAppleIDProvider()
        let request  = provider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(nonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate               = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    // MARK: - 로그아웃
    func signOut() throws {
        GIDSignIn.sharedInstance.signOut()
        try Auth.auth().signOut()
    }

    // MARK: - 계정 삭제
    func deleteAccount() async throws {
        try await Auth.auth().currentUser?.delete()
    }

    // MARK: - Nonce 생성 헬퍼
    private func randomNonceString(length: Int = 32) -> String {
        var randomBytes = [UInt8](repeating: 0, count: length)
        _ = SecRandomCopyBytes(kSecRandomDefault, length, &randomBytes)
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        return String(randomBytes.map { charset[Int($0) % charset.count] })
    }

    private func sha256(_ input: String) -> String {
        let hash = SHA256.hash(data: Data(input.utf8))
        return hash.compactMap { String(format: "%02x", $0) }.joined()
    }
}

// MARK: - Apple Sign-In Delegate (Step 2: Credential)
extension AuthManager: ASAuthorizationControllerDelegate {
    nonisolated func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard
            let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let nonce = Task { await self.nonce }.result.success,
            let appleIDToken = appleIDCredential.identityToken,
            let idTokenString = String(data: appleIDToken, encoding: .utf8)
        else { return }

        let credential = OAuthProvider.appleCredential(
            withIDToken:    idTokenString,
            rawNonce:       nonce,
            fullName:       appleIDCredential.fullName
        )
        Task {
            try? await Auth.auth().signIn(with: credential)
        }
    }

    nonisolated func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        print("Apple Sign-In 실패:", error.localizedDescription)
    }
}

extension AuthManager: ASAuthorizationControllerPresentationContextProviding {
    nonisolated func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first ?? UIWindow()
    }
}

// MARK: - AuthError
enum AuthError: LocalizedError {
    case missingClientID
    case missingRootViewController
    case missingToken

    var errorDescription: String? {
        switch self {
        case .missingClientID:          return "Firebase Client ID를 찾을 수 없습니다."
        case .missingRootViewController: return "Root ViewController를 찾을 수 없습니다."
        case .missingToken:             return "인증 토큰을 가져오지 못했습니다."
        }
    }
}
