## Options
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable,Signature,JavascriptInterface,Exceptions
-verbose

## Arch Components
-keep class * implements android.arch.lifecycle.GeneratedAdapter {<init>(...);}

## Fabric
-dontnote com.google.android.gms.**
-dontnote com.google.firebase.crash.FirebaseCrash
-keep class com.codebutler.retrograde.common.jna.**

## JNA
-dontwarn java.awt.*
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }

## Kotlin
-dontwarn kotlin.**
-dontnote kotlin.**
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keep class android.arch.lifecycle.**

## Okio
-dontwarn okio.**

## OkHttp
-dontwarn okhttp3.**
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote com.android.org.conscrypt.SSLParametersImpl
-dontnote dalvik.system.CloseGuard
-dontnote sun.security.ssl.SSLContextImpl
-dontnote org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontnote org.conscrypt.ConscryptEngineSocket

## Retrofit
-dontwarn retrofit2.Platform$Java8

## Moshi
-dontnote sun.misc.Unsafe

## Google API
-dontwarn com.google.api.client.json.jackson2.JacksonFactory
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

## Guava
-dontnote com.google.appengine.api.ThreadManager
-dontnote com.google.apphosting.api.ApiProxy

## Retrograde
-keep class com.codebutler.retrograde.lib.retro.**
-keep class **.model.**
-keepclassmembers class **.model.** {
  <init>(...);
  <fields>;
}

## Misc
-dontwarn com.uber.javaxextras.**
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn junit.**
-dontwarn com.google.errorprone.**
-dontnote android.net.http.*
