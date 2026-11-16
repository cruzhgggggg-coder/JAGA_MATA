# JagaMata ProGuard rules
-keepattributes *Annotation*

# MediaPipe tasks
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# ML Kit
-keep class com.google.mlkit.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Dependensi turunan MediaPipe/ML Kit memakai javax.lang.model (hanya untuk
# compile-time) — tidak dibutuhkan saat runtime.
-dontwarn javax.lang.model.**
-dontwarn javax.lang.model.element.**
-dontwarn javax.lang.model.type.**
-dontwarn javax.lang.model.util.**
-dontwarn autovalue.shaded.**
-dontwarn com.squareup.javapoet.**

# Autovalue / javapoet hanya dipakai saat build, bukan runtime
-dontwarn com.google.auto.value.**
