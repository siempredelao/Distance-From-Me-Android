# androidx.window (pulled in by material3-adaptive) references the window extensions and the older
# sidecar API. The device supplies those classes at runtime, so R8 doesn't find them at build time.
-dontwarn androidx.window.extensions.**
-dontwarn androidx.window.sidecar.**
