# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:



# ButterKnife (https://github.com/JakeWharton/butterknife/blob/master/butterknife/proguard-rules.txt)
# Automatically done from library


# InMobi Ads (https://support.inmobi.com/monetize/android-guidelines/)
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** { *; }
-dontwarn com.moat.**


# Dagger 2 (https://github.com/codepath/android_guides/wiki/Dependency-Injection-with-Dagger-2)
# No configuration needed


# GreenDAO (http://greenrobot.org/greendao/documentation/technical-faq/)
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public *;
}
-keep class **$Properties


# GraphView
# ?


# Crashlytics (https://docs.fabric.io/android/crashlytics/dex-and-proguard.html)
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-printmapping mapping.txt
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**


# Support Design
#-dontwarn android.support.design.**
#-keep class android.support.design.NavigationView { *; }
#-keep class android.support.design.Snackbar { *; }
#-keep interface android.support.design.** { *; }
#-keep public class android.support.design.R$* { *; }


# Support
#-keep class android.support.v7.app.ActionBar { *; }
#-keep class android.support.v7.app.ActionBarDrawerToggle { *; }
#-keep class android.support.v7.app.AppCompatActivity { *; }
#-keep class android.support.v7.widget.DefaultItemAnimator { *; }
#-keep class android.support.v7.widget.LinearLayoutManager { *; }
#-keep class android.support.v7.widget.RecyclerView { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class android.support.v7.widget.ShareActionProvider { *; }
#-keep class android.support.v7.widget.Toolbar { *; }
#-keep class android.support.v7.preference.Preference { *; }
#-keep class android.support.v7.preference.PreferenceFragmentCompat { *; }


# Play Services Maps & Location (https://developers.google.com/android/guides/setup)
# Automatically done from library
