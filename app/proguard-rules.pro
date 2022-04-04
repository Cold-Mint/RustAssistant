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


#--配置Glide--
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#glide如果你的API级别<=Android API 27 则需要添加 4.6.1
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn me.iwf.photopicker.adapter.**


#保留dataBean，防止Gson序列化出错
-keep class com.coldmint.rust.core.dataBean.** {
    <fields>;
    <methods>;
}

#保留dataBase，防止Gson序列化出错
-keep class com.coldmint.rust.core.database.** {
    <fields>;
    <methods>;
}

#保留程序的dataBean
-keep class com.coldmint.rust.pro.databean.**{
<fields>;
<methods>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}