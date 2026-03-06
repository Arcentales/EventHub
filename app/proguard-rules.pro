# ── ProGuard rules para EventHub ─────────────────────────────────────────────

# Conservar números de línea en stack traces para debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Firebase ──────────────────────────────────────────────────────────────────
# Firestore — conservar los modelos de datos para serialización automática
-keep class com.Arcentales.eventhub.data.models.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.functions.** { *; }

# ── Google Wallet / Play Services ─────────────────────────────────────────────
-keep class com.google.android.gms.** { *; }

# ── ML Kit Barcode ────────────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }

# ── CameraX ───────────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }

# ── Kotlin serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature

# ── Reglas generales de Jetpack Compose ───────────────────────────────────────
-keep class androidx.compose.** { *; }

# Si usas WebView con JS (no aplica aquí, pero por las dudas):
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
# }
