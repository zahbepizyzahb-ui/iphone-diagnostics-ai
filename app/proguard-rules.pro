# ProGuard rules for iPhone Diagnostics AI

# Keep model classes
-keep class com.diagnostics.model.** { *; }

# Keep ViewModels
-keep class com.diagnostics.viewmodel.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# ML Kit
-keep class com.google.mlkit.** { *; }

# Compose
-keep class androidx.compose.** { *; }
