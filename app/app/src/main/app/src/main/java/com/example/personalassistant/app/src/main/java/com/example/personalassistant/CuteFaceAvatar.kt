package com.example.personalassistant

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun CuteFaceAvatar(
    state: AvatarState,
    modifier: Modifier = Modifier,
    faceColor: Color = Color(0xFFFFD93D),
    eyeColor: Color = Color(0xFF1A1A1A)
) {
    if (state == AvatarState.HIDDEN) return

    val infiniteTransition = rememberInfiniteTransition(label = "cuteface")

    val blink by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2600
                1f at 0; 1f at 2350; 0.08f at 2480; 1f at 2600
            }
        ), label = "blink"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val mouthOpen by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "mouth"
    )

    Box(modifier = modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val scale = if (state == AvatarState.LISTENING) pulse else 1f
            drawCuteFace(
                bodyScale = scale,
                eyeScaleY = if (state == AvatarState.LISTENING) 1f else blink,
                mouthOpenAmount = if (state == AvatarState.SPEAKING) mouthOpen else 0.2f,
                faceColor = faceColor,
                eyeColor = eyeColor,
                showListeningRing = state == AvatarState.LISTENING
            )
        }
    }
}

private fun DrawScope.drawCuteFace(
    bodyScale: Float,
    eyeScaleY: Float,
    mouthOpenAmount: Float,
    faceColor: Color,
    eyeColor: Color,
    showListeningRing: Boolean
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension / 2.3f) * bodyScale

    if (showListeningRing) {
        drawCircle(color = faceColor.copy(alpha = 0.25f), radius = radius * 1.3f, center = center)
    }

    drawCircle(color = faceColor, radius = radius, center = center)

    val cheekRadius = radius * 0.15f
    val cheekColor = Color(0xFFFF8FA3).copy(alpha = 0.5f)
    drawCircle(color = cheekColor, radius = cheekRadius, center = Offset(center.x - radius * 0.55f, center.y + radius * 0.15f))
    drawCircle(color = cheekColor, radius = cheekRadius, center = Offset(center.x + radius * 0.55f, center.y + radius * 0.15f))

    val eyeOffsetX = radius * 0.38f
    val eyeY = center.y - radius * 0.1f
    val eyeRadius = radius * 0.26f
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            color = eyeColor,
            topLeft = Offset(
                center.x + side * eyeOffsetX - eyeRadius,
                eyeY - eyeRadius * eyeScaleY
            ),
            size = Size(eyeRadius * 2, eyeRadius * 2 * eyeScaleY)
        )
        if (eyeScaleY > 0.3f) {
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.25f,
                center = Offset(
                    center.x + side * eyeOffsetX + eyeRadius * 0.3f,
                    eyeY - eyeRadius * 0.4f
                )
            )
        }
    }

    val mouthWidth = radius * 0.55f
    val mouthHeight = radius * 0.45f * mouthOpenAmount
    drawOval(
        color = Color(0xFF8B4A3D),
        topLeft = Offset(center.x - mouthWidth / 2, center.y + radius * 0.42f - mouthHeight / 2),
        size = Size(mouthWidth, mouthHeight)
    )
}
