package ru.finny.pet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finny.pet.content.ContentRepository
import ru.finny.pet.domain.model.ActionFeedback
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.OwnedClothingVariant
import ru.finny.pet.ui.theme.FinnyCoral
import ru.finny.pet.ui.theme.FinnyGrass
import ru.finny.pet.ui.theme.FinnySky
import ru.finny.pet.ui.theme.FinnySun

val LocalFinnyContent = compositionLocalOf<ContentRepository?> { null }

@Composable
fun WorkImage(
    fileName: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val repo = LocalFinnyContent.current
    val bmp = remember(fileName, repo) { repo?.workImage(fileName) }
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = contentDescription,
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .semantics { this.contentDescription = contentDescription },
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(FinnySun.copy(alpha = 0.5f), FinnySky.copy(alpha = 0.5f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(fileName.removeSuffix(".png").uppercase(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FinnyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .semantics { contentDescription = text },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors()
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FinnySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StatBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(400),
        label = label
    )
    Column(modifier = modifier.semantics {
        contentDescription = "$label: $value из 100"
    }) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$value", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animated.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun PetAvatar(
    profile: ChildProfile,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,

    activeWorkImageFile: String? = null,
    activeWorkTitle: String? = null
) {
    val now = rememberCurrentTimeMillis()

    val workActive = !profile.isSleeping(now) &&
        !profile.activeWorkId.isNullOrEmpty() &&
        now < profile.workEndsAtEpochMs
    val sleeping = profile.isSleeping(now)
    val working = workActive && !activeWorkImageFile.isNullOrEmpty()
    val remainingMin = profile.remainingMinutes(now)

    val pulse by animateFloatAsState(
        targetValue = if (animationsEnabled) 1.04f else 1f,
        animationSpec = tween(900),
        label = "pulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {

            HouseScene(
                lightsOn = !sleeping,
                animationsEnabled = animationsEnabled,
                modifier = Modifier.size(360.dp)
            )

            when {
                working -> {
                    WorkImage(
                        fileName = activeWorkImageFile ?: "",
                        contentDescription = activeWorkTitle ?: "Работа",
                        modifier = Modifier
                            .size(320.dp)
                            .scale(if (animationsEnabled) pulse else 1f)
                    )
                }
                else -> {
                    val repo = LocalFinnyContent.current
                    val speciesFile = profile.species.name.lowercase()

                    val animalBmp = remember(sleeping, speciesFile, repo) { repo?.animalImage(speciesFile, sleeping) }
                    val petBmp = remember(profile.species, repo) { repo?.petImage(speciesFile) }
                    if (animalBmp != null) {
                        Image(
                            bitmap = animalBmp,
                            contentDescription = if (sleeping) "${profile.petName} спит" else profile.petName,
                            modifier = Modifier
                                .size(300.dp)
                                .scale(if (animationsEnabled && !sleeping) pulse else 1f),
                            contentScale = ContentScale.Fit
                        )
                    } else if (petBmp != null) {

                        Image(
                            bitmap = petBmp,
                            contentDescription = profile.petName,
                            modifier = Modifier
                                .size(260.dp)
                                .clip(CircleShape)
                                .scale(if (animationsEnabled && !sleeping) pulse else 1f),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val equippedColors = profile.wardrobe.filter { it.isEquipped }.mapNotNull { variantColor(it) }
                        val brush = if (equippedColors.isNotEmpty()) {
                            Brush.verticalGradient(equippedColors + FinnySky)
                        } else {
                            Brush.verticalGradient(listOf(FinnySun, FinnySky, FinnyGrass))
                        }
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .scale(if (animationsEnabled && !sleeping) pulse else 1f)
                                .clip(CircleShape)
                                .background(brush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(profile.species.emoji, fontSize = 96.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            profile.petName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        val statusLine = when {
            working -> "🛠 ${activeWorkTitle ?: "Идёт работа…"} · осталось ${formatMinutes(remainingMin)}"
            sleeping -> "😴 Питомец спит · осталось ${formatMinutes(remainingMin)}"
            profile.stats.isIll -> {
                val have = profile.balance
                val need = EconomyConstants.MEDICINE_PRICE
                if (have >= need) "🤒 Болеет! Лекарство доступно — купи в «Покупках» ($need)"
                else "🤒 Болеет! Накопи на лекарство: $have / $need"
            }
            else -> profile.stage.displayName
        }
        Text(
            statusLine,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = if (profile.stats.isIll) FinnyCoral else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 2
        )

        if (!profile.stats.isIll && profile.stats.zeroSinceMs != 0L) {
            val hoursLeft = EconomyConstants.SICK_AFTER_ZERO_HOURS -
                ((now - profile.stats.zeroSinceMs) / EconomyConstants.HOURS_IN_MS)
            if (hoursLeft in 1..EconomyConstants.SICK_AFTER_ZERO_HOURS) {
                Text(
                    "⚠️ Показатель на нуле! Если не исправить — питомец заболеет через ${hoursLeft} ч",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FinnyCoral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

fun formatMinutes(min: Int): String {
    val h = min / 60
    val m = min % 60
    return if (h > 0) "${h} ч ${m} мин" else "$m мин"
}

@Composable
fun rememberCurrentTimeMillis(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(30_000)
        }
    }
    return now
}

@Composable
fun HouseScene(
    lightsOn: Boolean,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {

    val breathe by animateFloatAsState(
        targetValue = if (animationsEnabled) 1.015f else 1f,
        animationSpec = tween(durationMillis = 1600),
        label = "breathe"
    )
    Canvas(modifier = modifier.scale(breathe)) {
        val w = size.width
        val h = size.height
        val groundY = h * 0.86f

        drawCircle(
            color = if (lightsOn) Color(0xFFDFF3FF) else Color(0xFF2E3A59),
            radius = h * 0.48f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.46f),
            alpha = 0.7f
        )

        drawRoundRect(
            color = if (lightsOn) Color(0xFFBFE3A7) else Color(0xFF4A5D48),
            topLeft = androidx.compose.ui.geometry.Offset(0f, groundY),
            size = androidx.compose.ui.geometry.Size(w, h - groundY),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f)
        )

        val houseLeft = w * 0.10f
        val houseRight = w * 0.52f
        val houseTop = h * 0.46f
        val houseW = houseRight - houseLeft

        drawRoundRect(
            color = if (lightsOn) Color(0xFFF6D9A0) else Color(0xFF8A7A63),
            topLeft = androidx.compose.ui.geometry.Offset(houseLeft, houseTop),
            size = androidx.compose.ui.geometry.Size(houseW, groundY - houseTop),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
        )

        val roof = androidx.compose.ui.graphics.Path().apply {
            moveTo(houseLeft - w * 0.04f, houseTop)
            lineTo((houseLeft + houseRight) / 2f, h * 0.22f)
            lineTo(houseRight + w * 0.04f, houseTop)
            close()
        }
        drawPath(roof, color = if (lightsOn) Color(0xFFE2725B) else Color(0xFF6E4A45))

        drawRoundRect(
            color = if (lightsOn) Color(0xFF9C6B3F) else Color(0xFF5A4632),
            topLeft = androidx.compose.ui.geometry.Offset(houseLeft + houseW * 0.18f, groundY - h * 0.26f),
            size = androidx.compose.ui.geometry.Size(houseW * 0.22f, h * 0.26f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.015f)
        )

        val winCx = houseLeft + houseW * 0.66f
        val winCy = houseTop + (groundY - houseTop) * 0.34f
        val winR = w * 0.055f
        if (lightsOn) {

            drawCircle(color = Color(0xFFFFE58A).copy(alpha = 0.45f), radius = winR * 2.4f,
                center = androidx.compose.ui.geometry.Offset(winCx, winCy))
            drawCircle(color = Color(0xFFFFD84D), radius = winR,
                center = androidx.compose.ui.geometry.Offset(winCx, winCy))

            drawLine(Color(0xFF9C6B3F), androidx.compose.ui.geometry.Offset(winCx - winR, winCy),
                androidx.compose.ui.geometry.Offset(winCx + winR, winCy), strokeWidth = w * 0.006f)
            drawLine(Color(0xFF9C6B3F), androidx.compose.ui.geometry.Offset(winCx, winCy - winR),
                androidx.compose.ui.geometry.Offset(winCx, winCy + winR), strokeWidth = w * 0.006f)
        } else {
            drawCircle(color = Color(0xFF3A4463), radius = winR,
                center = androidx.compose.ui.geometry.Offset(winCx, winCy))
            drawCircle(color = Color(0xFF22283C), radius = winR,
                center = androidx.compose.ui.geometry.Offset(winCx, winCy), style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.008f))
        }

        val treeCx = w * 0.76f

        drawRoundRect(
            color = if (lightsOn) Color(0xFF8B5E3C) else Color(0xFF55402E),
            topLeft = androidx.compose.ui.geometry.Offset(treeCx - w * 0.03f, h * 0.52f),
            size = androidx.compose.ui.geometry.Size(w * 0.06f, groundY - h * 0.52f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
        )

        drawCircle(color = if (lightsOn) Color(0xFF7FBF6A) else Color(0xFF3E5A44), radius = w * 0.16f,
            center = androidx.compose.ui.geometry.Offset(treeCx, h * 0.40f))
        drawCircle(color = if (lightsOn) Color(0xFF9AD488) else Color(0xFF48684F), radius = w * 0.11f,
            center = androidx.compose.ui.geometry.Offset(treeCx - w * 0.09f, h * 0.46f))
        drawCircle(color = if (lightsOn) Color(0xFF9AD488) else Color(0xFF48684F), radius = w * 0.10f,
            center = androidx.compose.ui.geometry.Offset(treeCx + w * 0.09f, h * 0.45f))

        val eyeY = h * 0.40f
        val eyeLx = treeCx - w * 0.05f
        val eyeRx = treeCx + w * 0.05f
        val eyeRr = w * 0.022f
        if (lightsOn) {
            drawCircle(Color.White, radius = eyeRr, center = androidx.compose.ui.geometry.Offset(eyeLx, eyeY))
            drawCircle(Color.White, radius = eyeRr, center = androidx.compose.ui.geometry.Offset(eyeRx, eyeY))
            drawCircle(Color(0xFF333333), radius = eyeRr * 0.45f, center = androidx.compose.ui.geometry.Offset(eyeLx, eyeY))
            drawCircle(Color(0xFF333333), radius = eyeRr * 0.45f, center = androidx.compose.ui.geometry.Offset(eyeRx, eyeY))
        } else {

            val lash = androidx.compose.ui.graphics.Path().apply {
                moveTo(eyeLx - eyeRr, eyeY)
                quadraticBezierTo(eyeLx, eyeY + eyeRr * 0.9f, eyeLx + eyeRr, eyeY)
            }
            val lash2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(eyeRx - eyeRr, eyeY)
                quadraticBezierTo(eyeRx, eyeY + eyeRr * 0.9f, eyeRx + eyeRr, eyeY)
            }
            drawPath(lash, Color(0xFFE8ECF5), style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.008f))
            drawPath(lash2, Color(0xFFE8ECF5), style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.008f))

            listOf<Pair<Float, Float>>(0.2f to 0.12f, 0.35f to 0.06f, 0.62f to 0.10f, 0.88f to 0.16f).forEach { (fx, fy) ->
                drawCircle(Color(0xFFFFF6C8), radius = w * 0.008f, center = androidx.compose.ui.geometry.Offset(w * fx, h * fy))
            }
        }
    }
}

private fun variantColor(v: OwnedClothingVariant): Color? {

    val hash = v.variantId.hashCode()
    val r = (hash shr 16 and 0xFF).coerceIn(80, 255)
    val g = (hash shr 8 and 0xFF).coerceIn(80, 255)
    val b = (hash and 0xFF).coerceIn(80, 255)
    return Color(r, g, b)
}

@Composable
fun FeedbackCard(feedback: ActionFeedback?, onDismiss: () -> Unit) {
    if (feedback == null) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feedback.isPositive) FinnyGrass.copy(alpha = 0.25f)
            else FinnyCoral.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(feedback.title, style = MaterialTheme.typography.headlineMedium)
            Text(feedback.message, style = MaterialTheme.typography.bodyLarge)
            if (feedback.balanceDelta != 0) {
                Text(
                    "Баланс: ${if (feedback.balanceDelta > 0) "+" else ""}${feedback.balanceDelta}",
                    fontWeight = FontWeight.Bold
                )
            }
            if (feedback.savingsDelta != 0) {
                Text(
                    "Накопления: ${if (feedback.savingsDelta > 0) "+" else ""}${feedback.savingsDelta}",
                    fontWeight = FontWeight.Bold
                )
            }
            feedback.petStateChange?.let { Text("Питомец: $it") }
            feedback.nextStep?.let {
                Text("Дальше: $it", style = MaterialTheme.typography.bodyMedium)
            }
            FinnyPrimaryButton("Понятно", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
        }
    }
}

@Composable
fun CoinBadge(balance: Int, savings: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FinnySun.copy(alpha = 0.35f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Баланс: $balance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text("Копилка: $savings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun EmptyHint(text: String) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
}
