package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HistoryItemCard
import com.example.ui.components.MathTopAnimation
import com.example.ui.components.PrimeAnalysisDialog
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrimeCheckerScreen(
    viewModel: PrimeViewModel,
    modifier: Modifier = Modifier
) {
    val numberInput by viewModel.numberInput.collectAsStateWithLifecycle()
    val checkResult by viewModel.checkResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val showDialog by viewModel.showResultDialog.collectAsStateWithLifecycle()
    val speedMultiplier by viewModel.animSpeedMultiplier.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Navigation and onboarding state
    var showOnboarding by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf("SOLVER") }

    // Trigger analysis function
    val handleCheck = {
        if (numberInput.isNotEmpty()) {
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.runPrimeCheck(numberInput)
        }
    }

    // Determine what number and status to display in the Immersive Hero Graphic
    val (heroNumber, heroStatus) = remember(checkResult, history) {
        when (val result = checkResult) {
            is PrimeCheckResult.Success -> {
                result.number to "Status: ${result.resultType}"
            }
            else -> {
                if (history.isNotEmpty()) {
                    val latest = history.first()
                    latest.numberString to "Status: ${if (latest.isPrime) "PRIME" else if (latest.resultType == "NEITHER") "SPECIAL" else "COMPOSITE"}"
                } else {
                    "73" to "Status: PRIME"
                }
            }
        }
    }

    // Main layout container with transition animation
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        AnimatedContent(
            targetState = showOnboarding,
            transitionSpec = {
                if (targetState) {
                    // Slide / scale to onboarding
                    slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                } else {
                    // App opening entry animation (Slide/scale into main dashboard)
                    slideInVertically { it } + fadeIn(animationSpec = tween(600)) togetherWith
                        slideOutVertically { -it } + fadeOut(animationSpec = tween(400))
                }
            },
            label = "app_navigation_flow"
        ) { onboardingActive ->
            if (onboardingActive) {
                // 1. App Opening Onboarding/Intro Screen (Matching Phone 1 & 2 layout precisely)
                OnboardingScreen(
                    onDismiss = { showOnboarding = false }
                )
            } else {
                // 2. Main Dashboard (Solver, History, Theory) with Matte Dark Professional UI
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // App Header / Status Bar Simulation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Styled "P" Brand Logo block with soft neon shadow simulation
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonPurple)
                                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "PrimeCheck",
                                    color = OnCosmicBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "PRESENTED TO MENTOR ASHUTOSH SIR",
                                    color = NeonCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }

                        // Right header info badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurfaceVariant)
                                .border(1.dp, CosmicBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v2.1 Pro",
                                color = OnCosmicSurfaceMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Central Content View guided by Selected Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (activeTab) {
                            "SOLVER" -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    // Welcome & Dedication Header Card (Matte Dark Style, neat and clean)
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, CosmicBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonCyan.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "info",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Professional Prime Analytics Engine",
                                                        color = OnCosmicBackground,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Crafted by Yuvraj for Mentor Ashutosh Sir",
                                                        color = OnCosmicSurfaceMuted,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Immersive Hero Graphic & Mathematical Orbital Visualizer
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                        ) {
                                            HeroGraphic(
                                                number = heroNumber,
                                                status = heroStatus,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Math Orbit simulation layer (resembling phone 2 circular orbits!)
                                    item {
                                        MathTopAnimation(
                                            speedMultiplier = speedMultiplier,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                        )
                                    }

                                    // Checking Core Input Form
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                                .testTag("calculator_form_card"),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = CosmicSurface,
                                                contentColor = OnCosmicSurface
                                            ),
                                            border = BorderStroke(1.dp, CosmicBorder)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(18.dp)
                                            ) {
                                                Text(
                                                    text = "ENTER INTEGER TO ANALYZE",
                                                    color = OnCosmicSurfaceMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.5.sp
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Custom Input Row with large tracking numbers
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.CenterEnd
                                                ) {
                                                    OutlinedTextField(
                                                        value = numberInput,
                                                        onValueChange = { viewModel.onInputChange(it) },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("number_input_field"),
                                                        placeholder = {
                                                            Text(
                                                                text = "e.g., 104,729",
                                                                color = OnCosmicSurfaceMuted.copy(alpha = 0.5f),
                                                                fontSize = 16.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                        },
                                                        singleLine = true,
                                                        textStyle = LocalTextStyle.current.copy(
                                                            fontSize = 18.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.5.sp
                                                        ),
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = NeonCyan,
                                                            unfocusedBorderColor = CosmicBorder,
                                                            focusedTextColor = OnCosmicBackground,
                                                            unfocusedTextColor = OnCosmicSurface,
                                                            focusedContainerColor = CosmicSurfaceVariant,
                                                            unfocusedContainerColor = CosmicSurfaceVariant
                                                        ),
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number,
                                                            imeAction = ImeAction.Done
                                                        ),
                                                        keyboardActions = KeyboardActions(
                                                            onDone = { handleCheck() }
                                                        )
                                                    )

                                                    // Quick Clear action inside text field
                                                    if (numberInput.isNotEmpty()) {
                                                        IconButton(
                                                            onClick = { viewModel.onInputChange("") },
                                                            modifier = Modifier.padding(end = 12.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(22.dp)
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(OnCosmicSurfaceMuted.copy(alpha = 0.15f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "×",
                                                                    color = OnCosmicSurface,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Large check primality CTA button
                                                val isBtnEnabled = numberInput.isNotEmpty() && checkResult !is PrimeCheckResult.Checking
                                                Button(
                                                    onClick = { handleCheck() },
                                                    enabled = isBtnEnabled,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(50.dp)
                                                        .testTag("analyze_button"),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NeonPurple,
                                                        disabledContainerColor = CosmicSurfaceVariant,
                                                        contentColor = Color.White,
                                                        disabledContentColor = OnCosmicSurfaceMuted.copy(alpha = 0.5f)
                                                    )
                                                ) {
                                                    if (checkResult is PrimeCheckResult.Checking) {
                                                        CircularProgressIndicator(
                                                            color = NeonGold,
                                                            modifier = Modifier.size(20.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            "RUNNING PROOF ENGINE...",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            letterSpacing = 1.sp
                                                        )
                                                    } else {
                                                        Text(
                                                            "CHECK PRIMALITY",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            letterSpacing = 1.2.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Signature Tribute Footer on main solver screen
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 24.dp, bottom = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "MADE BY YUVRAJ",
                                                color = OnCosmicSurfaceMuted.copy(alpha = 0.7f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )
                                            Text(
                                                text = "Presented to Mentor Ashutosh Sir with respect",
                                                color = OnCosmicSurfaceMuted.copy(alpha = 0.5f),
                                                fontSize = 9.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }

                            "HISTORY" -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    // Segment Header
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "COMPUTATION HISTORY",
                                                color = OnCosmicSurfaceMuted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )

                                            if (history.isNotEmpty()) {
                                                TextButton(
                                                    onClick = { viewModel.clearHistory() },
                                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                                    modifier = Modifier.testTag("clear_history_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ClearAll,
                                                        contentDescription = "Clear all",
                                                        tint = NeonMagenta,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Clear All",
                                                        color = NeonMagenta,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (history.isEmpty()) {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 48.dp, horizontal = 12.dp)
                                                    .testTag("empty_history_placeholder"),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = "History empty",
                                                    tint = OnCosmicSurfaceMuted.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(54.dp)
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(
                                                    text = "No calculations performed",
                                                    color = OnCosmicSurface,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Compute or check arbitrary whole values in the Solver tab to save records.",
                                                    color = OnCosmicSurfaceMuted,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier.fillMaxWidth(0.8f)
                                                )
                                            }
                                        }
                                    } else {
                                        items(history, key = { it.id }) { item ->
                                            Box(modifier = Modifier.padding(vertical = 6.dp)) {
                                                HistoryItemCard(
                                                    check = item,
                                                    onDelete = { viewModel.deleteHistoryItem(item.id) },
                                                    onCardClick = {
                                                        viewModel.onInputChange(item.numberString)
                                                        viewModel.runPrimeCheck(item.numberString)
                                                        activeTab = "SOLVER"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "THEORY" -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "PRIMALITY CONCEPTS",
                                            color = OnCosmicSurfaceMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }

                                    // 1. Core definition
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, CosmicBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(18.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonCyan.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("1", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                    Text(
                                                        text = "What is a Prime?",
                                                        color = OnCosmicBackground,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "A prime number is a positive integer strictly greater than 1 that cannot be formed by multiplying two smaller positive integers. It has exactly two distinct natural divisors: 1 and itself.",
                                                    color = OnCosmicSurface,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Primes are the 'atoms' of number theory because of the Fundamental Theorem of Arithmetic.",
                                                    color = OnCosmicSurfaceMuted,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }

                                    // 2. Twin primes and special states
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, CosmicBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(18.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonGold.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("2", color = NeonGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                    Text(
                                                        text = "Mersenne and Twin Primes",
                                                        color = OnCosmicBackground,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "• Mersenne Primes: Primes that can be written in the form Mp = 2^p - 1. Computing them leverages special Lucas-Lehmer tests.\n\n• Twin Primes: A pair of primes that differ by exactly 2 (e.g., (11, 13), (41, 43)). The Twin Prime Conjecture asserts there are infinitely many such pairs.",
                                                    color = OnCosmicSurface,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }

                                    // 3. Algorithms card
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, CosmicBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(18.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonMagenta.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("3", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                    Text(
                                                        text = "Decomposition & Search",
                                                        color = OnCosmicBackground,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Our high-precision Prime Check algorithm utilizes trial division up to √N, with shortcuts for even powers, 3-multiples, and optimized step indices. This ensures high-speed verification while maintaining perfect deterministic correctness.",
                                                    color = OnCosmicSurface,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Bottom Immersive Navigation Bar / Gesture Bar Simulator (Matching HTML exactly)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicSurface)
                            .border(BorderStroke(0.5.dp, CosmicBorder.copy(alpha = 0.6f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Solver Tab Button
                            BottomNavItem(
                                label = "Solver",
                                icon = Icons.Default.Calculate,
                                isActive = activeTab == "SOLVER",
                                onClick = { activeTab = "SOLVER" }
                            )

                            // History Tab Button
                            BottomNavItem(
                                label = "History",
                                icon = Icons.Default.History,
                                isActive = activeTab == "HISTORY",
                                onClick = { activeTab = "HISTORY" }
                            )

                            // Theory Tab Button
                            BottomNavItem(
                                label = "Theory",
                                icon = Icons.Default.Book,
                                isActive = activeTab == "THEORY",
                                onClick = { activeTab = "THEORY" }
                            )
                        }

                        // Simulated Android Gesture Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(OnCosmicSurfaceMuted.copy(alpha = 0.25f))
                            )
                        }
                    }
                }
            }
        }

        // Floating Analysis Detail Popup (Dialogue bottom card format)
        if (!showOnboarding && showDialog && checkResult is PrimeCheckResult.Success) {
            PrimeAnalysisDialog(
                result = checkResult as PrimeCheckResult.Success,
                onDismiss = { viewModel.dismissResultDialog() }
            )
        }
    }
}

/**
 * Beautiful app onboarding splash screen replicating Phone 1 and Phone 2 elements.
 * Features a bold title layout, a rotating high-precision mathematical orbit,
 * and a stylized dedication banner for Mentor Ashutosh Sir by Yuvraj.
 */
@Composable
fun OnboardingScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotation transition for the landing mathematical orbit
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_spiral")
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Sleek top badge indicating current setup
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(CircleShape)
                .background(CosmicSurfaceVariant)
                .border(1.dp, CosmicBorder, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "MATHEMATICAL MATRIX V2.1",
                color = NeonCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }

        // 2. Bold display title (Replicating "ROCKID" layout from phone 1 in user's image)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "PRIME",
                color = OnCosmicBackground,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1.5).sp,
                lineHeight = 54.sp
            )
            Text(
                text = "NUMBERS",
                color = NeonCyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                lineHeight = 36.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Styled Dedication Ribbon (Matching matte pro UI guidelines)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.2.dp, CosmicBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DEDICATED TO",
                        color = OnCosmicSurfaceMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Mentor Ashutosh Sir",
                        color = OnCosmicBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(1.dp)
                            .background(NeonCyan.copy(alpha = 0.3f))
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Designed & Coded with respect by Yuvraj",
                        color = OnCosmicSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Floating 3D-looking Spiral Orbital (Replicating phone 2 in user's image)
        Box(
            modifier = Modifier
                .size(200.dp)
                .rotate(orbitRotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // Draw multiple orbital dotted circles (Phone 2 scanner vibe)
                drawCircle(
                    color = CosmicBorder.copy(alpha = 0.5f),
                    radius = maxRadius * 0.5f,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f)))
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.3f),
                    radius = maxRadius * 0.78f,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )
                drawCircle(
                    color = CosmicBorder.copy(alpha = 0.3f),
                    radius = maxRadius * 0.95f,
                    style = Stroke(width = 1.2f)
                )

                // Draw central "core" representing computational density
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = maxRadius * 0.45f
                    ),
                    radius = maxRadius * 0.45f
                )

                // Draw orbiting "Prime Atoms" (floating spheres at key mathematical angular positions)
                val angles = listOf(30, 90, 150, 210, 270, 330)
                angles.forEachIndexed { index, angleDeg ->
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val orbitRadius = maxRadius * (if (index % 2 == 0) 0.5f else 0.78f)
                    val x = center.x + orbitRadius * cos(angleRad).toFloat()
                    val y = center.y + orbitRadius * sin(angleRad).toFloat()

                    drawCircle(
                        color = if (index % 2 == 0) NeonCyan else NeonMagenta,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    // Core glow
                    drawCircle(
                        color = if (index % 2 == 0) NeonCyan.copy(alpha = 0.3f) else NeonMagenta.copy(alpha = 0.3f),
                        radius = 9.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // Central core text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "2, 3, 5, 7",
                    color = OnCosmicBackground,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "α-orbit",
                    color = OnCosmicSurfaceMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 4. "Start Exploring" CTA Button with Arrow-Right circle (Replicating phone 1 exactly)
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CosmicSurfaceVariant,
                contentColor = OnCosmicBackground
            ),
            border = BorderStroke(1.dp, CosmicBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "START EXPLORING",
                    color = OnCosmicBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                // Circular arrow bubble right aligned
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "»",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeroGraphic(
    number: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val isPrime = status.contains("Prime", ignoreCase = true)
    val color = if (isPrime) NeonCyan else NeonMagenta

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .testTag("immersive_hero_graphic"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = CosmicSurface
        ),
        border = BorderStroke(1.dp, CosmicBorder.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
                        radius = 400f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Decorative Bracket Corners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top-right corner bracket
                Canvas(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                ) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = color.copy(alpha = 0.25f),
                        start = Offset(w - 12.dp.toPx(), 0f),
                        end = Offset(w, 0f),
                        strokeWidth = 2.5.dp.toPx()
                    )
                    drawLine(
                        color = color.copy(alpha = 0.25f),
                        start = Offset(w, 0f),
                        end = Offset(w, 12.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
                // Bottom-left corner bracket
                Canvas(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomStart)
                ) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = color.copy(alpha = 0.25f),
                        start = Offset(0f, h),
                        end = Offset(12.dp.toPx(), h),
                        strokeWidth = 2.5.dp.toPx()
                    )
                    drawLine(
                        color = color.copy(alpha = 0.25f),
                        start = Offset(0f, h - 12.dp.toPx()),
                        end = Offset(0f, h),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // Central content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                val displayFontSize = when {
                    number.length <= 4 -> 76.sp
                    number.length <= 8 -> 48.sp
                    else -> 32.sp
                }
                Text(
                    text = number,
                    color = color,
                    fontSize = displayFontSize,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-1.5).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // High-contrast transparent backdrop pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f))
                        .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = status,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = NeonCyan
    val inactiveColor = OnCosmicSurfaceMuted

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = label.uppercase(),
            color = if (isActive) activeColor else inactiveColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
