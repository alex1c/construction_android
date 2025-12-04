# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep data classes
-keep class com.construction.domain.model.** { *; }

# Keep CalculatorEngine
-keep class com.construction.domain.engine.CalculatorEngine { *; }
-keep class com.construction.domain.engine.** { *; }

# Keep Repository
-keep class com.construction.domain.repository.** { *; }

# Keep Config
-keep class com.construction.config.** { *; }

# Keep Navigation
-keep class com.construction.navigation.** { *; }

# Keep UI components (for Compose)
-keep class com.construction.ui.** { *; }

# Keep utility classes
-keep class com.construction.util.** { *; }


