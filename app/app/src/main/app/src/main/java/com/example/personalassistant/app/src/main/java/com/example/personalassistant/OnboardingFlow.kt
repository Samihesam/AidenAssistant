package com.example.personalassistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class OnboardingStep { WELCOME, PERMISSIONS, VOICE_SELECTION, AVATAR_SELECTION, DONE }

@Composable
fun OnboardingFlow(
    ttsManager: AndroidTtsManager,
    permissionList: List<PermissionItem>,
    onPermissionsAccepted: () -> Unit,
    onComplete: (selectedVoice: AndroidTtsManager.VoiceProfile?, selectedAvatar: AvatarChoice) -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var selectedVoice by remember { mutableStateOf<AndroidTtsManager.VoiceProfile?>(null) }
    var selectedAvatar by remember { mutableStateOf(AvatarChoice.WIZARD) }

    LaunchedEffect(step) {
        if (step == OnboardingStep.WELCOME) {
            ttsManager.speak(
                "سلام! من ایدن هستم، دستیار شخصی تو. خیلی خوشحالم که می‌تونم کمکت کنم. " +
                "برای اینکه بهترین تجربه رو داشته باشیم، اول باید چندتا دسترسی ازت بخوام، " +
                "بعد صدا و شکل ظاهریم رو با هم انتخاب می‌کنیم."
            )
        }
    }

    when (step) {
        OnboardingStep.WELCOME -> WelcomeScreen(
            onContinue = { step = OnboardingStep.PERMISSIONS }
        )
        OnboardingStep.PERMISSIONS -> ConsentScreen(
            permissions = permissionList,
            onAccept = {
                onPermissionsAccepted()
                step = OnboardingStep.VOICE_SELECTION
            },
            onDecline = { step = OnboardingStep.VOICE_SELECTION }
        )
        OnboardingStep.VOICE_SELECTION -> VoiceSelectionScreen(
            ttsManager = ttsManager,
            onVoiceSelected = { voice ->
                selectedVoice = voice
                step = OnboardingStep.AVATAR_SELECTION
            }
        )
        OnboardingStep.AVATAR_SELECTION -> AvatarSelectionScreen(
            initialSelection = selectedAvatar,
            onAvatarSelected = { avatar ->
                selectedAvatar = avatar
                step = OnboardingStep.DONE
                onComplete(selectedVoice, avatar)
            }
        )
        OnboardingStep.DONE -> { }
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WizardAvatar(state = AvatarState.SPEAKING)
        Spacer(Modifier.height(24.dp))
        Text("سلام! من ایدن هستم 👋", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "دستیار شخصی تو. خوشحالم که می‌تونم کمکت کنم. بیا اول چندتا تنظیم ساده رو باهم انجام بدیم.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("بزن بریم")
        }
    }
}

@Composable
private fun VoiceSelectionScreen(
    ttsManager: AndroidTtsManager,
    onVoiceSelected: (AndroidTtsManager.VoiceProfile?) -> Unit
) {
    var selected by remember { mutableStateOf<AndroidTtsManager.VoiceProfile?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("صدای ایدن رو انتخاب کن", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("با لمس هر گزینه، یه نمونه صدا پخش می‌شه", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(ttsManager.availableVoices) { voice ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = {
                        selected = voice
                        ttsManager.speak("سلام، این صدای منه. امیدوارم خوشت بیاد!", voice)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(voice.label)
                        if (selected == voice) Text("✓ انتخاب شد")
                    }
                }
            }
        }

        Button(
            onClick = { onVoiceSelected(selected) },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ادامه")
        }
    }
}

enum class AvatarChoice(val label: String) {
    WIZARD("جادوگر پیر (پیش‌فرض)"),
    ROBOT("ربات کوچک"),
    CUTE_FACE("صورت زرد ناز"),
    ASTRO("آسترو (ربات فضایی)"),
    CAT("گربه‌ی کارتونی"),
    GHOST("روح کوچولوی ناز")
}

@Composable
private fun AvatarSelectionScreen(
    initialSelection: AvatarChoice,
    onAvatarSelected: (AvatarChoice) -> Unit
) {
    var selected by remember { mutableStateOf(initialSelection) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("شکل ظاهری ایدن رو انتخاب کن", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        when (selected) {
