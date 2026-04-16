import SwiftUI
import AuthenticationServices

// MARK: - RootView (인증 상태에 따라 화면 분기)
struct RootView: View {
    @State private var authManager = AuthManager()

    var body: some View {
        Group {
            switch authManager.authState {
            case .loading:
                ProgressView("로딩 중...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .signedOut:
                LoginView(authManager: authManager)

            case .signedIn(let user):
                AuthenticatedView(user: user, authManager: authManager)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: authManager.isSignedIn)
    }
}

// MARK: - AuthenticatedView (로그인 후 메인 앱)
struct AuthenticatedView: View {
    let user: FirebaseAuth.User
    let authManager: AuthManager
    @State private var vm: ExpenseViewModel

    init(user: FirebaseAuth.User, authManager: AuthManager) {
        self.user = user
        self.authManager = authManager
        // FirestoreManager를 storage로 사용
        let firestoreManager = FirestoreManager(userId: user.uid)
        self._vm = State(initialValue: ExpenseViewModel(storage: firestoreManager))
    }

    var body: some View {
        TabView {
            ExpenseListView(vm: vm)
                .tabItem { Label("지출", systemImage: "list.bullet.rectangle.portrait") }

            StatisticsView(vm: vm)
                .tabItem { Label("통계", systemImage: "chart.bar.fill") }

            ProfileView(authManager: authManager, user: user)
                .tabItem { Label("프로필", systemImage: "person.circle") }
        }
    }
}

// MARK: - LoginView
struct LoginView: View {
    let authManager: AuthManager

    @State private var isLoading       = false
    @State private var errorMessage: String? = nil

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            // 로고 영역
            VStack(spacing: 16) {
                ZStack {
                    Circle()
                        .fill(Color.blue.gradient)
                        .frame(width: 100, height: 100)
                    Image(systemName: "creditcard.fill")
                        .font(.system(size: 44))
                        .foregroundStyle(.white)
                }

                Text("가계부")
                    .font(.largeTitle.bold())

                Text("로그인하여 데이터를 클라우드에\n안전하게 저장하고 동기화하세요.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }

            Spacer()

            // 로그인 버튼 영역
            VStack(spacing: 12) {
                if let error = errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(.red)
                        .padding(.horizontal)
                }

                // Google 로그인 버튼
                Button {
                    Task { await signInGoogle() }
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "globe")
                            .font(.title3)
                        Text("Google로 계속하기")
                            .font(.headline)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color(.systemBackground))
                    .foregroundStyle(.primary)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(.separator), lineWidth: 1))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(isLoading)

                // Apple 로그인 버튼
                SignInWithAppleButton(.signIn) { request in
                    request.requestedScopes = [.fullName, .email]
                } onCompletion: { _ in
                    authManager.signInWithApple()
                }
                .signInWithAppleButtonStyle(.black)
                .frame(height: 50)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .disabled(isLoading)

                if isLoading {
                    ProgressView()
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 48)
        }
        .background(Color(.systemGroupedBackground))
    }

    private func signInGoogle() async {
        isLoading = true
        errorMessage = nil
        do {
            try await authManager.signInWithGoogle()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}

// MARK: - ProfileView (로그인 정보 + 로그아웃)
struct ProfileView: View {
    let authManager: AuthManager
    let user: FirebaseAuth.User
    @State private var showingSignOutAlert = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack(spacing: 16) {
                        AsyncImage(url: user.photoURL) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Image(systemName: "person.circle.fill")
                                .font(.system(size: 44))
                                .foregroundStyle(.blue)
                        }
                        .frame(width: 60, height: 60)
                        .clipShape(Circle())

                        VStack(alignment: .leading, spacing: 4) {
                            Text(user.displayName ?? "사용자")
                                .font(.headline)
                            Text(user.email ?? "")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.vertical, 4)
                }

                Section {
                    Button(role: .destructive) {
                        showingSignOutAlert = true
                    } label: {
                        Label("로그아웃", systemImage: "arrow.backward.circle")
                    }
                }
            }
            .navigationTitle("프로필")
            .alert("로그아웃", isPresented: $showingSignOutAlert) {
                Button("로그아웃", role: .destructive) {
                    try? authManager.signOut()
                }
                Button("취소", role: .cancel) {}
            } message: {
                Text("정말 로그아웃하시겠습니까?")
            }
        }
    }
}
