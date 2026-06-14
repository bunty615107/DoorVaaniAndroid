package com.doorvaani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.doorvaani.domain.repository.FakePreferencesRepository
import com.doorvaani.navigation.DoorVaaniNavHost
import com.doorvaani.ui.theme.DoorVaaniTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * DoorVaani - Android Application
 *
 * Phase 0 complete foundation following the MultiAgent prompt + full PRD.
 * - Root theme (DoorVaani light primary / Sangam dark architectural)
 * - DoorVaaniNavHost provides Splash (auto-advance) + Scaffold + Bottom Navigation + all core screens
 *   (Home bento dashboard, refined DialPad, Contacts bento grid, Settings, Active Call flow stub)
 * - All design system (tokens + ShaderBackground + HexButton + PulseRing + ArchitecturalCard) wired
 * - Navigation, call state machine (CallViewModel), fakes, haptics, permission helper in place
 *
 * Design fidelity: Every major screen/component matches Stitch goldens in stitch_opendialer_sangam_design_system/
 * (home_dashboard_doorvaani, dial_pad_doorvaani, contacts_doorvaani, active_call_*, splash_*, etc.)
 *
 * Run locally: Open DoorVaaniAndroid/ in Android Studio → Sync Gradle → Run (API 26+ device/emulator recommended for shaders).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Observe theme from shared FakePreferencesRepository (synced with Settings + Home VM)
            // This ensures global DoorVaani <-> Sangam switch propagates to all screens including root.
            var useSangam by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                FakePreferencesRepository.preferences.collectLatest { prefs ->
                    useSangam = prefs.themeIsSangam
                }
            }

            DoorVaaniTheme(useSangam = useSangam) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DoorVaaniNavHost(startInSplash = true)
                }
            }
        }
    }
}