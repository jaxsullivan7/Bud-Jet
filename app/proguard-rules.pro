# Keep serialization-generated serializers for the local data model.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.budjet.app.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.budjet.app.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
