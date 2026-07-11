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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun AstroRobotAvatar(state: AvatarState, modifier: Modifier = Modifier) {
    if (state == AvatarState.HIDDEN) return
    val transition = rememberInfiniteTransition(label = "astro")

    val blink by transition.animateFloat(1f, 1f, infiniteRepeatable(keyframes {
        durationMillis = 3000; 1f at 0; 1f at 2700; 0.1f at 2850; 1f at 3000
    }), label = "blink")
    val pulse by transition.animateFloat(0.95f, 1.12f, infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val antennaBlink by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "antenna")
    val mouthOpen by transition.animateFloat(0.15f, 1f, infiniteRepeatable(tween(220), RepeatMode.Reverse), label = "mouth")

    Box(modifier = modifier.size(165.dp)) {
        Canvas(modifier = Modifier.size(165.dp)) {
            val scale = if (state == AvatarState.LISTENING) pulse else 1f
            val center = Offset(size.width / 2, size.height / 2)
            val unit = (size.minDimension / 2.3f) * scale

            if (state == AvatarState.LISTENING) {
                drawCircle(Color(0xFF00C2CB).copy(alpha = 0.2f), unit * 1.35f, center)
            }

            drawLine(Color(0xFF607D8B), Offset(center.x, center.y - unit * 1.1f), Offset(center.x, center.y - unit * 0.75f), strokeWidth = unit * 0.06f)
            drawCircle(Color(0xFFFF5252).copy(alpha = antennaBlink), unit * 0.1f, Offset(center.x, center.y - unit * 1.15f))

            drawRoundRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(center.x - unit * 0.75f, center.y - unit * 0.7f),
                size = Size(unit * 1.5f, unit * 1.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(unit * 0.3f)
            )
            drawRoundRect(
                color = Color(0xFF263238),
                topLeft = Offset(center.x - unit * 0.6f, center.y - unit * 0.5f),
                size = Size(unit * 1.2f, unit * 1.0f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(unit * 0.2f)
            )

            val eyeR = unit * 0.14f
            listOf(-1f, 1f).forEach { side ->
                drawOval(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(center.x + side * unit * 0.3f - eyeR, center.y - unit * 0.15f - eyeR * blink),
                    size = Size(eyeR * 2, eyeR * 2 * blink)
                )
            }
            val mouthW = unit * 0.5f
            val mouthH = unit * 0.15f * mouthOpen
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(center.x - mouthW / 2, center.y + unit * 0.2f - mouthH / 2),
                size = Size(mouthW, mouthH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(mouthH / 2)
            )

            listOf(-1f, 1f).forEach { side ->
                drawRoundRect(
                    color = Color(0xFFB0BEC5),
                    topLeft = Offset(center.x + side * unit * 0.85f - unit * 0.08f, center.y - unit * 0.25f),
                    size = Size(unit * 0.16f, unit * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(unit * 0.08f)
                )
            }
        }
    }
}

@Composable
fun CartoonCatAvatar(state: AvatarState, modifier: Modifier = Modifier) {
    if (state == AvatarState.HIDDEN) return
    val transition = rememberInfiniteTransition(label = "cat")

    val blink by transition.animateFloat(1f, 1f, infiniteRepeatable(keyframes {
        durationMillis = 2800; 1f at 0; 1f at 2500; 0.1f at 2650; 1f at 2800
    }), label = "blink")
    val pulse by transition.animateFloat(0.95f, 1.1f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val earTwitch by transition.animateFloat(-0.05f, 0.05f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "ear")
    val mouthOpen by transition.animateFloat(0.15f, 0.8f, infiniteRepeatable(tween(240), RepeatMode.Reverse), label = "mouth")

    Box(modifier = modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val scale = if (state == AvatarState.LISTENING) pulse else 1f
            val center = Offset(size.width / 2, size.height / 2)
            val r = (size.minDimension / 2.4f) * scale

            if (state == AvatarState.LISTENING) drawCircle(Color(0xFFFFA07A).copy(alpha = 0.25f), r * 1.3f, center)

            listOf(-1f, 1f).forEach { side ->
                val earPath = Path().apply {
                    val baseX =
