REALITY SHIFT — Native Android starter project

এটি Web App নয়। এটি Kotlin + Jetpack Compose দিয়ে তৈরি Android Studio project।
Package name: com.realityshift.app
Firebase project: reality-shift-f9bd1

প্রজেক্টে google-services.json ইতিমধ্যে app/ ফোল্ডারে রাখা হয়েছে।

এই ভার্সনে:
- Native Android UI
- Home, Explore, Challenges, Ranking, Profile
- Brain Blitz trivia quiz
- Code Breaker number puzzle
- XP, level, wins, games (এই build-এর runtime state)
- Firebase anonymous authentication চেষ্টা
- Email/password account create/sign-in
- Firestore player profile write

Android ফোনে সরাসরি ZIP extract করে APK বানানো সাধারণত সহজ নয়; Android Studio বা cloud build প্রয়োজন।

Cloud build (কম্পিউটার ছাড়াও GitHub-এর মাধ্যমে):
1. ZIP extract করো।
2. GitHub-এ নতুন repository তৈরি করো (যেমন reality-shift-android)।
3. Extract করা project-এর সব ফাইল repository-তে upload করো; .github ফোল্ডারটিও upload করতে হবে।
4. GitHub repository > Actions > Build REALITY SHIFT APK > Run workflow।
5. Build সফল হলে workflow run খুলে নিচে Artifacts থেকে REALITY-SHIFT-debug-APK ডাউনলোড করো।
6. ZIP artifact extract করে app-debug.apk ফোনে ইনস্টল করো। ফোনে “Install unknown apps” অনুমতি চাইতে পারে।

Firebase Console-এ আরও যা করতে হবে:
- Authentication > Sign-in method > Email/Password enable করো।
- Authentication > Sign-in method > Anonymous enable করো (guest session-এর জন্য)।
- Firestore Database তৈরি করো।
- Firestore Rules-এ এই project-এর firestore.rules ফাইলের rules publish করো।

নিরাপত্তা/সীমাবদ্ধতা:
- এই project একটি প্রথম native prototype, production release নয়।
- Firestore rules ব্যবহারকারীর নিজের profile document-এ write সীমিত করে। কিন্তু XP client app থেকে পাঠানো হয়; তাই এটিকে প্রতিযোগিতার নিরাপদ score হিসেবে বিশ্বাস করা যাবে না।
- সত্যিকারের global leaderboard, anti-cheat, daily reset, live multiplayer, user-generated content moderation, push notifications, signed release keystore এবং Play Store release এখনও যোগ করা হয়নি।
- Production leaderboard-এর জন্য trusted backend/Cloud Functions দিয়ে score যাচাই করতে হবে।
- google-services.json-এ Firebase client configuration থাকে; Firebase Security Rules ও Auth সঠিকভাবে configure করা জরুরি। কখনো service-account private key অ্যাপে রাখবে না।
