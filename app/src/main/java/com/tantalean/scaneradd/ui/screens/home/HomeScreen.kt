package com.tantalean.scaneradd.ui.screens.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tantalean.scaneradd.ads.AdsManager
import com.tantalean.scaneradd.ui.components.ConfirmDialog
import com.tantalean.scaneradd.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    adsManager: AdsManager,
    onGoCamera: () -> Unit,
    onGoDocs: () -> Unit
) {
    val points by vm.points.collectAsState()
    val ctx = LocalContext.current
    val activity = ctx as? Activity

    var showMsg by remember { mutableStateOf<String?>(null) }

    // Precargar anuncio al entrar
    LaunchedEffect(Unit) { adsManager.loadRewarded() }

    val adReady = adsManager.isRewardedReady()
    val canScan = vm.canScan(points)

    val pagePadding = 16.dp
    val glassShape = RoundedCornerShape(18.dp)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Scanner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("🏆 $points") },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = pagePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            // Fondo glass grande (contenedor)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = glassShape
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Acciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Grid de botones "cuadraditos"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionTile(
                            title = if (adReady) "Anuncio" else "Cargando…",
                            subtitle = "+${vm.POINTS_PER_AD} puntos",
                            emoji = "🎁",
                            enabled = true, // permite click; si no está listo muestra msg
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (activity == null) return@ActionTile
                                adsManager.showRewarded(
                                    activity = activity,
                                    onRewarded = {
                                        vm.rewardUser()
                                        showMsg = "✅ Anuncio completado. +${vm.POINTS_PER_AD} puntos."
                                    },
                                    onNotReady = {
                                        showMsg = "⏳ El anuncio aún está cargando. Intenta de nuevo."
                                    },
                                    onClosed = {
                                        showMsg = "ℹ️ Anuncio cerrado."
                                    }
                                )
                            }
                        )

                        ActionTile(
                            title = "Escanear",
                            subtitle = "-${vm.POINTS_PER_SCAN} puntos",
                            emoji = "📷",
                            enabled = canScan,
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (canScan) onGoCamera()
                                else showMsg = "❌ No tienes puntos suficientes. Necesitas ${vm.POINTS_PER_SCAN}."
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionTile(
                            title = "Mis PDFs",
                            subtitle = "Ver / compartir",
                            emoji = "📁",
                            enabled = true,
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            onClick = onGoDocs
                        )

                        ActionTile(
                            title = "Estado",
                            subtitle = if (adReady) "Anuncio listo" else "Cargando anuncio",
                            emoji = "✨",
                            enabled = true,
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                showMsg = if (adReady) "✅ El anuncio está listo para mostrarse."
                                else "⏳ Aún cargando anuncio…"
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Reglas (glass)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = glassShape
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Reglas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("• Inicias con 0 puntos", style = MaterialTheme.typography.bodySmall)
                    Text("• Anuncio completo: +${vm.POINTS_PER_AD}", style = MaterialTheme.typography.bodySmall)
                    Text("• Escanear / Generar PDF: -${vm.POINTS_PER_SCAN}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "PDF se guarda en: Download/Scans",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }
    }

    showMsg?.let { msg ->
        ConfirmDialog(
            title = "Mensaje",
            text = msg,
            onDismiss = { showMsg = null }
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    // “Glass” = transparencia + degradado + borde suave
    val bg = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.03f)
        )
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = shape
            )
            .padding(0.dp),
        content = content
    )
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    emoji: String,
    enabled: Boolean,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    // Primary: rojo glass, Secondary: glass neutro
    val bg = if (isPrimary) {
        Brush.linearGradient(
            listOf(
                Color(0xFFE50914).copy(alpha = 0.75f),
                Color(0xFFE50914).copy(alpha = 0.45f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    }

    val borderColor = if (isPrimary) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    val contentAlpha = if (enabled) 1f else 0.45f
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .aspectRatio(1.35f) // “cuadradito” pero un pelín más ancho (se ve pro)
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null // minimal (sin ripple fuerte)
            ) { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alpha(contentAlpha)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alpha(contentAlpha)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(contentAlpha),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Start
            )
        }
    }
}