import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-app.js";
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
  signOut,
  onAuthStateChanged
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-auth.js";
import {
  getFirestore,
  collection,
  doc,
  setDoc,
  deleteDoc,
  onSnapshot,
  serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-firestore.js";

/* ==========================================================================
   1. KONSTANTEN & STREAMING-ANBIETER MAPPING (Kompatibel mit WatchMate TV)
   ========================================================================== */

const SUPPORTED_PROVIDERS = [
  { id: "netflix", label: "Netflix", tmdbIds: [8, 1796] },
  { id: "prime", label: "Prime Video", tmdbIds: [9, 119, 10] },
  { id: "disney", label: "Disney+", tmdbIds: [337] },
  { id: "wow", label: "WOW / Sky", tmdbIds: [30, 130, 29] },
  { id: "apple", label: "Apple TV+", tmdbIds: [350, 2] },
  { id: "paramount", label: "Paramount+", tmdbIds: [531] },
  { id: "rtl", label: "RTL+", tmdbIds: [298] },
  { id: "joyn", label: "Joyn", tmdbIds: [304, 421] },
  { id: "magenta", label: "MagentaTV", tmdbIds: [178] },
  { id: "crunchyroll", label: "Crunchyroll", tmdbIds: [283] }
];

const REGIONS = [
  { code: "DE", flag: "🇩🇪" },
  { code: "AT", flag: "🇦🇹" },
  { code: "CH", flag: "🇨🇭" }
];

// Kuratierte Beispieldaten (identisch zum WatchMate TV Starter-Katalog)
const CURATED_STARTER_CATALOG = [
  {
    id: 693134,
    mediaType: "movie",
    title: "Dune: Part Two",
    posterPath: "/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
    backdropPath: "/xOMo8BRK7PfcJv9JCnx7s5hj0x2.jpg",
    overview: "Paul Atreides vereinigt sich mit Chani und den Fremen, während er auf einem Rachefeldzug gegen die Verschwörer ist, die seine Familie zerstört haben.",
    releaseDate: "2024-02-27",
    voteAverage: 8.2,
    ageRating: "FSK 12",
    totalSeasons: 0,
    seasonEpisodeCount: "{}",
    streamingProviders: ["wow", "prime"]
  },
  {
    id: 95396,
    mediaType: "tv",
    title: "Severance",
    posterPath: "/pPHpeI2X1qEd1CS1SeyrdhZ4qnT.jpg",
    backdropPath: "/ixgFmf1X59PUZam2qbAfskx2gQr.jpg",
    overview: "Mark leitet ein Team von Büroangestellten, deren Erinnerungen zwischen Arbeit und Privatleben chirurgisch getrennt wurden.",
    releaseDate: "2022-02-17",
    voteAverage: 8.4,
    ageRating: "FSK 12",
    totalSeasons: 2,
    seasonEpisodeCount: JSON.stringify({ "1": 9, "2": 10 }),
    streamingProviders: ["apple"]
  },
  {
    id: 66732,
    mediaType: "tv",
    title: "Stranger Things",
    posterPath: "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
    backdropPath: "/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
    overview: "Als ein kleiner Junge verschwindet, deckt eine Kleinstadt ein Geheimnis um geheime Experimente, übernatürliche Kräfte und ein seltsames Mädchen auf.",
    releaseDate: "2016-07-15",
    voteAverage: 8.6,
    ageRating: "FSK 16",
    totalSeasons: 4,
    seasonEpisodeCount: JSON.stringify({ "1": 8, "2": 9, "3": 8, "4": 9 }),
    streamingProviders: ["netflix"]
  },
  {
    id: 94605,
    mediaType: "tv",
    title: "Arcane",
    posterPath: "/fqldf2t8ztc9aiwn3k6mlX3tvRT.jpg",
    backdropPath: "/rkB4LyZHo1NHXFEDHl9vSD9r1lI.jpg",
    overview: "Inmitten des Konflikts zwischen den Zwillingsstädten Piltover und Zhaun kämpfen zwei Schwestern auf gegnerischen Seiten eines Krieges.",
    releaseDate: "2021-11-06",
    voteAverage: 8.8,
    ageRating: "FSK 16",
    totalSeasons: 2,
    seasonEpisodeCount: JSON.stringify({ "1": 9, "2": 9 }),
    streamingProviders: ["netflix"]
  },
  {
    id: 27205,
    mediaType: "movie",
    title: "Inception",
    posterPath: "/ljsZTbVsrQSqZgWeep2B1QiDKuh.jpg",
    backdropPath: "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg",
    overview: "Dom Cobb ist ein begnadeter Dieb, der wertvolle Geheimnisse aus den Tiefen des Unterbewusstseins während der Traumphase stiehlt.",
    releaseDate: "2010-07-15",
    voteAverage: 8.4,
    ageRating: "FSK 12",
    totalSeasons: 0,
    seasonEpisodeCount: "{}",
    streamingProviders: ["prime", "netflix"]
  },
  {
    id: 1396,
    mediaType: "tv",
    title: "Breaking Bad",
    posterPath: "/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
    backdropPath: "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
    overview: "Ein an Lungenkrebs erkrankter Chemielehrer beginnt zusammen mit einem ehemaligen Schüler Methamphetamin herzustellen, um seine Familie abzusichern.",
    releaseDate: "2008-01-20",
    voteAverage: 8.9,
    ageRating: "FSK 16",
    totalSeasons: 5,
    seasonEpisodeCount: JSON.stringify({ "1": 7, "2": 13, "3": 13, "4": 13, "5": 16 }),
    streamingProviders: ["netflix"]
  }
];

/* ==========================================================================
   2. APP STATE & KONFIGURATION
   ========================================================================= */

const DEFAULT_FIREBASE_CONFIG = {
  apiKey: "AIzaSyDrG2xPXxCaid0EoJ9x3W6D4HbWgITs9YA",
  authDomain: "gen-lang-client-0275433631.firebaseapp.com",
  projectId: "gen-lang-client-0275433631",
  storageBucket: "gen-lang-client-0275433631.firebasestorage.app",
  messagingSenderId: "443537803076",
  appId: "1:443537803076:web:watchmate-companion"
};
const FIRESTORE_DATABASE_ID = "ai-studio-android-watchmat-2b9ece1e-8769-40dd-b89f-34c6f3acd253";

const state = {
  region: localStorage.getItem("wm_region") || "DE",
  tmdbKey: localStorage.getItem("wm_tmdb_key") || "",
  subscriptions: JSON.parse(localStorage.getItem("wm_subs") || '["netflix","prime","disney"]'),
  firebaseConfig: JSON.parse(localStorage.getItem("wm_firebase_config") || "null") || DEFAULT_FIREBASE_CONFIG,
  currentUser: null,
  firestoreDb: null,
  authInstance: null,
  unsubscribeWatchlist: null,
  watchlistMap: new Map(), // Key: String(id), Value: WatchlistItem
  activeTab: "search",
  activeFilter: "ALL",
  searchResults: [...CURATED_STARTER_CATALOG],
  currentDetailItem: null
};

// URL-Parameter aus QR-Code vom Fernseher auslesen (z. B. ?tmdb_key=...&region=DE)
function applyUrlPairingParams() {
  const params = new URLSearchParams(window.location.search);
  let updated = false;

  if (params.get("tmdb_key")) {
    state.tmdbKey = params.get("tmdb_key").trim();
    localStorage.setItem("wm_tmdb_key", state.tmdbKey);
    updated = true;
  }
  if (params.get("region") && ["DE", "AT", "CH"].includes(params.get("region").toUpperCase())) {
    state.region = params.get("region").toUpperCase();
    localStorage.setItem("wm_region", state.region);
    updated = true;
  }
  if (updated) {
    window.history.replaceState({}, document.title, window.location.pathname);
    showToast("🔗 TV-Parameter über QR-Code übernommen!");
  }
}

/* ==========================================================================
   3. FIREBASE AUTH & FIRESTORE REAL-TIME SYNC
   ========================================================================== */

function initFirebaseIfConfigured() {
  if (!state.firebaseConfig || !state.firebaseConfig.apiKey) {
    loadLocalWatchlistFallback();
    updateSyncStatusUI(false);
    return;
  }

  try {
    const app = getApps().length === 0 ? initializeApp(state.firebaseConfig) : getApps()[0];
    state.authInstance = getAuth(app);
    state.firestoreDb = getFirestore(app, FIRESTORE_DATABASE_ID);

    // Auf Rückkehr von mobilem Redirect-Login prüfen
    getRedirectResult(state.authInstance)
      .then((result) => {
        if (result && result.user) {
          showToast(`🎉 Angemeldet als ${result.user.displayName || result.user.email}!`);
        }
      })
      .catch((err) => {
        handleAuthError(err);
      });

    onAuthStateChanged(state.authInstance, (user) => {
      state.currentUser = user;
      updateAuthModalUI(user);

      if (user) {
        updateSyncStatusUI(true, user.email || user.displayName);
        subscribeToUserWatchlist(user.uid);
      } else {
        if (state.unsubscribeWatchlist) state.unsubscribeWatchlist();
        loadLocalWatchlistFallback();
        updateSyncStatusUI(false);
      }
    });
  } catch (err) {
    console.error("Firebase Init Error:", err);
    showToast("⚠️ Firebase-Konfiguration ungültig. Nutze lokalen Speicher.");
    loadLocalWatchlistFallback();
  }
}

function handleAuthError(err) {
  console.error("Google Auth Error:", err);
  const domainNotice = document.getElementById("unauthorizedDomainNotice");
  if (err && err.code === "auth/unauthorized-domain") {
    if (domainNotice) domainNotice.classList.remove("hidden");
    const settingsModal = document.getElementById("settingsModal");
    if (settingsModal) settingsModal.classList.remove("hidden");
    showToast("⚠️ Domain nicht in Firebase autorisiert! Siehe Anleitung im Profil.");
  } else if (err && (err.code === "auth/popup-blocked" || err.code === "auth/cancelled-popup-request")) {
    showToast("⚠️ Popup blockiert. Bitte Weiterleitung zulassen...");
  } else if (err) {
    showToast(`⚠️ Anmeldung fehlgeschlagen: ${err.message || err.code}`);
  }
}

function subscribeToUserWatchlist(userId) {
  if (state.unsubscribeWatchlist) state.unsubscribeWatchlist();

  const watchlistRef = collection(state.firestoreDb, "users", userId, "watchlist");
  state.unsubscribeWatchlist = onSnapshot(
    watchlistRef,
    (snapshot) => {
      state.watchlistMap.clear();
      snapshot.forEach((docSnap) => {
        const data = docSnap.data();
        state.watchlistMap.set(String(data.id), data);
      });
      saveLocalWatchlistBackup();
      renderAll();
    },
    (error) => {
      console.error("Firestore Listener Error:", error);
      showToast("⚠️ Synchronisationsfehler mit Firestore");
    }
  );
}

function loadLocalWatchlistFallback() {
  const raw = localStorage.getItem("wm_local_watchlist");
  state.watchlistMap.clear();
  if (raw) {
    try {
      const arr = JSON.parse(raw);
      arr.forEach((item) => state.watchlistMap.set(String(item.id), item));
    } catch (e) {
      console.error(e);
    }
  }
  renderAll();
}

function saveLocalWatchlistBackup() {
  const arr = Array.from(state.watchlistMap.values());
  localStorage.setItem("wm_local_watchlist", JSON.stringify(arr));
}

/* ==========================================================================
   4. WATCHLIST SCHREIBOPERATIONEN (Exaktes Schema gemäß Architekturplan)
   ========================================================================== */

async function addOrUpdateWatchlistItem(mediaItem, targetStatus = "PLAN_TO_WATCH") {
  // Haptisches Feedback auf Smartphones
  if (navigator.vibrate) {
    navigator.vibrate([35, 50, 35]);
  }

  // Bei Bedarf vollständige TV-Staffeldaten & Streaming-Anbieter von TMDb nachladen
  const enriched = await enrichMediaDetailsIfNeeded(mediaItem);
  const existing = state.watchlistMap.get(String(enriched.id));
  const now = Date.now();

  const payload = {
    id: Number(enriched.id),
    mediaType: enriched.mediaType === "tv" ? "tv" : "movie",
    title: enriched.title || "Unbekannter Titel",
    posterPath: enriched.posterPath || "",
    backdropPath: enriched.backdropPath || "",
    overview: enriched.overview || "",
    releaseDate: enriched.releaseDate || "",
    voteAverage: Number(enriched.voteAverage || 0),
    status: targetStatus,
    currentSeason: existing?.currentSeason || 1,
    currentEpisode: existing?.currentEpisode || 1,
    totalSeasons: Number(enriched.totalSeasons || 0),
    seasonEpisodeCount:
      typeof enriched.seasonEpisodeCount === "string"
        ? enriched.seasonEpisodeCount
        : JSON.stringify(enriched.seasonEpisodeCount || {}),
    genres: enriched.genres || existing?.genres || "",
    runtime: Number(enriched.runtime || existing?.runtime || 0),
    trailerYoutubeKey: enriched.trailerYoutubeKey || existing?.trailerYoutubeKey || "",
    addedAt: existing?.addedAt || now,
    lastWatchedAt: targetStatus === "WATCHING" || targetStatus === "WATCHED" ? now : existing?.lastWatchedAt || 0,
    streamingProviders:
      typeof enriched.streamingProviders === "string"
        ? enriched.streamingProviders
        : JSON.stringify(enriched.streamingProviders || []),
    source: "mobile_web"
  };

  // Sofort lokal aktualisieren (Optimistic UI)
  state.watchlistMap.set(String(payload.id), payload);
  saveLocalWatchlistBackup();
  renderAll();
  updateDetailModalIfOpen(payload.id);

  // In Cloud Firestore speichern (/users/{userId}/watchlist/{itemId})
  if (state.currentUser && state.firestoreDb) {
    try {
      const itemRef = doc(
        state.firestoreDb,
        "users",
        state.currentUser.uid,
        "watchlist",
        String(payload.id)
      );
      await setDoc(
        itemRef,
        {
          ...payload,
          createdAt: existing?.createdAt || serverTimestamp(),
          updatedAt: serverTimestamp()
        },
        { merge: true }
      );
      const statusNames = {
        PLAN_TO_WATCH: "„Noch nicht gesehen“",
        WATCHING: "„Gerade am ansehen“",
        WATCHED: "„Gesehen“",
        DROPPED: "„Abgebrochen“"
      };
      showToast(`✅ „${payload.title}“ auf ${statusNames[targetStatus] || targetStatus} gesetzt!`);
    } catch (err) {
      console.error("Firestore Write Error:", err);
      showToast("⚠️ Lokal gespeichert (Cloud-Schreibfehler)");
    }
  } else {
    showToast(`📌 „${payload.title}“ gespeichert! (Konto verbinden für Live-TV-Sync)`);
  }
}

async function changeItemStatus(itemId, targetStatus) {
  let item = state.watchlistMap.get(String(itemId));
  if (!item) {
    const searchMatch = state.searchResults.find((x) => String(x.id) === String(itemId));
    if (searchMatch) {
      await addOrUpdateWatchlistItem(searchMatch, targetStatus);
      return;
    }
    return;
  }
  await addOrUpdateWatchlistItem(item, targetStatus);
}

async function incrementEpisode(itemId) {
  const item = state.watchlistMap.get(String(itemId));
  if (!item || item.mediaType !== "tv") return;

  if (navigator.vibrate) navigator.vibrate(25);

  let seasonMap = {};
  try {
    seasonMap = JSON.parse(item.seasonEpisodeCount || "{}");
  } catch (_) {}

  let nextSeason = item.currentSeason || 1;
  let nextEp = (item.currentEpisode || 1) + 1;
  const maxEpInCurrentSeason = seasonMap[String(nextSeason)] || 99;

  if (nextEp > maxEpInCurrentSeason) {
    if (item.totalSeasons > 0 && nextSeason < item.totalSeasons) {
      nextSeason += 1;
      nextEp = 1;
    } else if (item.totalSeasons > 0 && nextSeason >= item.totalSeasons) {
      item.status = "WATCHED";
      nextEp = maxEpInCurrentSeason;
    }
  }

  item.currentSeason = nextSeason;
  item.currentEpisode = nextEp;
  item.lastWatchedAt = Date.now();
  if (item.status === "PLAN_TO_WATCH") item.status = "WATCHING";

  state.watchlistMap.set(String(item.id), item);
  saveLocalWatchlistBackup();
  renderAll();
  updateDetailModalIfOpen(item.id);

  if (state.currentUser && state.firestoreDb) {
    const itemRef = doc(state.firestoreDb, "users", state.currentUser.uid, "watchlist", String(item.id));
    await setDoc(itemRef, { ...item, updatedAt: serverTimestamp() }, { merge: true });
  }
  showToast(`📺 ${item.title}: S${item.currentSeason} • E${item.currentEpisode}`);
}

async function decrementEpisode(itemId) {
  const item = state.watchlistMap.get(String(itemId));
  if (!item || item.mediaType !== "tv") return;

  if (navigator.vibrate) navigator.vibrate(25);

  let season = item.currentSeason || 1;
  let ep = (item.currentEpisode || 1) - 1;

  if (ep < 1) {
    if (season > 1) {
      season -= 1;
      let seasonMap = {};
      try {
        seasonMap = JSON.parse(item.seasonEpisodeCount || "{}");
      } catch (_) {}
      ep = seasonMap[String(season)] || 1;
    } else {
      ep = 1;
    }
  }

  item.currentSeason = season;
  item.currentEpisode = ep;
  item.lastWatchedAt = Date.now();

  state.watchlistMap.set(String(item.id), item);
  saveLocalWatchlistBackup();
  renderAll();
  updateDetailModalIfOpen(item.id);

  if (state.currentUser && state.firestoreDb) {
    const itemRef = doc(state.firestoreDb, "users", state.currentUser.uid, "watchlist", String(item.id));
    await setDoc(itemRef, { ...item, updatedAt: serverTimestamp() }, { merge: true });
  }
  showToast(`📺 ${item.title}: S${item.currentSeason} • E${item.currentEpisode}`);
}

async function removeWatchlistItem(itemId) {
  const item = state.watchlistMap.get(String(itemId));
  if (!item) return;

  state.watchlistMap.delete(String(itemId));
  saveLocalWatchlistBackup();
  renderAll();
  updateDetailModalIfOpen(itemId);

  if (state.currentUser && state.firestoreDb) {
    const itemRef = doc(state.firestoreDb, "users", state.currentUser.uid, "watchlist", String(itemId));
    await deleteDoc(itemRef);
  }
  showToast(`🗑️ „${item.title}“ von der Watchlist entfernt.`);
}

/* ==========================================================================
   5. TMDB LIVE-SUCHE & METADATEN-ANREICHERUNG (DE / AT / CH)
   ========================================================================== */

function getTmdbFetchOptions() {
  const isBearer = state.tmdbKey.length > 45;
  return isBearer
    ? { headers: { Authorization: `Bearer ${state.tmdbKey}`, accept: "application/json" } }
    : {};
}

function buildTmdbUrl(endpoint, queryParams = {}) {
  const url = new URL(`https://api.themoviedb.org/3${endpoint}`);
  url.searchParams.set("language", "de-DE");
  if (state.tmdbKey && state.tmdbKey.length <= 45) {
    url.searchParams.set("api_key", state.tmdbKey);
  }
  Object.entries(queryParams).forEach(([k, v]) => url.searchParams.set(k, v));
  return url.toString();
}

async function performTmdbSearch(query) {
  const trimmed = query.trim();
  const titleEl = document.getElementById("searchSectionTitle");
  const loadingEl = document.getElementById("searchLoading");

  if (!trimmed) {
    titleEl.textContent = "Beliebte Empfehlungen";
    state.searchResults = [...CURATED_STARTER_CATALOG];
    renderSearchResults();
    return;
  }

  titleEl.textContent = `Suchergebnisse für „${trimmed}“`;

  // Falls noch kein TMDb-Key hinterlegt ist: Lokalen Katalog durchsuchen
  if (!state.tmdbKey) {
    const q = trimmed.toLowerCase();
    state.searchResults = CURATED_STARTER_CATALOG.filter(
      (item) => item.title.toLowerCase().includes(q) || item.overview.toLowerCase().includes(q)
    );
    renderSearchResults();
    return;
  }

  loadingEl.classList.remove("hidden");
  try {
    const url = buildTmdbUrl("/search/multi", {
      query: trimmed,
      include_adult: "false",
      region: state.region
    });
    const res = await fetch(url, getTmdbFetchOptions());
    if (!res.ok) throw new Error(`TMDb HTTP ${res.status}`);
    const data = await res.json();

    const filtered = (data.results || [])
      .filter((r) => r.media_type === "movie" || r.media_type === "tv")
      .slice(0, 12)
      .map((raw) => ({
        id: raw.id,
        mediaType: raw.media_type,
        title: raw.title || raw.name || "Unbekannt",
        posterPath: raw.poster_path || "",
        backdropPath: raw.backdrop_path || "",
        overview: raw.overview || "Keine deutsche Beschreibung verfügbar.",
        releaseDate: raw.release_date || raw.first_air_date || "",
        voteAverage: raw.vote_average ? Number(raw.vote_average.toFixed(1)) : 0,
        ageRating: "",
        totalSeasons: 0,
        seasonEpisodeCount: "{}",
        streamingProviders: [],
        _detailsLoaded: false
      }));

    state.searchResults = filtered;
    renderSearchResults();

    // Streaming-Anbieter & FSK für die Top-6-Treffer im Hintergrund nachladen
    filtered.slice(0, 6).forEach(async (item) => {
      await enrichMediaDetailsIfNeeded(item);
      renderSearchResults();
    });
  } catch (err) {
    console.error("TMDb Search Error:", err);
    showToast("⚠️ TMDb-Abfrage fehlgeschlagen. Prüfe deinen API-Key.");
  } finally {
    loadingEl.classList.add("hidden");
  }
}

async function enrichMediaDetailsIfNeeded(item) {
  if (item._detailsLoaded && item.genres && (item.runtime || item.totalSeasons)) return item;

  if (!state.tmdbKey) return item;

  try {
    const append =
      item.mediaType === "movie"
        ? "watch/providers,release_dates,videos"
        : "watch/providers,content_ratings,videos";
    const url = buildTmdbUrl(`/${item.mediaType}/${item.id}`, { append_to_response: append });
    const res = await fetch(url, getTmdbFetchOptions());
    if (!res.ok) return item;

    const details = await res.json();

    // Backdrop & Overview vervollständigen falls fehlend
    if (details.backdrop_path && !item.backdropPath) item.backdropPath = details.backdrop_path;
    if (details.poster_path && !item.posterPath) item.posterPath = details.poster_path;
    if (details.overview && (!item.overview || item.overview.length < 10)) item.overview = details.overview;
    if (details.release_date && !item.releaseDate) item.releaseDate = details.release_date;
    if (details.first_air_date && !item.releaseDate) item.releaseDate = details.first_air_date;

    // Genres
    if (details.genres && Array.isArray(details.genres)) {
      item.genres = details.genres.map((g) => g.name).join(", ");
    }

    // Laufzeit bei Filmen
    if (details.runtime) {
      item.runtime = details.runtime;
    }

    // 1. Streaming-Provider in der gewählten Region (DE / AT / CH)
    const regionProviders = details["watch/providers"]?.results?.[state.region] || details["watch/providers"]?.results?.["DE"];
    const flatrateList = [
      ...(regionProviders?.flatrate || []),
      ...(regionProviders?.free || []),
      ...(regionProviders?.ads || [])
    ];
    const buyRentList = [
      ...(regionProviders?.buy || []),
      ...(regionProviders?.rent || [])
    ];

    const matchedSlugs = new Set();
    const providersArray = [];

    flatrateList.forEach((p) => {
      const match = SUPPORTED_PROVIDERS.find((sp) => sp.tmdbIds.includes(p.provider_id));
      if (match) matchedSlugs.add(match.id);
      providersArray.push({
        name: p.provider_name,
        logo: p.logo_path,
        type: "flatrate"
      });
    });

    buyRentList.forEach((p) => {
      providersArray.push({
        name: p.provider_name,
        logo: p.logo_path,
        type: "buy_rent"
      });
    });

    item.streamingProviders = Array.from(matchedSlugs);
    item.fullProvidersList = providersArray;

    // YouTube Trailer suchen (bevorzugt deutsch oder offizieller Trailer)
    const videos = details.videos?.results || [];
    const ytTrailer =
      videos.find((v) => v.site === "YouTube" && v.type === "Trailer" && (v.iso_639_1 === "de" || v.name.toLowerCase().includes("deutsch") || v.name.toLowerCase().includes("german"))) ||
      videos.find((v) => v.site === "YouTube" && v.type === "Trailer") ||
      videos.find((v) => v.site === "YouTube" && v.type === "Teaser") ||
      videos.find((v) => v.site === "YouTube");
    if (ytTrailer) {
      item.trailerYoutubeKey = ytTrailer.key;
    }

    // 2. Serien-Staffeln & Episodenanzahl
    if (item.mediaType === "tv") {
      item.totalSeasons = details.number_of_seasons || 0;
      item.numberOfEpisodes = details.number_of_episodes || 0;
      const epMap = {};
      (details.seasons || []).forEach((s) => {
        if (s.season_number > 0) {
          epMap[String(s.season_number)] = s.episode_count || 1;
        }
      });
      item.seasonEpisodeCount = JSON.stringify(epMap);

      const ratingEntry = (details.content_ratings?.results || []).find(
        (r) => r.iso_3166_1 === state.region || r.iso_3166_1 === "DE"
      );
      if (ratingEntry?.rating) item.ageRating = `FSK ${ratingEntry.rating}`;
    } else {
      // Film FSK-Altersfreigabe
      const relEntry = (details.release_dates?.results || []).find(
        (r) => r.iso_3166_1 === state.region || r.iso_3166_1 === "DE"
      );
      const cert = relEntry?.release_dates?.find((d) => d.certification)?.certification;
      if (cert) item.ageRating = `FSK ${cert}`;
    }

    item._detailsLoaded = true;
  } catch (e) {
    console.warn("Detail Enrichment Failed:", e);
  }
  return item;
}

/* ==========================================================================
   6. UI RENDERING
   ========================================================================== */

function renderAll() {
  document.getElementById("watchlistCounter").textContent = state.watchlistMap.size;
  renderSearchResults();
  renderWatchlist();
}

function parseProviders(providersField) {
  if (Array.isArray(providersField)) return providersField;
  try {
    return JSON.parse(providersField || "[]");
  } catch (_) {
    return [];
  }
}

function createMediaCardHTML(item, isWatchlistTab = false) {
  const savedItem = state.watchlistMap.get(String(item.id));
  const isSaved = Boolean(savedItem);
  const currentStatus = savedItem?.status || null;

  const year = (item.releaseDate || "").slice(0, 4) || "—";
  const typeLabel = item.mediaType === "tv" ? "Serie" : "Film";
  const rating = item.voteAverage ? `★ ${Number(item.voteAverage).toFixed(1)}` : "";
  const posterUrl = item.posterPath
    ? `https://image.tmdb.org/t/p/w342${item.posterPath}`
    : "https://via.placeholder.com/342x513/0E162B/38BDF8?text=WatchMate+TV";

  const providers = parseProviders(savedItem?.streamingProviders || item.streamingProviders);
  const providerPillsHTML =
    providers.length > 0
      ? providers
          .map((slug) => {
            const pObj = SUPPORTED_PROVIDERS.find((p) => p.id === slug);
            const label = pObj ? pObj.label : slug;
            const isSubscribed = state.subscriptions.includes(slug);
            return `<span class="provider-pill ${isSubscribed ? "subscribed" : ""}">
              ${isSubscribed ? "✓ " : ""}${label}
            </span>`;
          })
          .join("")
      : `<span class="provider-pill">Verfügbarkeit prüfen</span>`;

  // Episoden-Tracker für Serien in der Watchlist
  const episodeTrackerHTML =
    isWatchlistTab && item.mediaType === "tv"
      ? `<div class="episode-tracker">
          <span>Fortschritt: <strong class="ep-badge">Staffel ${savedItem.currentSeason || 1} • Episode ${savedItem.currentEpisode || 1}</strong></span>
          <button class="ep-next-btn" data-action="next-ep" data-id="${item.id}">+1 Episode ▶</button>
        </div>`
      : "";

  const actionsHTML = isWatchlistTab
    ? `<div class="card-actions">
        <button class="action-btn ${currentStatus === "WATCHING" ? "btn-watching active" : "btn-watchlist"}"
                data-action="toggle-watching" data-id="${item.id}">
          ${currentStatus === "WATCHING" ? "▶ Schaue ich gerade" : "▶ Jetzt anschauen"}
        </button>
        <button class="action-btn btn-danger-Ghost" data-action="remove" data-id="${item.id}">
          🗑️ Entfernen
        </button>
      </div>`
    : `<div class="card-actions">
        <button class="action-btn btn-watchlist ${currentStatus === "PLAN_TO_WATCH" ? "active" : ""}"
                data-action="plan" data-id="${item.id}">
          ${currentStatus === "PLAN_TO_WATCH" ? "✓ Auf Watchlist" : "+ Auf die Watchlist"}
        </button>
        <button class="action-btn btn-watching ${currentStatus === "WATCHING" ? "active" : ""}"
                data-action="watch-now" data-id="${item.id}">
          ${currentStatus === "WATCHING" ? "▶ Läuft auf TV" : "🍿 Jetzt anschauen"}
        </button>
      </div>`;

  return `
    <article class="media-card ${isSaved ? "in-watchlist" : ""}" data-card-id="${item.id}">
      <div class="media-card-main">
        <div class="poster-wrap">
          <img class="poster-img" src="${posterUrl}" alt="${escapeHtml(item.title)}" loading="lazy" />
          <span class="media-type-badge">${typeLabel}</span>
        </div>
        <div class="media-info">
          <h3 class="media-title">${escapeHtml(item.title)}</h3>
          <div class="media-meta-row">
            <span>${year}</span>
            ${rating ? `<span class="rating-badge">${rating}</span>` : ""}
            ${item.ageRating ? `<span class="age-badge">${escapeHtml(item.ageRating)}</span>` : ""}
            ${item.totalSeasons > 0 ? `<span>• ${item.totalSeasons} Staffeln</span>` : ""}
          </div>
          <p class="media-overview">${escapeHtml(item.overview || "")}</p>
          <div class="providers-row">${providerPillsHTML}</div>
        </div>
      </div>
      ${episodeTrackerHTML}
      ${actionsHTML}
    </article>
  `;
}

function updateDetailModalIfOpen(itemId) {
  if (!state.currentDetailItem || String(state.currentDetailItem.id) !== String(itemId)) return;
  const fresh = state.watchlistMap.get(String(itemId)) || state.currentDetailItem;
  renderDetailModalContent(fresh);
}

async function openDetailModal(item) {
  state.currentDetailItem = item;
  const modal = document.getElementById("detailModal");
  modal.classList.remove("hidden");
  renderDetailModalContent(item);

  // Im Hintergrund tiefe Details (Genres, Trailer, Provider, Laufzeit) nachladen falls nötig
  const enriched = await enrichMediaDetailsIfNeeded(item);
  if (state.currentDetailItem && String(state.currentDetailItem.id) === String(enriched.id)) {
    state.currentDetailItem = { ...state.currentDetailItem, ...enriched };
    renderDetailModalContent(state.currentDetailItem);
  }
}

function closeDetailModal() {
  const modal = document.getElementById("detailModal");
  modal.classList.add("hidden");
  state.currentDetailItem = null;
}

function renderDetailModalContent(item) {
  const savedItem = state.watchlistMap.get(String(item.id));
  const isSaved = Boolean(savedItem);
  const currentStatus = savedItem?.status || null;

  // 1. Hero Backdrop
  const heroEl = document.getElementById("detailHero");
  const backdropUrl = item.backdropPath
    ? (item.backdropPath.startsWith("http") ? item.backdropPath : `https://image.tmdb.org/t/p/w780${item.backdropPath}`)
    : "";
  if (backdropUrl) {
    heroEl.style.backgroundImage = `url("${backdropUrl}")`;
  } else {
    heroEl.style.backgroundImage = "none";
    heroEl.style.backgroundColor = "var(--bg-surface-elevated)";
  }

  // 2. Poster
  const posterEl = document.getElementById("detailPoster");
  const posterUrl = item.posterPath
    ? (item.posterPath.startsWith("http") ? item.posterPath : `https://image.tmdb.org/t/p/w342${item.posterPath}`)
    : "https://via.placeholder.com/342x513/0E162B/38BDF8?text=WatchMate+TV";
  posterEl.src = posterUrl;
  posterEl.alt = item.title;

  // 3. Typ & Titel
  document.getElementById("detailTypeBadge").textContent = item.mediaType === "tv" ? "Serie" : "Film";
  document.getElementById("detailTitle").textContent = item.title;

  // 4. Meta-Zeile: Jahr, Laufzeit / Staffeln, Rating, FSK
  const year = (item.releaseDate || "").slice(0, 4) || "—";
  document.getElementById("detailYear").textContent = year;

  const runtimeOrSeasonsEl = document.getElementById("detailRuntimeOrSeasons");
  if (item.mediaType === "tv") {
    const seasons = item.totalSeasons || 1;
    const epCount = item.numberOfEpisodes ? ` (${item.numberOfEpisodes} Folgen)` : "";
    runtimeOrSeasonsEl.textContent = `• ${seasons} ${seasons === 1 ? "Staffel" : "Staffeln"}${epCount}`;
  } else if (item.runtime && item.runtime > 0) {
    const h = Math.floor(item.runtime / 60);
    const m = item.runtime % 60;
    runtimeOrSeasonsEl.textContent = `• ${h > 0 ? `${h}h ` : ""}${m}m`;
  } else {
    runtimeOrSeasonsEl.textContent = "";
  }

  const ratingEl = document.getElementById("detailRating");
  if (item.voteAverage && Number(item.voteAverage) > 0) {
    ratingEl.textContent = `★ ${Number(item.voteAverage).toFixed(1)}`;
    ratingEl.classList.remove("hidden");
  } else {
    ratingEl.classList.add("hidden");
  }

  const ageEl = document.getElementById("detailAgeRating");
  if (item.ageRating) {
    ageEl.textContent = item.ageRating;
    ageEl.classList.remove("hidden");
  } else {
    ageEl.classList.add("hidden");
  }

  // 5. Genres
  const genresEl = document.getElementById("detailGenres");
  const genresList = (item.genres || "").split(",").map((g) => g.trim()).filter(Boolean);
  if (genresList.length > 0) {
    genresEl.innerHTML = genresList.map((g) => `<span class="genre-chip">${escapeHtml(g)}</span>`).join("");
    genresEl.classList.remove("hidden");
  } else {
    genresEl.innerHTML = "";
    genresEl.classList.add("hidden");
  }

  // 6. Status-Auswahl Buttons
  const statusBadge = document.getElementById("detailCurrentStatusBadge");
  const statusButtons = document.querySelectorAll(".status-choice-btn");
  statusButtons.forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.status === currentStatus);
  });

  if (!isSaved) {
    statusBadge.textContent = "Noch nicht auf TV-Liste";
    statusBadge.className = "status-pill offline";
  } else {
    const statusLabels = {
      PLAN_TO_WATCH: { text: "Noch nicht gesehen", cls: "offline" },
      WATCHING: { text: "Gerade am ansehen", cls: "online" },
      WATCHED: { text: "Gesehen ✓", cls: "online" },
      DROPPED: { text: "Abgebrochen", cls: "offline" }
    };
    const info = statusLabels[currentStatus] || { text: currentStatus, cls: "online" };
    statusBadge.textContent = info.text;
    statusBadge.className = `status-pill ${info.cls}`;
  }

  // 7. Serien Episoden-Tracker
  const epTracker = document.getElementById("detailEpisodeTracker");
  if (item.mediaType === "tv") {
    epTracker.classList.remove("hidden");
    const curSeason = savedItem?.currentSeason || 1;
    const curEpisode = savedItem?.currentEpisode || 1;
    document.getElementById("detailEpisodeProgressText").textContent = `Staffel ${curSeason} • Folge ${curEpisode}`;
  } else {
    epTracker.classList.add("hidden");
  }

  // 8. Streaming Anbieter
  const providersContainer = document.getElementById("detailStreamingProviders");
  const allProviders = item.fullProvidersList || [];
  const simpleProviders = parseProviders(savedItem?.streamingProviders || item.streamingProviders);

  if (allProviders.length > 0) {
    providersContainer.innerHTML = allProviders.map((p) => {
      const isSub = KnownProvidersMatches(p.name, state.subscriptions);
      const logoUrl = p.logo ? `https://image.tmdb.org/t/p/w92${p.logo}` : "";
      return `<div class="provider-pill ${isSub ? "subscribed" : ""}" style="display:inline-flex;align-items:center;gap:6px;">
        ${logoUrl ? `<img src="${logoUrl}" alt="${escapeHtml(p.name)}" style="width:18px;height:18px;border-radius:4px;object-fit:cover;" />` : ""}
        <span>${isSub ? "✓ " : ""}${escapeHtml(p.name)}</span>
        <span style="font-size:0.68rem;opacity:0.75;">(${p.type === "buy_rent" ? "Leihen/Kaufen" : "Abo"})</span>
      </div>`;
    }).join("");
  } else if (simpleProviders.length > 0) {
    providersContainer.innerHTML = simpleProviders.map((slug) => {
      const pObj = SUPPORTED_PROVIDERS.find((p) => p.id === slug);
      const label = pObj ? pObj.label : slug;
      const isSub = state.subscriptions.includes(slug);
      return `<span class="provider-pill ${isSub ? "subscribed" : ""}">${isSub ? "✓ " : ""}${escapeHtml(label)}</span>`;
    }).join("");
  } else {
    providersContainer.innerHTML = `<span class="provider-pill">Keine Streaming-Rechte für ${state.region} hinterlegt</span>`;
  }

  // 9. Handlung
  document.getElementById("detailOverview").textContent = item.overview || "Keine Beschreibung verfügbar.";

  // 10. Trailer & Löschen Buttons
  const trailerBtn = document.getElementById("detailTrailerBtn");
  if (item.trailerYoutubeKey) {
    trailerBtn.href = `https://www.youtube.com/watch?v=${item.trailerYoutubeKey}`;
    trailerBtn.classList.remove("hidden");
  } else {
    trailerBtn.classList.add("hidden");
  }

  const removeBtn = document.getElementById("detailRemoveBtn");
  removeBtn.classList.toggle("hidden", !isSaved);
}

function KnownProvidersMatches(provName, userSubs) {
  if (!provName) return false;
  const lower = provName.toLowerCase();
  for (const sub of userSubs) {
    if (sub === "netflix" && lower.includes("netflix")) return true;
    if (sub === "prime" && (lower.includes("prime") || lower.includes("amazon"))) return true;
    if (sub === "disney" && lower.includes("disney")) return true;
    if (sub === "wow" && (lower.includes("wow") || lower.includes("sky"))) return true;
    if (sub === "apple" && (lower.includes("apple") || lower.includes("itunes"))) return true;
    if (sub === "paramount" && lower.includes("paramount")) return true;
    if (sub === "rtl" && lower.includes("rtl")) return true;
    if (sub === "joyn" && lower.includes("joyn")) return true;
    if (sub === "magenta" && lower.includes("magenta")) return true;
    if (sub === "crunchyroll" && lower.includes("crunchyroll")) return true;
  }
  return false;
}

function renderSearchResults() {
  const container = document.getElementById("searchResultsGrid");
  if (!state.searchResults.length) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3>Keine Treffer gefunden</h3>
        <p>Prüfe den Titel oder hinterlege in den Einstellungen ⚙️ deinen TMDb API-Key für Millionen weitere Titel.</p>
      </div>`;
    return;
  }
  container.innerHTML = state.searchResults.map((item) => createMediaCardHTML(item, false)).join("");
}

function renderWatchlist() {
  const container = document.getElementById("watchlistGrid");
  const emptyEl = document.getElementById("watchlistEmptyState");

  let items = Array.from(state.watchlistMap.values()).sort((a, b) => (b.addedAt || 0) - (a.addedAt || 0));

  if (state.activeFilter !== "ALL") {
    items = items.filter((item) => {
      if (state.activeFilter === "movie" || state.activeFilter === "tv") {
        return item.mediaType === state.activeFilter;
      }
      return item.status === state.activeFilter;
    });
  }

  if (items.length === 0) {
    container.innerHTML = "";
    emptyEl.classList.remove("hidden");
    return;
  }

  emptyEl.classList.add("hidden");
  container.innerHTML = items.map((item) => createMediaCardHTML(item, true)).join("");
}

/* ==========================================================================
   7. HILFSFUNKTIONEN & EVENT-HANDLER
   ========================================================================== */

function updateSyncStatusUI(isOnline, label = "") {
  const dot = document.getElementById("syncStatusDot");
  const text = document.getElementById("syncStatusText");
  if (isOnline) {
    dot.classList.add("online");
    text.textContent = `Mit TV synchronisiert (${label.split("@")[0]})`;
  } else {
    dot.classList.remove("online");
    text.textContent = "Lokaler Modus (Tippe auf 👤 für Cloud-Sync)";
  }
}

function updateAuthModalUI(user) {
  const badge = document.getElementById("authBadge");
  const infoBox = document.getElementById("authUserInfo");
  const signInBtn = document.getElementById("googleSignInBtn");
  const signOutBtn = document.getElementById("googleSignOutBtn");
  const avatar = document.getElementById("userAvatar");
  const defaultIcon = document.getElementById("userIconDefault");
  const domainNotice = document.getElementById("unauthorizedDomainNotice");

  // Hostname für Firebase Authorized Domains vorbereiten
  const currentHost = window.location.hostname || "absolutmatti.github.io";
  const hostLabel = document.getElementById("currentHostnameLabel");
  const codeLabel = document.getElementById("copyDomainCode");
  if (hostLabel) hostLabel.textContent = currentHost;
  if (codeLabel) codeLabel.textContent = currentHost;

  if (user) {
    badge.textContent = "Verbunden";
    badge.className = "status-pill online";
    infoBox.textContent = `Angemeldet als: ${user.displayName || ""} (${user.email})`;
    infoBox.classList.remove("hidden");
    signInBtn.classList.add("hidden");
    signOutBtn.classList.remove("hidden");
    if (domainNotice) domainNotice.classList.add("hidden");

    if (user.photoURL) {
      avatar.src = user.photoURL;
      avatar.classList.remove("hidden");
      defaultIcon.classList.add("hidden");
    }
  } else {
    badge.textContent = "Nicht angemeldet";
    badge.className = "status-pill offline";
    infoBox.classList.add("hidden");
    signInBtn.classList.remove("hidden");
    signOutBtn.classList.add("hidden");
    avatar.classList.add("hidden");
    defaultIcon.classList.remove("hidden");
  }
}

function showToast(message) {
  const container = document.getElementById("toastContainer");
  const toast = document.createElement("div");
  toast.className = "toast";
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transition = "opacity 0.25s ease";
    setTimeout(() => toast.remove(), 250);
  }, 2800);
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function renderSubscriptionsModalGrid() {
  const grid = document.getElementById("subscriptionsGrid");
  grid.innerHTML = SUPPORTED_PROVIDERS.map((p) => {
    const active = state.subscriptions.includes(p.id);
    return `<button type="button" class="sub-chip ${active ? "active" : ""}" data-sub-id="${p.id}">
      ${active ? "✓ " : ""}${p.label}
    </button>`;
  }).join("");
}

function updateRegionButtonUI() {
  const r = REGIONS.find((reg) => reg.code === state.region) || REGIONS[0];
  document.getElementById("currentRegionFlag").textContent = r.flag;
  document.getElementById("currentRegionCode").textContent = r.code;
}

/* ==========================================================================
   8. INITIALISIERUNG & DOM-EVENTS
   ========================================================================== */

document.addEventListener("DOMContentLoaded", () => {
  applyUrlPairingParams();
  updateRegionButtonUI();
  renderSubscriptionsModalGrid();
  initFirebaseIfConfigured();
  renderAll();

  // 1. Debounced Search (300ms wie im Architekturplan spezifiziert)
  const searchInput = document.getElementById("searchInput");
  const clearBtn = document.getElementById("clearSearchBtn");
  let debounceTimer = null;

  searchInput.addEventListener("input", (e) => {
    const val = e.target.value;
    clearBtn.classList.toggle("hidden", !val);

    if (state.activeTab !== "search") switchTab("search");

    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      performTmdbSearch(val);
    }, 300);
  });

  clearBtn.addEventListener("click", () => {
    searchInput.value = "";
    clearBtn.classList.add("hidden");
    performTmdbSearch("");
    searchInput.focus();
  });

  // 2. Tab-Umschaltung
  document.querySelectorAll(".nav-tab").forEach((btn) => {
    btn.addEventListener("click", () => switchTab(btn.dataset.tab));
  });

  function switchTab(tabName) {
    state.activeTab = tabName;
    document.getElementById("tabSearchBtn").classList.toggle("active", tabName === "search");
    document.getElementById("tabWatchlistBtn").classList.toggle("active", tabName === "watchlist");
    document.getElementById("searchView").classList.toggle("hidden", tabName !== "search");
    document.getElementById("watchlistView").classList.toggle("hidden", tabName !== "watchlist");
  }

  // 3. Watchlist-Filter
  document.querySelectorAll(".filter-pill").forEach((pill) => {
    pill.addEventListener("click", () => {
      document.querySelectorAll(".filter-pill").forEach((p) => p.classList.remove("active"));
      pill.classList.add("active");
      state.activeFilter = pill.dataset.filter;
      renderWatchlist();
    });
  });

  // 4. Event-Delegation für Karten (Klick auf Karte -> Detailansicht; Klick auf Buttons -> Schnellaktion)
  document.body.addEventListener("click", async (e) => {
    // Falls auf einen Button innerhalb einer Karte geklickt wurde:
    const btn = e.target.closest("[data-action]");
    if (btn) {
      e.stopPropagation();
      const action = btn.dataset.action;
      const id = btn.dataset.id;
      const item =
        state.searchResults.find((x) => String(x.id) === String(id)) ||
        state.watchlistMap.get(String(id));

      if (!item) return;

      if (action === "plan") {
        await addOrUpdateWatchlistItem(item, "PLAN_TO_WATCH");
      } else if (action === "watch-now") {
        await addOrUpdateWatchlistItem(item, "WATCHING");
      } else if (action === "toggle-watching") {
        const nextStatus = item.status === "WATCHING" ? "PLAN_TO_WATCH" : "WATCHING";
        await addOrUpdateWatchlistItem(item, nextStatus);
      } else if (action === "next-ep") {
        await incrementEpisode(id);
      } else if (action === "remove") {
        await removeWatchlistItem(id);
      }
      return;
    }

    // Klick auf eine Film-/Serien-Karte -> Detailansicht öffnen
    const card = e.target.closest(".media-card");
    if (card) {
      const cardId = card.dataset.cardId;
      const item =
        state.watchlistMap.get(String(cardId)) ||
        state.searchResults.find((x) => String(x.id) === String(cardId));
      if (item) {
        openDetailModal(item);
      }
    }
  });

  // 4b. Detail Modal Events (Status-Auswahl, Episoden, Schließen, Löschen)
  document.getElementById("closeDetailBtn").addEventListener("click", closeDetailModal);

  // Klick auf Status-Auswahl-Buttons im Detail-Modal
  document.querySelectorAll(".status-choice-btn").forEach((statusBtn) => {
    statusBtn.addEventListener("click", async () => {
      if (!state.currentDetailItem) return;
      const targetStatus = statusBtn.dataset.status;
      await changeItemStatus(state.currentDetailItem.id, targetStatus);
    });
  });

  // Episoden-Stepper im Detail-Modal
  document.getElementById("detailNextEpBtn").addEventListener("click", async () => {
    if (!state.currentDetailItem) return;
    await incrementEpisode(state.currentDetailItem.id);
  });

  document.getElementById("detailPrevEpBtn").addEventListener("click", async () => {
    if (!state.currentDetailItem) return;
    await decrementEpisode(state.currentDetailItem.id);
  });

  // Von Watchlist löschen im Detail-Modal
  document.getElementById("detailRemoveBtn").addEventListener("click", async () => {
    if (!state.currentDetailItem) return;
    const title = state.currentDetailItem.title;
    if (confirm(`Möchtest du „${title}“ wirklich von deiner TV-Watchlist entfernen?`)) {
      await removeWatchlistItem(state.currentDetailItem.id);
      closeDetailModal();
    }
  });

  // Modal schließen bei Klick auf den Backdrop
  document.getElementById("detailModal").addEventListener("click", (e) => {
    if (e.target.id === "detailModal") {
      closeDetailModal();
    }
  });

  // Escape-Taste schließt Modals
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      closeDetailModal();
      document.getElementById("settingsModal").classList.add("hidden");
    }
  });

  // 5. Region-Wechsel (DE -> AT -> CH)
  document.getElementById("regionToggleBtn").addEventListener("click", () => {
    const idx = REGIONS.findIndex((r) => r.code === state.region);
    const next = REGIONS[(idx + 1) % REGIONS.length];
    state.region = next.code;
    localStorage.setItem("wm_region", state.region);
    updateRegionButtonUI();
    showToast(`${next.flag} Streaming-Region auf ${next.code} geändert`);
    if (searchInput.value.trim()) performTmdbSearch(searchInput.value);
  });

  // 6. Einstellungs-Modal öffnen / schließen
  const settingsModal = document.getElementById("settingsModal");
  const openSettings = () => {
    document.getElementById("tmdbApiKeyInput").value = state.tmdbKey;
    document.getElementById("firebaseConfigInput").value = state.firebaseConfig
      ? JSON.stringify(state.firebaseConfig, null, 2)
      : "";
    settingsModal.classList.remove("hidden");
  };

  document.getElementById("settingsBtn").addEventListener("click", openSettings);
  document.getElementById("authBtn").addEventListener("click", openSettings);
  document.getElementById("closeSettingsBtn").addEventListener("click", () => {
    settingsModal.classList.add("hidden");
  });

  // Abo-Chips im Modal umschalten
  document.getElementById("subscriptionsGrid").addEventListener("click", (e) => {
    const chip = e.target.closest("[data-sub-id]");
    if (!chip) return;
    const subId = chip.dataset.subId;
    if (state.subscriptions.includes(subId)) {
      state.subscriptions = state.subscriptions.filter((s) => s !== subId);
    } else {
      state.subscriptions.push(subId);
    }
    localStorage.setItem("wm_subs", JSON.stringify(state.subscriptions));
    renderSubscriptionsModalGrid();
    renderAll();
  });

  // Einstellungen speichern
  document.getElementById("saveSettingsBtn").addEventListener("click", () => {
    state.tmdbKey = document.getElementById("tmdbApiKeyInput").value.trim();
    localStorage.setItem("wm_tmdb_key", state.tmdbKey);

    const fbRaw = document.getElementById("firebaseConfigInput").value.trim();
    if (fbRaw) {
      try {
        state.firebaseConfig = JSON.parse(fbRaw);
        localStorage.setItem("wm_firebase_config", JSON.stringify(state.firebaseConfig));
      } catch (err) {
        showToast("⚠️ Firebase Config ist kein gültiges JSON.");
        return;
      }
    }

    settingsModal.classList.add("hidden");
    initFirebaseIfConfigured();
    if (searchInput.value.trim()) performTmdbSearch(searchInput.value);
    showToast("✅ Einstellungen gespeichert!");
  });

  // Google Sign-In / Sign-Out
  document.getElementById("googleSignInBtn").addEventListener("click", async () => {
    if (!state.authInstance) {
      showToast("⚠️ Bitte zuerst unten die Firebase Projekt-Konfiguration eintragen.");
      return;
    }
    const provider = new GoogleAuthProvider();
    provider.setCustomParameters({ prompt: "select_account" });

    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
    try {
      if (isMobile) {
        // Auf Mobilgeräten blockieren Browser oft Popups oder verlieren den Window-Context
        await signInWithRedirect(state.authInstance, provider);
      } else {
        try {
          await signInWithPopup(state.authInstance, provider);
          showToast("🎉 Erfolgreich mit Google-Konto verbunden!");
        } catch (popupErr) {
          if (popupErr.code === "auth/popup-blocked" || popupErr.code === "auth/cancelled-popup-request") {
            await signInWithRedirect(state.authInstance, provider);
          } else {
            throw popupErr;
          }
        }
      }
    } catch (err) {
      handleAuthError(err);
    }
  });

  document.getElementById("googleSignOutBtn").addEventListener("click", async () => {
    if (state.authInstance) {
      await signOut(state.authInstance);
      showToast("👋 Abgemeldet.");
    }
  });
});

// PWA Service Worker registrieren
if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("./sw.js").catch(() => {});
  });
}
