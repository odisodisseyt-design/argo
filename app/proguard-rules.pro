# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep DTOs that are deserialized reflectively-free but still need names.
-keep class com.textworld.app.data.model.** { *; }
-keep class com.textworld.app.data.network.** { *; }
