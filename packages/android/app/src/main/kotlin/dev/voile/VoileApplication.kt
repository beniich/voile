package dev.voile

import android.app.Application
import dev.voile.data.prefs.VoilePrefs
import dev.voile.data.auth.AuthRepository
import dev.voile.data.warp.WarpConfigRepository

class VoileApplication : Application() {
    lateinit var prefs: VoilePrefs
        private set
    lateinit var warpRepo: WarpConfigRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Init Supabase (URL et clé depuis BuildConfig)
        // Stub for now.
        /*
        SupabaseClient.init(
            url = BuildConfig.SUPABASE_URL,
            key = BuildConfig.SUPABASE_ANON_KEY,
        )
        */

        prefs = VoilePrefs(this)
        warpRepo = WarpConfigRepository()
    }
}
