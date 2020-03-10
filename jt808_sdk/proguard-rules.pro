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

-dontwarn com.saicmaxus.jt808_sdk.oksocket.client.**
-dontwarn com.saicmaxus.jt808_sdk.oksocket.common.**
-dontwarn com.saicmaxus.jt808_sdk.oksocket.server.**
-dontwarn com.saicmaxus.jt808_sdk.oksocket.core.**

-keep class com.saicmaxus.jt808_sdk.oksocket.client.** { *; }
-keep class com.saicmaxus.jt808_sdk.oksocket.common.** { *; }
-keep class com.saicmaxus.jt808_sdk.oksocket.server.** { *; }
-keep class com.saicmaxus.jt808_sdk.oksocket.core.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.saicmaxus.jt808_sdk.oksocket.client.sdk.client.OkSocketOptions$* {
    *;
}
-keep class com.saicmaxus.jt808_sdk.oksocket.server.impl.OkServerOptions$* {
    *;
}
