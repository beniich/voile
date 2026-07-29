# Sentry
-keep class io.sentry.** { *; }
-keep class io.sentry.android.** { *; }
-keep class io.sentry.compose.** { *; }

# Votre application
-keep class dev.voile.** { *; }

# WireGuard
-keep class com.wireguard.** { *; }
-dontwarn com.wireguard.**

# Supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Ktor (utilisé par Supabase)
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
