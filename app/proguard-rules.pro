# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep generic signature of TypeToken, JsonToken, etc.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-dontwarn com.google.errorprone.annotations.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.po4yka.heauton.**$$serializer { *; }
-keepclassmembers class com.po4yka.heauton.** {
    *** Companion;
}
-keepclasseswithmembers class com.po4yka.heauton.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Glance Widgets
-keep class androidx.glance.appwidget.** { *; }

# Markwon
-dontwarn org.commonmark.**
-keep class org.commonmark.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
