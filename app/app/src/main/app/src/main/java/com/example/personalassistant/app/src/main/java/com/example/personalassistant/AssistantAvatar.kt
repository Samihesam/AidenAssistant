package com.example.personalassistant

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

enum class AvatarState { IDLE, LISTENING, SPEAKING, HIDDEN }

@Composable
fun AssistantAvatar(
    state: AvatarState,
    modifier: Modifier = Modifier,
    faceColor: Color = Color(0xFF6C63FF)
) {
    if (state == AvatarState.HIDDEN) return

    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    val blink by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1f at 0; 1f at 2700; 0.1f at 2850; 1f at 3000
            }
        ), label = "blink"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val mouthOpen by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "mouth"
    )

    Box(modifier = modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val scale = if (state == AvatarState.LISTENING) pulse else 1f
            drawRobotFace(
                eyeScaleY = if (state == AvatarState.LISTENING) 1f else blink,
                mouthOpenAmount = if (state == AvatarState.SPEAKING) mouthOpen else 0.15f,
                bodyScale = scale,
                color = faceColor,
                showListeningRing = state == AvatarState.LISTENING
            )
        }
    }
}

private fun DrawScope.drawRobotFace(
    eyeScaleY: Float,
    mouthOpenAmount: Float,
    bodyScale: Float,
    color: Color,
    showListeningRing: Boolean
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension / 2.5f) * bodyScale

    if (showListeningRing) {
        drawCircle(
            color = color.copy(alpha = 0.25f),
            radius = radius * 1.35f,
            center = center
        )
    }

    drawCircle(color = color, radius = radius, center = center)

    val eyeOffsetX = radius * 0.4f
    val eyeY = center.y - radius * 0.15f
    val eyeRadius = radius * 0.15f
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            color = Color.White,
            topLeft = Offset(
                center.x + side * eyeOffsetX - eyeRadius,
                eyeY - eyeRadius * eyeScaleY
            ),
            size = androidx.compose.ui.geometry.Size(eyeRadius * 2, eyeRadius * 2 * eyeScaleY)
        )
    }

    val mouthWidth = radius * 0.7f
    val mouthHeight = radius * 0.5f * mouthOpenAmount
    val mouthY = center.y + radius * 0.4f
    drawOval(
        color = Color.White,
        topLeft = Offset(center.x - mouthWidth / 2, mouthY - mouthHeight / 2),
        size = androidx.compose.ui.geometry.Size(mouthWidth, mouthHeight)
    )
}
