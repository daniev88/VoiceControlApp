# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep all classes in our package
-keep class com.voicecontrol.app.** { *; }

# Keep accessibility service
-keep class * extends android.accessibilityservice.AccessibilityService { *; }

# Keep speech recognition classes
-keep class android.speech.** { *; }

# Keep database classes
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }

# Keep service classes
-keep class * extends android.app.Service { *; }

# Keep broadcast receiver classes
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep overlay classes
-keep class android.view.WindowManager$LayoutParams { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile