package com.example.data.sync

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistItem
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Bidirektionaler Echtzeit-Synchronisationsdienst zwischen Android TV (Room)
 * und Firebase Firestore (/users/{userId}/watchlist/{itemId}).
 */
class WatchlistFirestoreSyncService(
    private val context: Context,
    private val dao: WatchlistDao,
    private val scope: CoroutineScope
) {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow("Bereit")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private var snapshotListener: ListenerRegistration? = null
    private var isApplyingRemoteUpdate = false

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                _syncStatus.value = "Mit Cloud synchronisiert (${user.email ?: user.displayName ?: user.uid})"
                startFirestoreListener(user.uid)
            } else {
                _syncStatus.value = "Lokaler Modus (Nicht angemeldet)"
                stopFirestoreListener()
            }
        }
    }

    /**
     * Startet den Firestore-Echtzeit-Listener für die Watchlist des angemeldeten Benutzers.
     * Eingehende Web-Companion-Einträge werden sofort in die lokale Room-Datenbank geschrieben.
     */
    fun startFirestoreListener(userId: String) {
        stopFirestoreListener()
        val collectionRef = firestore.collection("users").document(userId).collection("watchlist")

        snapshotListener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreSync", "Snapshot listener error: ${error.message}", error)
                _syncStatus.value = "Sync-Fehler: ${error.localizedMessage}"
                return@addSnapshotListener
            }

            if (snapshot != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        isApplyingRemoteUpdate = true
                        for (doc in snapshot.documents) {
                            val data = doc.data ?: continue
                            val id = (data["id"] as? Number)?.toLong() ?: continue
                            val title = data["title"] as? String ?: "Unbekannter Titel"
                            val mediaType = (data["mediaType"] as? String) ?: "movie"
                            val posterPath = data["posterPath"] as? String
                            val backdropPath = data["backdropPath"] as? String
                            val overview = data["overview"] as? String
                            val releaseDate = data["releaseDate"] as? String
                            val voteAverage = (data["voteAverage"] as? Number)?.toDouble() ?: 0.0
                            val status = (data["status"] as? String) ?: WatchlistItem.STATUS_PLAN_TO_WATCH
                            val currentSeason = (data["currentSeason"] as? Number)?.toInt() ?: 1
                            val currentEpisode = (data["currentEpisode"] as? Number)?.toInt() ?: 0
                            val totalSeasons = (data["totalSeasons"] as? Number)?.toInt()
                            val seasonEpisodeCount = data["seasonEpisodeCount"] as? String
                            val addedAt = (data["addedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            val lastWatchedAt = (data["lastWatchedAt"] as? Number)?.toLong() ?: 0L
                            val streamingProviders = data["streamingProviders"] as? String

                            val existing = dao.getItemByIdDirect(id)
                            val item = WatchlistItem(
                                id = id,
                                mediaType = mediaType,
                                title = title,
                                originalTitle = existing?.originalTitle,
                                overview = overview ?: existing?.overview,
                                posterPath = posterPath ?: existing?.posterPath,
                                backdropPath = backdropPath ?: existing?.backdropPath,
                                releaseYear = releaseDate?.take(4) ?: existing?.releaseYear,
                                voteAverage = voteAverage,
                                runtime = existing?.runtime,
                                numberOfSeasons = totalSeasons ?: existing?.numberOfSeasons,
                                numberOfEpisodes = existing?.numberOfEpisodes,
                                genres = existing?.genres ?: "",
                                status = status,
                                currentSeason = currentSeason,
                                currentEpisode = currentEpisode,
                                seasonEpisodeCountsJson = seasonEpisodeCount ?: existing?.seasonEpisodeCountsJson,
                                addedAt = addedAt,
                                lastWatchedAt = lastWatchedAt,
                                trailerYoutubeKey = existing?.trailerYoutubeKey,
                                primaryProviderName = existing?.primaryProviderName,
                                primaryProviderLogo = existing?.primaryProviderLogo,
                                providersJson = streamingProviders ?: existing?.providersJson
                            )
                            dao.insert(item)
                        }
                    } catch (e: Exception) {
                        Log.e("FirestoreSync", "Error inserting remote items: ${e.message}", e)
                    } finally {
                        isApplyingRemoteUpdate = false
                    }
                }
            }
        }
    }

    fun stopFirestoreListener() {
        snapshotListener?.remove()
        snapshotListener = null
    }

    /**
     * Pusht eine lokale Änderung (Titel hinzugefügt / Status geändert) nach Firestore.
     */
    suspend fun syncItemToCloud(item: WatchlistItem) {
        val user = auth.currentUser ?: return
        if (isApplyingRemoteUpdate) return

        withContext(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("users").document(user.uid)
                    .collection("watchlist").document(item.id.toString())

                val payload = hashMapOf<String, Any>(
                    "id" to item.id,
                    "mediaType" to item.mediaType,
                    "title" to item.title,
                    "posterPath" to (item.posterPath ?: ""),
                    "backdropPath" to (item.backdropPath ?: ""),
                    "overview" to (item.overview ?: ""),
                    "releaseDate" to (item.releaseYear ?: ""),
                    "voteAverage" to item.voteAverage,
                    "status" to item.status,
                    "currentSeason" to item.currentSeason,
                    "currentEpisode" to item.currentEpisode,
                    "totalSeasons" to (item.numberOfSeasons ?: 0),
                    "seasonEpisodeCount" to (item.seasonEpisodeCountsJson ?: "{}"),
                    "addedAt" to item.addedAt,
                    "lastWatchedAt" to item.lastWatchedAt,
                    "streamingProviders" to (item.providersJson ?: "[]"),
                    "source" to "android_tv",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("FirestoreSync", "Cloud sync write failed: ${e.message}")
            }
        }
    }

    /**
     * Löscht ein Item aus Firestore, wenn es am TV entfernt wird.
     */
    suspend fun deleteItemFromCloud(itemId: Long) {
        val user = auth.currentUser ?: return
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("watchlist").document(itemId.toString()).delete().await()
            } catch (e: Exception) {
                Log.w("FirestoreSync", "Cloud sync delete failed: ${e.message}")
            }
        }
    }

    /**
     * Interaktiver Google Sign-In auf dem Android TV via Jetpack Credential Manager.
     */
    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Web-Client-ID nicht in Ressourcen gefunden")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _syncStatus.value = "Erfolgreich angemeldet"
                    onSuccess()
                } else {
                    onError("Unerwarteter Anmeldetyp")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Anmeldung fehlgeschlagen")
            }
        }
    }

    /**
     * Stiller Auto-Sign-In beim App-Start.
     */
    fun attemptAutoSignIn(activity: Activity) {
        if (auth.currentUser != null) return

        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                }
            } catch (_: Exception) {
                // Stiller Auto-Sign-in fehlgeschlagen; Nutzer kann jederzeit manuell anmelden
            }
        }
    }

    /**
     * Abmeldung vom Google-Konto.
     */
    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        auth.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {}
            stopFirestoreListener()
            onComplete()
        }
    }
}
