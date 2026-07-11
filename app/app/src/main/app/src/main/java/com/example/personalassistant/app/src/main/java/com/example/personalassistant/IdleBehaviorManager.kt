package com.example.personalassistant

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

enum class IdleAnimation { NONE, FUNNY_FACE, SITTING, PIGEON_TRICK }

@Composable
fun rememberIdleBehavior(
    currentState: AvatarState,
    idleTimeoutMs: Long = 30_000
): IdleAnimation {
    var idleAnimation by remember { mutableStateOf(IdleAnimation.NONE) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(currentState) {
        if (currentState == AvatarState.LISTENING || currentState == AvatarState.SPEAKING) {
            lastInteractionTime = System.currentTimeMillis()
            idleAnimation = IdleAnimation.NONE
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val silentDuration = System.currentTimeMillis() - lastInteractionTime
            if (currentState == AvatarState.IDLE && silentDuration > idleTimeoutMs) {
                idleAnimation = listOf(
                    IdleAnimation.FUNNY_FACE,
                    IdleAnimation.SITTING,
                    IdleAnimation.PIGEON_TRICK
                ).random()
                delay(6000)
                idleAnimation = IdleAnimation.NONE
                lastInteractionTime = System.currentTimeMillis()
            }
        }
    }

    return idleAnimation
}
