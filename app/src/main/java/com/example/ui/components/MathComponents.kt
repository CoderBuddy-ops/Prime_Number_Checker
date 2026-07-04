package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.model.PrimeCheck
import com.example.ui.PrimeCheckResult
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MathTopAnimation(
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MathTopAnimation")

    // Rotation angle driven continuously
    val baseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing factor for glowing nodes
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Adjust actual rotation based on VM speed multiplier
    var currentAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(baseAngle) {
        currentAngle = (currentAngle + (1f * speedMultiplier)) % 360f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CosmicBackground,
                        CosmicSurface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val radiusMax = Math.min(width, height) * 0.45f

            // 1. Draw polar radial grid lines (mathematical look)
            drawCircle(
                color = CosmicBorder.copy(alpha = 0.4f),
                radius = radiusMax * 0.4f,
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            )
            drawCircle(
                color = CosmicBorder.copy(alpha = 0.4f),
                radius = radiusMax * 0.7f,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = CosmicBorder.copy(alpha = 0.2f),
                radius = radiusMax,
                style = Stroke(width = 1.5f)
            )

            // Draw axis lines
            drawLine(
                color = CosmicBorder.copy(alpha = 0.25f),
                start = Offset(centerX - radiusMax * 1.1f, centerY),
                end = Offset(centerX + radiusMax * 1.1f, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = CosmicBorder.copy(alpha = 0.25f),
                start = Offset(centerX, centerY - radiusMax * 1.1f),
                end = Offset(centerX, centerY + radiusMax * 1.1f),
                strokeWidth = 1f
            )

            // 2. Rotate and draw mathematical prime orbit structures
            rotate(currentAngle, pivot = Offset(centerX, centerY)) {
                // Draw Golden Ratio Spiral Nodes
                val primeNodes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)
                primeNodes.forEachIndexed { index, prime ->
                    val angleRad = Math.toRadians((prime * 23.5f).toDouble())
                    val scaleFactor = (index + 1) / primeNodes.size.toFloat()
                    val r = radiusMax * scaleFactor
                    val x = centerX + (r * cos(angleRad)).toFloat()
                    val y = centerY + (r * sin(angleRad)).toFloat()

                    // Pulse speed changes depending on VM checking speed
                    val currentPulse = if (speedMultiplier > 1.5f) pulse * 1.3f else pulse

                    // Glow circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.35f * currentPulse),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = 24.dp.toPx()
                        ),
                        radius = 24.dp.toPx(),
                        center = Offset(x, y)
                    )

                    // Core point
                    drawCircle(
                        color = if (prime % 2 == 0) NeonMagenta else NeonCyan,
                        radius = (3.dp + (2.dp * currentPulse)).toPx(),
                        center = Offset(x, y)
                    )
                }

                // Connect primary nodes with orbiting geometric paths
                drawCircle(
                    color = NeonPurple.copy(alpha = 0.15f),
                    radius = radiusMax * 0.85f,
                    style = Stroke(width = 1.5f)
                )
            }

            // Outer golden ratio spiral simulation
            rotate(-currentAngle * 0.4f, pivot = Offset(centerX, centerY)) {
                drawCircle(
                    color = NeonGold.copy(alpha = 0.08f),
                    radius = radiusMax * 0.55f,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )
            }
        }

        // Central math focal indicator
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(CosmicSurfaceVariant)
                .border(1.5.dp, NeonPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (speedMultiplier > 1.5f) "COMPUTING" else "PRIME\nCORE",
                color = if (speedMultiplier > 1.5f) NeonGold else NeonCyan,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun PrimeAnalysisDialog(
    result: PrimeCheckResult.Success,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val triggerHaptic = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val mainAccentColor = when (result.resultType) {
        "PRIME" -> NeonCyan
        "COMPOSITE" -> NeonMagenta
        else -> NeonGold
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Container Card - styled as an immersive bottom/center sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(bottom = 32.dp)
                    .wrapContentHeight()
                    .testTag("result_analysis_popup")
                    .clickable(enabled = false) {}, // Prevent closing when clicking card body
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CosmicSurface,
                    contentColor = OnCosmicSurface
                ),
                border = BorderStroke(1.5.dp, CosmicBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Drag pill indicator (Immersive UI touch)
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(CosmicBorder)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Beautiful glowing status circle indicator
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant)
                            .border(1.5.dp, mainAccentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (result.isPrime) "PRIME" else if (result.resultType == "NEITHER") "SPECIAL" else "COMPOSITE",
                            color = mainAccentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Sub-title confirmation header
                    Text(
                        text = "Analysis Result",
                        color = OnCosmicSurfaceMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Large displaying of analyzed number
                    val numberFontSize = when {
                        result.number.length <= 8 -> 32.sp
                        result.number.length <= 15 -> 24.sp
                        else -> 18.sp
                    }
                    Text(
                        text = result.number,
                        color = OnCosmicBackground,
                        fontSize = numberFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Detailed math explanation text
                    Text(
                        text = result.explanation,
                        color = OnCosmicSurface,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. Number attributes layout (Grid metrics)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicSurfaceVariant)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(
                            label = "DIGITS",
                            value = result.digitCount.toString(),
                            accentColor = NeonCyan
                        )
                        DividerVertical()
                        MetricItem(
                            label = "PARITY",
                            value = if (result.isEven) "EVEN" else "ODD",
                            accentColor = NeonPurple
                        )
                        DividerVertical()
                        MetricItem(
                            label = "TIME",
                            value = "${result.durationMs}ms",
                            accentColor = NeonGold
                        )
                    }

                    // Next Primes / Extra factors section
                    if (result.isPrime && result.nextPrimes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Next Prime Numbers after ${result.number}:",
                            color = OnCosmicSurfaceMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(result.nextPrimes) { prime ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CosmicSurfaceVariant)
                                        .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = prime,
                                        color = NeonCyan,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (!result.isPrime && result.smallestFactor != null) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                            border = BorderStroke(1.dp, CosmicBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Factor info",
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Smallest prime divisor factor is ${result.smallestFactor}.",
                                    color = OnCosmicSurface,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 6. Utility Action Buttons (Copy/Share) + Dismiss Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Copy Analysis Report
                        OutlinedButton(
                            onClick = {
                                triggerHaptic()
                                val report = generateReportText(result)
                                clipboardManager.setText(AnnotatedString(report))
                                Toast.makeText(context, "Report copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnCosmicSurface
                            ),
                            border = BorderStroke(1.dp, CosmicBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy text",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", fontSize = 13.sp)
                        }

                        // Share analysis report
                        OutlinedButton(
                            onClick = {
                                triggerHaptic()
                                shareReport(context, result)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnCosmicSurface
                            ),
                            border = BorderStroke(1.dp, CosmicBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large prominent dismiss results button (matching original design HTML exactly)
                    Button(
                        onClick = {
                            triggerHaptic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicSurfaceVariant,
                            contentColor = OnCosmicBackground
                        ),
                        border = BorderStroke(1.dp, CosmicBorder)
                    ) {
                        Text(
                            text = "Dismiss Results",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = OnCosmicSurfaceMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DividerVertical() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(CosmicBorder.copy(alpha = 0.5f))
    )
}

@Composable
fun HistoryItemCard(
    check: PrimeCheck,
    onDelete: () -> Unit,
    onCardClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val mainColor = when (check.resultType) {
        "PRIME" -> NeonCyan
        "COMPOSITE" -> NeonMagenta
        else -> NeonGold
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(check.timestamp) { dateFormatter.format(Date(check.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCardClick()
            }
            .testTag("history_item_${check.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CosmicSurface,
            contentColor = OnCosmicSurface
        ),
        border = BorderStroke(1.dp, CosmicBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Color Tag Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(mainColor)
                    )
                    Text(
                        text = check.resultType,
                        color = mainColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val numberDisplay = if (check.numberString.length > 20) {
                    check.numberString.take(18) + "..."
                } else {
                    check.numberString
                }

                Text(
                    text = numberDisplay,
                    color = OnCosmicBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    color = OnCosmicSurfaceMuted,
                    fontSize = 11.sp
                )
            }

            // Right side Action: Delete
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier.testTag("delete_button_${check.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = OnCosmicSurfaceMuted.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun generateReportText(result: PrimeCheckResult.Success): String {
    return """
        === PRIME CHECKER ANALYSIS REPORT ===
        Number: ${result.number}
        Status: ${result.resultType}
        Is Prime: ${result.isPrime}
        Analysis: ${result.explanation}
        
        [Detailed Metrics]
        - Digit Count: ${result.digitCount}
        - Parity: ${if (result.isEven) "Even" else "Odd"}
        - Smallest Factor: ${result.smallestFactor ?: "N/A (Divisible only by 1 and itself)"}
        ${if (result.isPrime && result.nextPrimes.isNotEmpty()) "- Next Prime Numbers: ${result.nextPrimes.joinToString(", ")}" else ""}
        - Checked in: ${result.durationMs} ms
        
        Generated via Prime Checker - High Fidelity Mobile Mathematical Core
    """.trimIndent()
}

private fun shareReport(context: Context, result: PrimeCheckResult.Success) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, generateReportText(result))
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Math Report")
    context.startActivity(shareIntent)
}
