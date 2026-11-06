package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast

object StreamingAppLauncher {

    private const val TAG = "StreamingAppLauncher"

    fun getCandidatePackages(providerName: String): List<String> {
        val lower = providerName.lowercase()
        return when {
            lower.contains("prime") || lower.contains("amazon") -> listOf(
                "com.amazon.amazonvideo.livingroom",
                "com.amazon.avod.thirdpartyclient",
                "com.amazon.amazonvideo.livingroom.nvidia",
                "com.amazon.aiv.eu",
                "com.amazon.avod",
                "com.amazon.mp3"
            )
            lower.contains("netflix") -> listOf(
                "com.netflix.ninja",
                "com.netflix.mediaclient"
            )
            lower.contains("disney") -> listOf(
                "com.disney.disneyplus"
            )
            lower.contains("apple") -> listOf(
                "com.apple.atve.androidtv.appletv",
                "com.apple.atve.sony.appletv"
            )
            lower.contains("magenta") || lower.contains("telekom") -> listOf(
                "de.telekom.magentatv.tv",
                "de.telekom.magentatv.atv",
                "de.telekom.magentatv",
                "de.telekom.magentatv.mobile",
                "de.telekom.ott.atv",
                "com.telekom.magentatv",
                "de.telekom.magentatv.firetv"
            )
            lower.contains("joyn") -> listOf(
                "de.joyn.android.tv",
                "de.prosiebensat1digital.seventv"
            )
            lower.contains("wow") || lower.contains("sky") -> listOf(
                "de.sky.online",
                "com.bskyb.nowtv.beta"
            )
            lower.contains("rtl") || lower.contains("tvnow") -> listOf(
                "de.rtli.tvnow",
                "de.rtli.tvnow.firetv",
                "de.rtl.plus",
                "de.rtli.tvnow.androidtv"
            )
            lower.contains("paramount") -> listOf(
                "com.paramountplus.androidtv",
                "com.cbs.app",
                "com.cbs.ott"
            )
            lower.contains("youtube") || lower.contains("smarttube") -> listOf(
                "org.smarttube.beta",
                "com.teamsmart.videomanager.tv",
                "com.google.android.youtube.tv",
                "com.google.android.youtube"
            )
            lower.contains("crunchyroll") -> listOf(
                "com.crunchyroll.crunchyroid"
            )
            else -> emptyList()
        }
    }

    /**
     * Resolves the proper launcher Intent for Android TV apps.
     * CRITICAL: Android TV apps (e.g. Amazon Prime Video, MagentaTV) use LEANBACK_LAUNCHER,
     * so getLaunchIntentForPackage returns null unless getLeanbackLaunchIntentForPackage is used!
     */
    fun getAppLaunchIntent(pm: PackageManager, packageName: String): Intent? {
        // 1. Android TV Leanback launcher (essential for TV apps)
        try {
            val leanbackIntent = pm.getLeanbackLaunchIntentForPackage(packageName)
            if (leanbackIntent != null) {
                return leanbackIntent
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking getLeanbackLaunchIntentForPackage: ${e.message}")
        }

        // 2. Standard phone/tablet launcher
        try {
            val standardIntent = pm.getLaunchIntentForPackage(packageName)
            if (standardIntent != null) {
                return standardIntent
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking getLaunchIntentForPackage: ${e.message}")
        }

        // 3. Explicit Leanback Activity resolution
        try {
            val queryIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                setPackage(packageName)
            }
            val resolves = pm.queryIntentActivities(queryIntent, 0)
            if (resolves.isNotEmpty()) {
                val act = resolves[0].activityInfo
                return Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                    setClassName(act.packageName, act.name)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error resolving explicit leanback activity: ${e.message}")
        }

        // 4. Explicit standard Activity resolution
        try {
            val queryIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            val resolves = pm.queryIntentActivities(queryIntent, 0)
            if (resolves.isNotEmpty()) {
                val act = resolves[0].activityInfo
                return Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(act.packageName, act.name)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error resolving explicit standard activity: ${e.message}")
        }

        return null
    }

    fun launch(
        context: Context,
        providerName: String,
        title: String,
        explicitCandidates: List<String>,
        fallbackUrl: String?
    ) {
        val pm = context.packageManager
        val candidates = (explicitCandidates + getCandidatePackages(providerName)).distinct()

        Log.d(TAG, "Attempting to launch app for provider: '$providerName' (candidates: $candidates)")

        // 1. Try direct package launch with Leanback / Standard intent
        for (pkg in candidates) {
            val intent = getAppLaunchIntent(pm, pkg)
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(intent)
                    Toast.makeText(context, "Öffne $providerName...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Successfully launched package: $pkg")
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "Failed starting intent for $pkg: ${e.message}")
                }
            }
        }

        // 2. Fuzzy search installed packages on this TV device
        val lowerProvider = providerName.lowercase()
        try {
            val installedPackages = pm.getInstalledPackages(0)
            val matchedPackage = installedPackages.firstOrNull { pkgInfo ->
                val p = pkgInfo.packageName.lowercase()
                when {
                    lowerProvider.contains("prime") || lowerProvider.contains("amazon") ->
                        p.contains("amazonvideo") || p.contains("primevideo") || p.contains("avod")
                    lowerProvider.contains("magenta") || lowerProvider.contains("telekom") ->
                        p.contains("magentatv") || p.contains("telekom.ott")
                    lowerProvider.contains("netflix") -> p.contains("netflix")
                    lowerProvider.contains("disney") -> p.contains("disneyplus")
                    lowerProvider.contains("apple") -> p.contains("atve") || (p.contains("apple") && p.contains("tv"))
                    lowerProvider.contains("wow") || lowerProvider.contains("sky") -> p.contains("sky.online") || p.contains("nowtv")
                    lowerProvider.contains("rtl") || lowerProvider.contains("tvnow") ->
                        p.contains("tvnow") || p.contains("rtli") || p.contains("rtl.plus") || p.contains("de.rtl")
                    lowerProvider.contains("joyn") -> p.contains("joyn") || p.contains("seventv")
                    lowerProvider.contains("paramount") -> p.contains("paramount") || p.contains("cbs.app")
                    else -> false
                }
            }

            if (matchedPackage != null) {
                val intent = getAppLaunchIntent(pm, matchedPackage.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(intent)
                    Toast.makeText(context, "Öffne $providerName...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Successfully launched fuzzy matched package: ${matchedPackage.packageName}")
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fuzzy package search error: ${e.message}")
        }

        // 3. Try custom scheme intents (e.g. primevideo://, netflix://)
        val schemeUri = when {
            lowerProvider.contains("prime") || lowerProvider.contains("amazon") -> Uri.parse("primevideo://")
            lowerProvider.contains("netflix") -> Uri.parse("netflix://")
            lowerProvider.contains("disney") -> Uri.parse("disneyplus://")
            else -> null
        }
        if (schemeUri != null) {
            try {
                val schemeIntent = Intent(Intent.ACTION_VIEW, schemeUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (schemeIntent.resolveActivity(pm) != null) {
                    context.startActivity(schemeIntent)
                    Toast.makeText(context, "Öffne $providerName...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Scheme launch failed: ${e.message}")
            }
        }

        // 4. If the app is genuinely not installed on the TV, open Google Play Store directly on TV
        val primaryPackage = candidates.firstOrNull()
        if (primaryPackage != null) {
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$primaryPackage")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (storeIntent.resolveActivity(pm) != null) {
                    Toast.makeText(context, "$providerName App nicht gefunden. Öffne Play Store...", Toast.LENGTH_LONG).show()
                    context.startActivity(storeIntent)
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Play Store launch failed: ${e.message}")
            }
        }

        // 5. Last resort: web fallback
        if (!fallbackUrl.isNullOrBlank()) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "$providerName App konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Dedicated YouTube & SmartTube trailer launcher.
     * Supports third-party SmartTube Beta ('org.smarttube.beta') as requested by user.
     */
    fun launchTrailer(context: Context, youtubeKey: String) {
        val pm = context.packageManager
        val ytPackages = listOf(
            "org.smarttube.beta",
            "com.teamsmart.videomanager.tv",
            "com.google.android.youtube.tv",
            "com.google.android.youtube"
        )
        val videoUrl = "https://www.youtube.com/watch?v=$youtubeKey"

        // 1. Try launching directly with installed YouTube / SmartTube app
        for (pkg in ytPackages) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                    Toast.makeText(context, "Trailer wird abgespielt...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Trailer launch with package $pkg failed: ${e.message}")
            }
        }

        // 2. Try vnd.youtube scheme
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeKey")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (appIntent.resolveActivity(pm) != null) {
                context.startActivity(appIntent)
                return
            }
        } catch (e: Exception) {
            // ignore
        }

        // 3. Fallback: standard web intent
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Trailer konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
        }
    }
}
