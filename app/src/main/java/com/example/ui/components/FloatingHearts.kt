package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.ui.theme.NeonHeartCyan
import com.example.ui.theme.NeonHeartGold
import com.example.ui.theme.NeonHeartPink
import com.example.ui.theme.NeonHeartRed
import com.example.ui.theme.NeonHeartViolet
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class HeartParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val wobbleFrequency: Float,
    val wobbleAmplitude: Float,
    val rotation: Float,
    val durationMs: Int
)

val HEART_COLORS = listOf(
    NeonHeartPink,
    NeonHeartCyan,
    NeonHeartRed,
    NeonHeartGold,
    NeonHeartViolet
)

fun createRandomHeart(id: Long, originX: Float, originY: Float): HeartParticle {
    val spreadX = Random.nextFloat() * 120f - 60f
    val spreadY = Random.nextFloat() * 80f - 40f
    return HeartParticle(
        id = id,
        startX = originX + spreadX,
        startY = originY + spreadY,
        color = HEART_COLORS[Random.nextInt(HEART_COLORS.size)],
        size = Random.nextFloat() * 24f + 26f, // 26 to 50 dp equivalent
        wobbleFrequency = Random.nextFloat() * 8f + 4f,
        wobbleAmplitude = Random.nextFloat() * 30f + 15f,
        rotation = Random.nextFloat() * 40f - 20f,
        durationMs = Random.nextInt(900, 1400)
    )
}

@Composable
fun SingleAnimatedHeart(
    particle: HeartParticle,
    onFinished: (Long) -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(particle.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = particle.durationMs, easing = LinearEasing)
        )
        onFinished(particle.id)
    }

    val t = progress.value
    // Height traveled is up to 450 pixels upwards
    val currentY = particle.startY - (t * 480f)
    val currentX = particle.startX + (sin(t * particle.wobbleFrequency) * particle.wobbleAmplitude)
    val alpha = (1f - t).coerceIn(0f, 1f)
    val currentScale = if (t < 0.2f) t / 0.2f else 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (alpha > 0f) {
            rotate(
                degrees = particle.rotation + (t * 20f),
                pivot = Offset(currentX, currentY)
            ) {
                scale(
                    scale = currentScale,
                    pivot = Offset(currentX, currentY)
                ) {
                    drawHeart(
                        center = Offset(currentX, currentY),
                        size = particle.size,
                        color = particle.color.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}

fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path()
    val width = size
    val height = size * 0.9f
    val left = center.x - width / 2
    val top = center.y - height / 2

    path.moveTo(left + width / 2, top + height / 5)

    // Top left lobe
    path.cubicTo(
        left + width / 5, top - height / 3,
        left - width / 4, top + height / 3,
        left + width / 2, top + height
    )

    // Top right lobe
    path.cubicTo(
        left + width * 1.25f, top + height / 3,
        left + width * 0.8f, top - height / 3,
        left + width / 2, top + height / 5
    )
    path.close()

    drawPath(path = path, color = color)
}
