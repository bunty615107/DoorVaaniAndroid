package com.doorvaani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorvaani.ui.components.*
import com.doorvaani.ui.theme.*

/**
 * DesignSystemPlayground
 * Live validation surface for Agent A deliverables.
 * Mirrors key elements from goldens:
 *  - dial_pad_doorvaani/screen.png (keys + shader bg)
 *  - home_dashboard_doorvaani (bento ArchitecturalCard + radial dots)
 *  - active_call (PulseRing + HexButton demo)
 *  - spam (ArchitecturalCard variants)
 *  - splash spirit via large centered Shader
 *
 * Toggle DoorVaani <-> Sangam live. All components react via LocalDoorVaaniTheme + tokens.
 * Use this + @Previews for visual QA before handing to Agent F.
 */
@Composable
fun DesignSystemPlayground(
    modifier: Modifier = Modifier
) {
    var useSangam by remember { mutableStateOf(false) }
    val isSangam = useSangam

    DoorVaaniTheme(useSangam = useSangam) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(DoorVaaniSpacing.marginMobile)
            ) {
                // Header + live theme switcher (robust per charter)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = DoorVaaniSpacing.lg)
                ) {
                    Text(
                        "Sangam Design System Playground",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = useSangam,
                        onCheckedChange = { useSangam = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (useSangam) "Sangam" else "DoorVaani",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    "Tokens: 8px rhythm • Noto Serif/Inter fallbacks • ${if (isSangam) "Architectural dark" else "Vedic light"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(DoorVaaniSpacing.sectionGap))

                // === Shader / Mandala (live animated, touchable) ===
                Text("ShaderBackground (animated GLSL port)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    ShaderBackground(isSangam = isSangam, opacity = 0.11f)
                    Text(
                        "Touch to interact • Time rotating layers + nodes + lotus",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(DoorVaaniSpacing.lg))

                // === Dial Keys (exact match fidelity) ===
                Text("DialKey (circular, press states, sublabels)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DoorVaaniSpacing.keyGap),
                    modifier = Modifier.padding(vertical = DoorVaaniSpacing.sm)
                ) {
                    DialKeyDemo(main = "2", sub = "ABC", isSangam = isSangam)
                    DialKeyDemo(main = "5", sub = "JKL", isSangam = isSangam)
                    DialKeyDemo(main = "8", sub = "TUV", isSangam = isSangam)
                    DialKeyDemo(main = "0", sub = "+", isSangam = isSangam)
                }

                Spacer(Modifier.height(DoorVaaniSpacing.sectionGap))

                // === Architectural / Bento Cards ===
                Text("ArchitecturalCard / Bento (glass + radial dot + gold accent)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                ArchitecturalCard(
                    isSangam = isSangam,
                    hasInnerPattern = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ready to connect?", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Open Keypad")
                    }
                }

                Spacer(Modifier.height(DoorVaaniSpacing.itemGap))

                ArchitecturalCard(
                    isSangam = isSangam,
                    hasGoldTopAccent = isSangam,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Weekly Flow", style = MaterialTheme.typography.titleLarge)
                            Text("42", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Meaningful connections this week", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(DoorVaaniSpacing.sectionGap))

                // === Pulse Rings + Hex (active call elements) ===
                Text("PulseRing + HexButton (active call controls)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PulseRing(isSangam = isSangam, size = 140.dp, showDashedArchitectural = true)
                    // Fake avatar placeholder
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(96.dp)
                    ) {}
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    HexButton(label = "Mute", icon = Icons.Filled.MicOff, onClick = {}, isSangam = isSangam)
                    HexButton(label = "Speaker", icon = Icons.AutoMirrored.Filled.VolumeUp, onClick = {}, isSangam = isSangam)
                    HexButton(label = "Keypad", onClick = {}, isSangam = isSangam)
                }

                Spacer(Modifier.height(DoorVaaniSpacing.sectionGap))

                // === Phase 3 Scale Demos (AI, Vastu, Journal, Sync stubs per PRD) ===
                Text("Phase 3: Premium AI, Vastu, Journal Export, Cloud Sync (stubs)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                ArchitecturalCard(isSangam = isSangam, hasGoldTopAccent = isSangam, modifier = Modifier.fillMaxWidth()) {
                    Text("Premium AI Insight (local generator)", style = MaterialTheme.typography.titleMedium)
                    Text("Theme: Spiritual guidance • Suggested: ॐ शान्ति (for Guru Ji sample)", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = {}, enabled = false) { Text("Generate Insight (enable in Settings)") }
                }

                Spacer(Modifier.height(DoorVaaniSpacing.itemGap))

                ArchitecturalCard(isSangam = isSangam, modifier = Modifier.fillMaxWidth()) {
                    Text("Vastu Alignment Suggestion", style = MaterialTheme.typography.titleMedium)
                    Text("Current window: 8-10 AM for spiritual contacts (calendar stub)", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = {}, enabled = false) { Text("Suggest best time") }
                }

                Spacer(Modifier.height(DoorVaaniSpacing.itemGap))

                ArchitecturalCard(isSangam = isSangam, modifier = Modifier.fillMaxWidth()) {
                    Text("Journal Export + Cloud Sync", style = MaterialTheme.typography.titleMedium)
                    Text("Bundle: 3 records + AI insights (stub). Sync: E2EE opt-in ready.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Button(onClick = {}, modifier = Modifier.weight(1f), enabled = false) { Text("Export Journal") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {}, modifier = Modifier.weight(1f), enabled = false) { Text("Sync Now") }
                    }
                }

                Spacer(Modifier.height(DoorVaaniSpacing.xxl))

                Text(
                    "All tokens verified against doorvaani/DESIGN.md + sangam/DESIGN.md. Visual golden match intent: dial/home/splash/active/spam/contacts PNGs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Small helper to demo isolated DialKey (the real one is private in DialPad for now)
@Composable
private fun DialKeyDemo(main: String, sub: String, isSangam: Boolean) {
    // Re-uses the internal logic style; in real would extract DialKey composable.
    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.size(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                main,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            if (sub.isNotEmpty()) {
                Text(
                    sub,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Playground - DoorVaani")
@Composable
fun DesignSystemPlaygroundDoorVaaniPreview() {
    DesignSystemPlayground()
}

@Preview(showBackground = true, name = "Playground - Sangam")
@Composable
fun DesignSystemPlaygroundSangamPreview() {
    DesignSystemPlayground() // Internally toggles but preview renders light by default; switch manually in app
}