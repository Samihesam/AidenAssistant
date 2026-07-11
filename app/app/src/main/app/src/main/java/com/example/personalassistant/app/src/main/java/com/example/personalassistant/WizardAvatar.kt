package com.example.personalassistant

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WizardAvatar(
    state: AvatarState,
    modifier: Modifier = Modifier,
    idleAnimation: IdleAnimation = IdleAnimation.NONE,
    robeColor: Color = Color(0xFF2C4A9E),
    starColor: Color = Color(0xFFFFD700),
    skinColor: Color = Color(0xFFE8C2A0),
    beardColor: Color = Color(0xFFF5F5F5)
) {
    if (state == AvatarState.HIDDEN) return

    val infiniteTransition = rememberInfiniteTransition(label = "wizard")

    val blink by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                1f at 0; 1f at 2900; 0.1f at 3050; 1f at 3200
            }
        ), label = "blink"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val mouthOpen by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "mouth"
    )

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "twinkle"
    )

    Box(modifier = modifier.size(170.dp)) {
        Canvas(modifier = Modifier.size(170.dp)) {
            val scale = if (state == AvatarState.LISTENING) pulse else 1f
            drawWizard(
                bodyScale = scale,
                eyeScaleY = if (state == AvatarState.LISTENING) 1f else blink,
                mouthOpenAmount = if (state == AvatarState.SPEAKING) mouthOpen else 0.15f,
                starAlpha = starTwinkle,
                robeColor = robeColor,
                starColor = starColor,
                skinColor = skinColor,
                beardColor = beardColor,
                showListeningRing = state == AvatarState.LISTENING,
                idleAnimation = idleAnimation
            )
        }
    }
}

private fun DrawScope.drawWizard(
    bodyScale: Float,
    eyeScaleY: Float,
    mouthOpenAmount: Float,
    starAlpha: Float,
    robeColor: Color,
    starColor: Color,
    skinColor: Color,
    beardColor: Color,
    showListeningRing: Boolean,
    idleAnimation: IdleAnimation = IdleAnimation.NONE
) {
    val center = Offset(size.width / 2, size.height / 2)
    val unit = (size.minDimension / 2.2f) * bodyScale

    if (showListeningRing) {
        drawCircle(color = robeColor.copy(alpha = 0.2f), radius = unit * 1.3f, center = center)
    }

    if (idleAnimation == IdleAnimation.SITTING) {
        val chairColor = Color(0xFF8B5A2B)
        drawLine(chairColor, Offset(center.x - unit * 0.5f, center.y + unit * 1.1f), Offset(center.x - unit * 0.5f, center.y + unit * 1.6f), strokeWidth = unit * 0.08f)
        drawLine(chairColor, Offset(center.x + unit * 0.5f, center.y + unit * 1.1f), Offset(center.x + unit * 0.5f, center.y + unit * 1.6f), strokeWidth = unit * 0.08f)
        drawLine(chairColor, Offset(center.x - unit * 0.6f, center.y + unit * 1.1f), Offset(center.x + unit * 0.6f, center.y + unit * 1.1f), strokeWidth = unit * 0.1f)
        drawLine(chairColor, Offset(center.x + unit * 0.55f, center.y + unit * 0.1f), Offset(center.x + unit * 0.55f, center.y + unit * 1.15f), strokeWidth = unit * 0.08f)
    }

    if (idleAnimation == IdleAnimation.PIGEON_TRICK) {
        val pigeonCenter = Offset(center.x + unit * 0.55f, center.y - unit * 1.7f)
        drawCircle(color = Color.White, radius = unit * 0.22f, center = pigeonCenter)
        drawCircle(color = Color.White, radius = unit * 0.14f, center = Offset(pigeonCenter.x + unit * 0.2f, pigeonCenter.y - unit * 0.05f))
        drawCircle(color = Color(0xFFFF9800), radius = unit * 0.03f, center = Offset(pigeonCenter.x + unit * 0.3f, pigeonCenter.y - unit * 0.05f))
    }

    val robePath = Path().apply {
        moveTo(center.x, center.y - unit * 0.1f)
        lineTo(center.x - unit * 0.75f, center.y + unit * 1.1f)
        lineTo(center.x + unit * 0.75f, center.y + unit * 1.1f)
        close()
    }
    drawPath(robePath, color = robeColor)

    val starPositions = listOf(
        Offset(center.x - unit * 0.35f, center.y + unit * 0.5f),
        Offset(center.x + unit * 0.3f, center.y + unit * 0.7f),
        Offset(center.x - unit * 0.1f, center.y + unit * 0.85f)
    )
    starPositions.forEach { pos -> drawStar(pos, unit * 0.09f, starColor.copy(alpha = starAlpha)) }

    val faceRadius = unit * 0.55f
    drawCircle(color = skinColor, radius = faceRadius, center = Offset(center.x, center.y - unit * 0.35f))

    val faceCenter = Offset(center.x, center.y - unit * 0.35f)

    val eyeOffsetX = faceRadius * 0.4f
    val eyeY = faceCenter.y - faceRadius * 0.05f
    val eyeRadius = faceRadius * 0.12f
    val crossEyed = idleAnimation == IdleAnimation.FUNNY_FACE
    listOf(-1f, 1f).forEach { side ->
        val xOffset = if (crossEyed) side * eyeOffsetX * 0.5f else side * eyeOffsetX
        drawOval(
            color = Color(0xFF2C4A9E),
            topLeft = Offset(
                faceCenter.x + xOffset - eyeRadius,
                eyeY - eyeRadius * eyeScaleY
            ),
            size = Size(eyeRadius * 2, eyeRadius * 2 * eyeScaleY)
        )
    }

    listOf(-1f, 1f).forEach { side ->
        drawLine(
            color = beardColor,
            start = Offset(faceCenter.x + side * eyeOffsetX - eyeRadius, eyeY - eyeRadius * 2.2f),
            end = Offset(faceCenter.x + side * eyeOffsetX + eyeRadius, eyeY - eyeRadius * 2.8f),
            strokeWidth = eyeRadius * 0.7f
        )
    }

    val mouthWidth = faceRadius * 0.5f
    val mouthHeight = faceRadius * 0.35f * mouthOpenAmount
    drawOval(
        color = Color(0xFF6B3B2A),
        topLeft = Offset(faceCenter.x - mouthWidth / 2, faceCenter.y + faceRadius * 0.35f - mouthHeight / 2),
        size = Size(mouthWidth, mouthHeight)
    )

    if (idleAnimation == IdleAnimation.FUNNY_FACE) {
        drawOval(
            color = Color(0xFFE0607E),
            topLeft = Offset(faceCenter.x - mouthWidth * 0.2f, faceCenter.y + faceRadius * 0.45f),
            size = Size(mouthWidth * 0.4f, faceRadius * 0.35f)
        )
    }

    val beardPath = Path().apply {
        moveTo(faceCenter.x - faceRadius * 0.6f, faceCenter.y + faceRadius * 0.2f)
        quadraticTo(
            faceCenter.x, faceCenter.y + faceRadius * 1.6f,
            faceCenter.x + faceRadius * 0.6f, faceCenter.y + faceRadius * 0.2f
        )
        close()
    }
    drawPath(beardPath, color = beardColor)

    val hatPath = Path().apply {
        moveTo(faceCenter.x, faceCenter.y - faceRadius * 2.3f)
        lineTo(faceCenter.x - faceRadius * 0.9f, faceCenter.y - faceRadius * 0.75f)
        lineTo(faceCenter.x + faceRadius * 0.9f, faceCenter.y - faceRadius * 0.75f)
        close()
    }
    drawPath(hatPath, color = robeColor)
    drawOval(
        color = robeColor,
        topLeft = Offset(faceCenter.x - faceRadius * 1.05f, faceCenter.y - faceRadius * 0.85f),
        size = Size(faceRadius * 2.1f, faceRadius * 0.25f)
    )
    drawStar(
        Offset(faceCenter.x, faceCenter.y - faceRadius * 1.6f),
        faceRadius * 0.16f,
        starColor.copy(alpha = starAlpha)
    )
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val angle = Math.PI * i / points - Math.PI / 2
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}
