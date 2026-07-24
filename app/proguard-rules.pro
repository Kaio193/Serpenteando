# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

# ===== Filament 3D Engine =====
-keep class com.google.android.filament.** { *; }
-dontwarn com.google.android.filament.**
-keepclassmembers class com.google.android.filament.Engine { *; }
-keepclassmembers class com.google.android.filament.Renderer { *; }

# ===== GLB/glTF model loading =====
-keep class com.kaio.cobra3d.view.renderer.** { *; }
-keepclassmembers class com.kaio.cobra3d.model.entity.** { *; }

# ===== SQLite database entities =====
-keep class com.kaio.cobra3d.model.entity.** { *; }
-keepclassmembers class com.kaio.cobra3d.model.entity.** { *; }
