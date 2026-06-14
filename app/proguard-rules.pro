# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Compose specific rules (important for production minification)
-keep class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# Keep our domain models (used for encrypted index parsing with reflection-ish split)
-keep class com.doorvaani.domain.model.** { *; }
-keep class com.doorvaani.domain.model.CallDirection { *; }
-keep class com.doorvaani.domain.model.SecurityLevel { *; }
-keep class com.doorvaani.domain.model.RecordingSource { *; }

# EncryptedVaultStore + crypto (avoid obfuscating key logic if any)
-keep class com.doorvaani.platform.EncryptedVaultStore { *; }
-keep class com.doorvaani.platform.RecordingCoordinator { *; }

# Keep for platform permission and telephony
-keep class com.doorvaani.platform.PermissionHelper { *; }

# General Android / Keystore (usually safe but explicit)
-keep class android.security.keystore.** { *; }
-keep class javax.crypto.** { *; }

# For MediaRecorder and Contacts (framework, but keep any wrappers)
-keep class com.doorvaani.** { *; }  # broad but safe for small app; tighten later

# Remove logs in release (optional)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep for Hilt / other if added later (none now)

# Compose preview / tooling (debug only anyway)
-dontwarn androidx.compose.ui.tooling.**
