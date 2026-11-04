package com.realityshift.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

private val Bg = Color(0xFF080817)
private val Panel = Color(0xFF15152D)
private val Purple = Color(0xFF913CFF)
private val Blue = Color(0xFF315DFF)
private val Muted = Color(0xFFAAA8C7)
private val White = Color(0xFFF8F6FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RealityShiftApp() }
    }
}

@Composable
fun RealityShiftApp() {
    var tab by remember { mutableStateOf("Home") }
    var xp by remember { mutableIntStateOf(0) }
    var wins by remember { mutableIntStateOf(0) }
    var games by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Connecting to Firebase…") }
    var showQuiz by remember { mutableStateOf(false) }
    var showPuzzle by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }

    val auth = remember { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
    val db = remember { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }

    LaunchedEffect(Unit) {
        if (auth == null || db == null) {
            message = "Firebase setup needs checking"
        } else {
            if (auth.currentUser == null) {
                auth.signInAnonymously()
                    .addOnSuccessListener { message = "Connected to Firebase (guest player)" }
                    .addOnFailureListener { message = "Firebase sign-in failed: ${it.localizedMessage}" }
            } else message = "Connected to Firebase"
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple, secondary = Color(0xFF39D8FF),
            background = Bg, surface = Panel, onBackground = White, onSurface = White
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                Header(onProfile = { tab = "Profile" })
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        "Home" -> HomeScreen(xp, wins, onQuiz = { showQuiz = true }, onPuzzle = { showPuzzle = true }, onChallenges = { tab = "Challenges" }, onGames = { tab = "Games" })
                        "Explore" -> ExploreScreen(onQuiz = { showQuiz = true }, onPuzzle = { showPuzzle = true })
                        "Challenges" -> ChallengesScreen(onQuiz = { showQuiz = true }, onPuzzle = { showPuzzle = true })
                        "Ranking" -> RankingScreen(xp)
                        else -> ProfileScreen(xp, wins, games, message, onAccount = { showAccount = true })
                    }
                }
                NavigationBar(containerColor = Color(0xFF101025), contentColor = White) {
                    listOf("Home", "Explore", "Challenges", "Ranking", "Profile").forEach { item ->
                        val icon = when (item) {
                            "Home" -> Icons.Default.Home
                            "Explore" -> Icons.Default.Explore
                            "Challenges" -> Icons.Default.EmojiEvents
                            "Ranking" -> Icons.Default.Leaderboard
                            else -> Icons.Default.Person
                        }
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFC69AFF),
                                selectedTextColor = Color(0xFFC69AFF),
                                indicatorColor = Color(0xFF30204D),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        }

        if (showQuiz) {
            QuizDialog(
                onDismiss = { showQuiz = false },
                onReward = { earned ->
                    xp += earned; games++
                    if (earned >= 30) wins++
                    syncPlayer(db, auth, xp, wins, games)
                }
            )
        }
        if (showPuzzle) {
            PuzzleDialog(
                onDismiss = { showPuzzle = false },
                onWin = {
                    xp += 40; wins++; games++
                    syncPlayer(db, auth, xp, wins, games)
                },
                onLose = { games++; syncPlayer(db, auth, xp, wins, games) }
            )
        }
        if (showAccount) {
            AccountDialog(auth = auth, onDismiss = { showAccount = false }, onMessage = { message = it })
        }
    }
}

private fun syncPlayer(db: FirebaseFirestore?, auth: FirebaseAuth?, xp: Int, wins: Int, games: Int) {
    val user = auth?.currentUser ?: return
    db?.collection("players")?.document(user.uid)?.set(
        mapOf("xp" to xp, "wins" to wins, "games" to games, "updatedAt" to System.currentTimeMillis()),
        com.google.firebase.firestore.SetOptions.merge()
    )
}

@Composable
private fun Header(onProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Bg).padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("REALITY SHIFT", fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, color = White)
            Text("ENTER A WORLD. CHANGE THE STORY.", fontSize = 9.sp, color = Muted, letterSpacing = 1.2.sp)
        }
        Box(
            Modifier.size(40.dp).background(Brush.linearGradient(listOf(Purple, Blue)), RoundedCornerShape(14.dp))
                .clickable { onProfile() },
            contentAlignment = Alignment.Center
        ) { Text("RS", fontWeight = FontWeight.Black, color = White) }
    }
}

@Composable
private fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(Bg).padding(horizontal = 16.dp).padding(bottom = 12.dp), content = content)
}

@Composable
private fun Eyebrow(text: String) {
    Text(text.uppercase(), color = Color(0xFFB99AFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
}

@Composable
private fun Hero(title: String, subtitle: String, button: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(Color(0xFF40117D), Color(0xFF1D347D), Color(0xFF102E4B))),
            RoundedCornerShape(24.dp)
        ).padding(20.dp)
    ) {
        Text("✦ GLOBAL PLAY UNIVERSE", color = Color(0xFFE1D4FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(title, color = White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
        Text(subtitle, color = Color(0xFFE0DAF5), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text(button, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun Stats(xp: Int, wins: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatBox("XP EARNED", xp.toString(), Modifier.weight(1f))
        StatBox("LEVEL", (xp / 100 + 1).toString(), Modifier.weight(1f))
        StatBox("WINS", wins.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(15.dp)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
        Text(label, fontSize = 9.sp, color = Muted)
    }
}

@Composable
private fun GameCard(emoji: String, title: String, desc: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp))
            .clickable { onClick() }.padding(13.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(76.dp).background(
            Brush.linearGradient(listOf(Color(0xFF6940B7), Color(0xFF1D2E70))),
            RoundedCornerShape(13.dp)
        ), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 36.sp) }
        Text(title, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 9.dp))
        Text(desc, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun HomeScreen(xp: Int, wins: Int, onQuiz: () -> Unit, onPuzzle: () -> Unit, onChallenges: () -> Unit, onGames: () -> Unit) {
    Page {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 5.dp, bottom = 20.dp)) {
            item { Hero("Ready to shift\\nyour reality?", "Play quick games, take on global challenges and climb the world ranking.", "Explore challenges", onChallenges) }
            item { Stats(xp, wins) }
            item { Text("FEATURED GAMES", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) { GameCard("🧠", "Brain Blitz", "Fast global trivia", onQuiz) }
                    Box(Modifier.weight(1f)) { GameCard("🔐", "Code Breaker", "Crack the number", onPuzzle) }
                }
            }
            item { Text("TODAY'S MISSION", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            item {
                Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(17.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 30.sp); Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Answer 3 trivia questions", color = White, fontWeight = FontWeight.Bold); Text("Earn bonus XP today", color = Muted, fontSize = 11.sp) }
                    TextButton(onClick = onQuiz) { Text("PLAY") }
                }
            }
            item { TextButton(onClick = onGames) { Text("Browse all games →") } }
        }
    }
}

@Composable
private fun ExploreScreen(onQuiz: () -> Unit, onPuzzle: () -> Unit) {
    Page {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 10.dp)) {
            item { Eyebrow("DISCOVER"); Text("Explore the universe", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Choose a world and find your next challenge.", color = Muted) }
            item { GameCard("🧠", "Knowledge Zone", "Trivia and quick thinking", onQuiz) }
            item { GameCard("🧩", "Mystery Zone", "Puzzles and logic", onPuzzle) }
            item { GameCard("🌐", "Global Arena", "Worldwide missions", onQuiz) }
            item { Text("New worlds and community-created challenges will arrive in future updates.", color = Muted, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun ChallengesScreen(onQuiz: () -> Unit, onPuzzle: () -> Unit) {
    Page {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 10.dp)) {
            item { Eyebrow("COMPETE TOGETHER"); Text("Global challenges", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Complete missions to earn XP.", color = Muted) }
            item { GameCard("🧠", "Trivia Explorer", "Answer quiz questions • +30 XP", onQuiz) }
            item { GameCard("🔢", "Code Breaker", "Solve the secret number • +40 XP", onPuzzle) }
            item { GameCard("🌟", "Daily Explorer", "Play both games to build your streak", onQuiz) }
            item { Text("Daily mission progress is currently stored in this app prototype. Shared global challenges will need additional backend logic.", color = Muted, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun RankingScreen(xp: Int) {
    Page {
        LazyColumn(contentPadding = PaddingValues(top = 10.dp)) {
            item { Eyebrow("GLOBAL COMPETITION"); Text("Leaderboard", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text("Sample standings for the prototype.", color = Muted) }
            val rows = listOf("🥇  NovaRider" to "12,500 XP", "🥈  PixelWizard" to "10,800 XP", "🥉  StarDreamer" to "9,700 XP", "4  GameLegend" to "8,900 XP", "5  You" to "$xp XP")
            items(rows.size) { i ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Panel, RoundedCornerShape(13.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(rows[i].first, color = White); Text(rows[i].second, color = Color(0xFFC69AFF), fontWeight = FontWeight.Bold)
                }
            }
            item { Text("Real competitive rankings require server-verified scores. Sample players are not real accounts.", color = Muted, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun ProfileScreen(xp: Int, wins: Int, games: Int, status: String, onAccount: () -> Unit) {
    Page {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.padding(top = 12.dp)) {
            Eyebrow("PLAYER PROFILE")
            Text("Your profile", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).background(Brush.linearGradient(listOf(Purple, Blue)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text("RS", fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(12.dp))
                Column { Text("Guest Player", color = White, fontWeight = FontWeight.Bold); Text("LEVEL ${xp / 100 + 1}", color = Color(0xFFC69AFF), fontSize = 11.sp) }
            }
            Stats(xp, wins)
            Text("Games played: $games", color = Muted)
            Button(onClick = onAccount, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Account options") }
            Text(status, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuizDialog(onDismiss: () -> Unit, onReward: (Int) -> Unit) {
    val qs = remember { listOf(
        Triple("Which planet is known as the Red Planet?", listOf("Venus", "Mars", "Jupiter", "Mercury"), 1),
        Triple("How many sides does a hexagon have?", listOf("5", "6", "7", "8"), 1),
        Triple("Which ocean is the largest?", listOf("Atlantic", "Indian", "Pacific", "Arctic"), 2),
        Triple("What is the chemical formula for water?", listOf("CO2", "O2", "H2O", "NaCl"), 2),
        Triple("Which is the largest living mammal?", listOf("Elephant", "Blue whale", "Giraffe", "Hippo"), 1)
    ) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = { Text(if (finished) "Quiz complete!" else "Brain Blitz • ${index + 1}/5", color = White) },
        text = {
            Column {
                if (finished) {
                    Text("You scored $score out of 5.", color = White)
                    Text("+${score * 10} XP", color = Color(0xFFC69AFF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(qs[index].first, color = White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    qs[index].second.forEachIndexed { i, answer ->
                        OutlinedButton(
                            onClick = {
                                if (i == qs[index].third) score++
                                if (index == 4) {
                                    finished = true
                                    onReward(score * 10)
                                } else index++
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) { Text(answer, color = White) }
                    }
                }
            }
        },
        confirmButton = { if (finished) TextButton(onClick = onDismiss) { Text("Done") } },
        dismissButton = { if (!finished) TextButton(onClick = onDismiss) { Text("Exit") } }
    )
}

@Composable
private fun PuzzleDialog(onDismiss: () -> Unit, onWin: () -> Unit, onLose: () -> Unit) {
    val secret = remember { Random.nextInt(1, 21) }
    var guess by remember { mutableStateOf("") }
    var tries by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf("Guess the secret number from 1 to 20. You have 5 tries.") }
    var done by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Panel,
        title = { Text("Code Breaker", color = White) },
        text = {
            Column {
                Text(result, color = White)
                if (!done) {
                    OutlinedTextField(value = guess, onValueChange = { guess = it.filter(Char::isDigit).take(2) }, label = { Text("Your guess") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val n = guess.toIntOrNull()
                        if (n == null || n !in 1..20) result = "Enter a number from 1 to 20."
                        else {
                            tries++
                            when {
                                n == secret -> { result = "Correct! You earned 40 XP."; done = true; onWin() }
                                tries >= 5 -> { result = "Out of tries. The number was $secret."; done = true; onLose() }
                                n < secret -> result = "Too low. Try higher! ($tries/5)"
                                else -> result = "Too high. Try lower! ($tries/5)"
                            }
                            guess = ""
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Submit guess") }
                }
            }
        },
        confirmButton = { if (done) TextButton(onClick = onDismiss) { Text("Done") } },
        dismissButton = { if (!done) TextButton(onClick = onDismiss) { Text("Exit") } }
    )
}

@Composable
private fun AccountDialog(auth: FirebaseAuth?, onDismiss: () -> Unit, onMessage: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Panel,
        title = { Text("Account options", color = White) },
        text = {
            Column {
                Text("Create an email account or sign in to this device.", color = Muted, fontSize = 12.sp)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(password, { password = it }, label = { Text("Password (6+ characters)") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (auth == null) onMessage("Firebase Auth is not available.")
                    else auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onMessage("Account created and signed in.") }
                        .addOnFailureListener { onMessage(it.localizedMessage ?: "Account creation failed.") }
                }, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
                OutlinedButton(onClick = {
                    if (auth == null) onMessage("Firebase Auth is not available.")
                    else auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onMessage("Signed in successfully.") }
                        .addOnFailureListener { onMessage(it.localizedMessage ?: "Sign in failed.") }
                }, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
                TextButton(onClick = { auth?.signOut(); auth?.signInAnonymously(); onMessage("Signed out; guest session requested.") }) { Text("Continue as guest") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
