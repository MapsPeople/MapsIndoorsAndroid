# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-optimizations *

# ===================== APP RULES

-keepclassmembers class com.mapsindoors.stdapp.models.** { *; }
-keep class com.caverock.**{ *;}
-dontwarn com.caverock.**
-keep class java.awt.geom.** { *; }
-dontwarn java.awt.geom.**

# ===================== MapsIndoors SDK ======= <START>
#
-keep interface com.mapsindoors.mapssdk.** { *; }
-keepclasseswithmembernames interface com.mapsindoors.mapssdk.** { *; }
-keep class com.mapsindoors.mapssdk.errors.** { *; }
-keepclassmembers class * implements com.mapsindoors.mapssdk.MIModelBase { <fields>; }
-keep class com.mapsindoors.mapssdk.dbglog
# ===================== MapsIndoors SDK ======= <END>


# ===================== Polestar

-keep class com.polestar.** { *; }

# ====================================================================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# ===================== Exclude Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# ===================== SQUAREUP (RETROFIT, OKHTTP, OKIO)
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.squareup.*{ *; }
-dontwarn com.squareup.**
-keep class okio.*{ *; }
-dontwarn okio.**
-keep class retrofit.android.*{ *;}
-dontwarn retrofit.android.**
-keep class retrofit.client.*{ *;}
-dontwarn retrofit.client.**

-keep class com.parse.*{ *; }
-dontwarn com.parse.**

-keep class com.google.appengine.api.urlfetch.*{ *; }
-dontwarn com.google.appengine.api.urlfetch.**

-keep class rx.*{ *; }
-dontwarn rx.**

-keep class org.apache.commons.codec.binary.*{ *; }
-dontwarn org.apache.commons.codec.binary.**

-keep class com.google.protobuf.*{ *;}
-dontwarn com.google.protobuf.**

-keep class J*{ *; }
-dontwarn J**

-keep class org.apache.http.*{ *;}
-dontwarn org.apache.http.**

-keep class com.google.android.gms.internal.*{ *;}
-dontwarn com.google.android.gms.internal.**

-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**
