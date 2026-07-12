package com.example.personalassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: AndroidTtsManager
    private val prefs by lazy { getSharedPreferences("aiden_prefs", Context.MODE_PRIVATE) }

    private val roleRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val multiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsManager = AndroidTtsManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AidenApp(
                        activity = this,
                        ttsManager = ttsManager,
                        prefs = prefs,
                        roleRequestLauncher = roleRequestLauncher,
                        multiplePermissionsLauncher = multiplePermissionsLauncher
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}

@Composable
fun AidenApp(
    activity: MainActivity,
    ttsManager: AndroidTtsManager,
    prefs: android.content.SharedPreferences,
    roleRequestLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    multiplePermissionsLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    var termsAccepted by remember { mutableStateOf(prefs.getBoolean("terms_accepted", false)) }
    var onboardingDone by remember { mutableStateOf(prefs.getBoolean("onboarding_done", false)) }
    var selectedAvatar by remember {
        mutableStateOf(
            AvatarChoice.valueOf(prefs.getString("selected_avatar", AvatarChoice.WIZARD.name)!!)
        )
    }
    var selectedVoiceLabel by remember { mutableStateOf(prefs.getString("selected_voice", null)) }

    if (!termsAccepted) {
        TermsOfServiceScreen(
            onAccepted = {
                prefs.edit().putBoolean("terms_accepted", true).apply()
                termsAccepted = true
            }
        )
    } else if (!onboardingDone) {
        OnboardingFlow(
            ttsManager = ttsManager,
            permissionList = buildPermissionList(),
            onPermissionsAccepted = {
                val permsToRequest = buildPermissionList().map { it.permission }.toTypedArray()
                multiplePermissionsLauncher.launch(permsToRequest)
            },
            onComplete = { voice, avatar ->
                selectedAvatar = avatar
                selectedVoiceLabel = voice?.label
                prefs.edit()
                    .putBoolean("onboarding_done", true)
                    .putString("selected_avatar", avatar.name)
                    .putString("selected_voice", voice?.label)
                    .apply()
                onboardingDone = true
            }
        )
    } else {
        HomeShell(
            activity = activity,
            ttsManager = ttsManager,
            prefs = prefs,
            selectedAvatar = selectedAvatar,
            onAvatarChanged = {
                selectedAvatar = it
                prefs.edit().putString("selected_avatar", it.name).apply()
            },
            roleRequestLauncher = roleRequestLauncher
        )
    }
}

enum class AidenTab { HOME, RADAR, SETTINGS }

@Composable
fun HomeShell(
    activity: MainActivity,
    ttsManager: AndroidTtsManager,
    prefs: android.content.SharedPreferences,
    selectedAvatar: AvatarChoice,
    onAvatarChanged: (AvatarChoice) -> Unit,
    roleRequestLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    var currentTab by remember { mutableStateOf(AidenTab.HOME) }
    var avatarState by remember { mutableStateOf(AvatarState.IDLE) }
    val idleAnimation = rememberIdleBehavior(currentState = avatarState)

    var assistantName by remember { mutableStateOf(PersonalizationManager.getAssistantName(activity)) }

    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val text = intent?.getStringExtra("recognized_text") ?: return
                avatarState = AvatarState.LISTENING

                when (val command = VoiceCommandParser.parse(text)) {
                    is VoiceCommandParser.AssistantCommand.ChangeAvatarVisibility -> {
                        avatarState = if (command.show) AvatarState.IDLE else AvatarState.HIDDEN
                        return
                    }
                    is VoiceCommandParser.AssistantCommand.OpenApp -> {
                        val opened = AppLauncher.openAppByName(activity, command.appName)
                        if (!opened) {
                            ttsManager.speak("اپی به اسم ${command.appName} پیدا نکردم")
                        }
                        return
                    }
                    else -> { }
                }

                when (val personalCommand = PersonalizationCommandParser.parse(text)) {
                    is PersonalizationCommandParser.PersonalizationCommand.RenameAssistant -> {
                        PersonalizationManager.setAssistantName(activity, personalCommand.newName)
                        assistantName = personalCommand.newName
                        ttsManager.speak("باشه، از این به بعد اسمم ${personalCommand.newName} هست")
                    }
                    is PersonalizationCommandParser.PersonalizationCommand.RememberFact -> {
                        PersonalizationManager.saveMemory(activity, personalCommand.key, personalCommand.value)
                        ttsManager.speak("باشه، یادم موند")
                    }
                    is PersonalizationCommandParser.PersonalizationCommand.RecallFact -> {
                        val memory = PersonalizationManager.findMemory(activity, personalCommand.query)
                        if (memory != null) {
                            ttsManager.speak(memory.value)
                        }
                    }
                    is PersonalizationCommandParser.PersonalizationCommand.ForgetFact -> {
                        PersonalizationManager.deleteMemory(activity, personalCommand.key)
                        ttsManager.speak("باشه، فراموشش کردم")
                    }
                    else -> { }
                }
            }
        }
        val filter = android.content.IntentFilter(AlwaysListeningService.ACTION_WAKE_WORD_DETECTED)
        ContextCompat.registerReceiver(activity, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { activity.unregisterReceiver(receiver) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == AidenTab.HOME,
                    onClick = { currentTab = AidenTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "خانه") },
                    label = { Text("خانه") }
                )
                NavigationBarItem(
                    selected = currentTab == AidenTab.RADAR,
                    onClick = { currentTab = AidenTab.RADAR },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "رادار") },
                    label = { Text("رادار") }
                )
                NavigationBarItem(
                    selected = currentTab == AidenTab.SETTINGS,
                    onClick = { currentTab = AidenTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "تنظیمات") },
                    label = { Text("تنظیمات") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                AidenTab.HOME -> HomeScreen(
                    selectedAvatar = selectedAvatar,
                    avatarState = avatarState,
                    idleAnimation = idleAnimation,
                    assistantName = assistantName
                )
                AidenTab.RADAR -> {
                    val savedToken = prefs.getString("auth_token", null)
                    if (savedToken != null) {
                        RadarScreen(authToken = savedToken)
                    } else {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("برای استفاده از رادار، اول باید وارد حساب کاربری بشی")
                        }
                    }
                }
                AidenTab.SETTINGS -> SettingsScreen(
                    activity = activity,
                    selectedAvatar = selectedAvatar,
                    onAvatarChanged = onAvatarChanged,
                    roleRequestLauncher = roleRequestLauncher
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    selectedAvatar: AvatarChoice,
    avatarState: AvatarState,
    idleAnimation: IdleAnimation,
    assistantName: String
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (selectedAvatar) {
            AvatarChoice.WIZARD -> WizardAvatar(state = avatarState, idleAnimation = idleAnimation)
            AvatarChoice.ROBOT -> AssistantAvatar(state = avatarState)
            AvatarChoice.CUTE_FACE -> CuteFaceAvatar(state = avatarState)
            AvatarChoice.ASTRO -> AstroRobotAvatar(state = avatarState)
            AvatarChoice.CAT -> CartoonCatAvatar(state = avatarState)
            AvatarChoice.GHOST -> GhostBuddyAvatar(state = avatarState)
        }
        Spacer(Modifier.height(16.dp))
        Text(assistantName, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            when (avatarState) {
                AvatarState.LISTENING -> "در حال گوش دادن..."
                AvatarState.SPEAKING -> "در حال صحبت..."
                else -> "بگو «دستیار» تا فعال بشم"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SettingsScreen(
    activity: MainActivity,
    selectedAvatar: AvatarChoice,
    onAvatarChanged: (AvatarChoice) -> Unit,
    roleRequestLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        SettingsSection("آواتار") {
            AvatarChoice.entries.forEach { choice ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onAvatarChanged(choice) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(choice.label)
                        if (selectedAvatar == choice) Text("✓")
                    }
                }
            }
        }

        SettingsSection("دسترسی‌های ویژه") {
            SettingsButton("اپ پیش‌فرض پیامک") {
                DefaultAppManager.requestDefaultSmsApp(activity, roleRequestLauncher)
            }
            SettingsButton("اپ پیش‌فرض تلفن") {
                DefaultAppManager.requestDefaultDialerApp(activity, roleRequestLauncher)
            }
            SettingsButton("نمایش روی سایر اپ‌ها (حباب شناور)") {
                if (!OverlayPermissionHelper.hasOverlayPermission(activity)) {
                    OverlayPermissionHelper.requestOverlayPermission(activity)
                } else {
                    activity.startService(Intent(activity, FloatingBubbleService::class.java))
                }
            }
            SettingsButton("دسترسی‌پذیری (برای تعامل با سایر اپ‌ها)") {
                activity.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        SettingsSection("زبان") {
            var expanded by remember { mutableStateOf(false) }
            var selectedLang by remember { mutableStateOf(LanguageManager.supportedLanguages.first()) }
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedLang.label)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    LanguageManager.supportedLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.label) },
                            onClick = { selectedLang = lang; expanded = false }
                        )
                    }
                }
            }
        }

        SettingsSection("رادار") {
            Text(
                "با فعال‌سازی رادار، افراد نزدیک (بالای ۱۸ سال) می‌تونن ببیننت و درخواست چت بفرستن.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    content()
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun SettingsButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label)
    }
}
