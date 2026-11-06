# WatchMate TV ↔ Web-Companion: Bidirektionaler Sync & Kopplung

Vollständige Implementierungs-Architektur zur nahtlosen Verbindung der Android-TV-App (WatchMate TV) und der mobilen Web-Companion-App über bidirektionalen Room- und Firebase-Firestore-Sync, TV-Kopplungs-Overlay mit QR-Code und lokalen Test-Server.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **In Phase 1 bestätigte Kernentscheidungen:**
> - **Synchronisations-Modell:** Bidirektionaler Sync zwischen lokaler Room-Datenbank (schneller Offline-First-Zugriff auf dem TV) und Firebase Firestore (Cloud-Kopplung mit der Web-App).
> - **Kopplung & QR-Code:** Zentraler TV-Kopplungsdialog aufrufbar über die TV-Top-Bar (Smartphone-Icon) sowie über den Einstellungsbereich.
> - **Web-App Bereitstellung:** Lokaler Webserver-Startbefehl für direktes Testen im Browser, kompatibel mit Development-URLs und Smartphone-Aufrufen.

---

## 1. Overview & Core Concept

- **Was geliefert wird:**
  1. **TV-Kopplungs-Feature (QR-Code & URL):** Der Fernseher generiert einen dynamischen QR-Code auf Basis der Web-App-URL inklusive hinterlegter TMDb-Parameter (`?tmdb_key=...&region=DE`). Das Smartphone scannt den Code, öffnet sofort den Companion und übernimmt die Einstellungen.
  2. **Bidirektionaler Sync (Room ↔ Firestore):**
     - Auf dem Smartphone in der Web-App hinzugefügte oder aktualisierte Titel erscheinen in Echtzeit auf dem TV-Bildschirm.
     - Auf dem TV gesehene Folgen oder Statusänderungen werden automatisch in die Cloud zurückgespielt.
  3. **Google Sign-In auf Android TV:** Authentifizierung über Jetpack Credential Manager / Firebase Auth, damit TV und Web-App denselben Firestore-Nutzerpfad (`/users/{userId}/watchlist/{itemId}`) teilen.
  4. **Web-App Server:** Einfacher Node/Python-Entwicklungs-Server für den `web-companion/`-Ordner zum direkten Ausprobieren im Browser.

---

## 2. User Experience & Visual Design

### TV-Ablauf & Interaktion:
1. **Top-Bar & Einstellungen:**
   - In der TV-Top-Bar erscheint neben dem Einstellungs-Icon ein neues Smartphone-Kopplungs-Icon (`ic_phone_android` / `QrCode2`).
   - Mit der Fernbedienung (D-Pad) wählbar mit sanftem Z-Ebenen-Fokus-Effekt und Cyan-Glow.
2. **Kopplungs-Dialog:**
   - Großformatiger, gestochen scharfer QR-Code in der Bildschirmmitte (generiert per Canvas/ZXing-Algorithmus ohne externe unsichere Web-Dienste).
   - Anzeige des Anmeldestatus (z. B. "Angemeldet als: max@gmail.com" oder "Mit Google anmelden").
   - Schritt-für-Schritt-Kurzanleitung: "1. Kamera öffnen, 2. QR-Code scannen, 3. Titel vom Sofa aus auf den TV senden".

### Web-Companion Ablauf:
1. Nach dem Scannen öffnet sich die Web-App (`web-companion/index.html`).
2. TMDb-Schlüssel und Region werden automatisch aus der URL in den `localStorage` geschrieben.
3. Nach Google-Login (`Mit Google anmelden`) verbindet sich die Web-App sofort mit demselben Firestore-Dokumentenbestand.

---

## 3. Key Product Decisions & Trade-Offs

- **Entscheidung 1: Room beibehalten vs. Reines Firestore**
  - *Gewählter Ansatz:* Room bleibt die primäre Single-Source-of-Truth für die TV-UI; ein Synchronisations-Manager spiegelt Änderungen in Firestore und empfängt Snapshots.
  - *Warum:* Garantiert 60 FPS Ruckelfreiheit auf dem Fernseher, sofortiges Laden ohne Lade-Spinner beim Kaltstart und vollständige Offline-Funktionalität bei Internetabbrüchen.
- **Entscheidung 2: Lokale QR-Code Generierung**
  - *Gewählter Ansatz:* Lokaler Bitmatrix-Renderer im Android TV-Client.
  - *Warum:* Keine externen API-Abhängigkeiten, funktioniert verzögerungsfrei und schützt sensible Token vor Drittanbieter-Logging.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   Mobile Web-Companion                 │
│              (PWA: index.html / app.js)                │
│   - Sucht Titel über TMDb API                          │
│   - Speichert in Firestore: /users/{uid}/watchlist/    │
└───────────────────────────┬────────────────────────────┘
                            │
                   Firebase Firestore
               (/users/{uid}/watchlist/{id})
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Android TV App                      │
│                                                        │
│  ┌──────────────────┐           ┌──────────────────┐   │
│  │ FirestoreSync    │◄─────────►│  WatchlistDao    │   │
│  │ Repository       │           │  (Room SQLite)   │   │
│  └────────┬─────────┘           └────────┬─────────┘   │
│           │                              │             │
│  ┌────────▼──────────────────────────────▼─────────┐   │
│  │               WatchlistViewModel                │   │
│  └────────────────────────┬────────────────────────┘   │
│                           │                            │
│  ┌────────────────────────▼────────────────────────┐   │
│  │ Compose TV UI (HomeScreen, DetailScreen, TopBar)│   │
│  │ + TvPairingDialog (QR-Code Generator)           │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

### Konkrete Implementierungs-Schritte:

1. **Firebase Backend Setup (`set_up_firebase`):**
   - Firestore Provisionierung und Google Sign-In Konfiguration über den Plattform-Handshake.
   - Bereitstellung von `firestore.rules` und `firebase-blueprint.json`.
2. **Android TV Data-Layer Erweiterung:**
   - Aktivierung der Firebase-Dependencies in `app/build.gradle.kts` (`firebase-firestore`, `firebase-auth`, `credentials`).
   - Implementierung des `FirestoreSyncManager` / `WatchlistSyncRepository`, der Room mit Firestore abgleicht (`onSnapshot`-Listener für eingehende Web-Items und lokaler DB-Observer für ausgehende TV-Status-Updates).
3. **QR-Code Generator & Dialog:**
   - Erstellung des `TvPairingDialog` in `TvDialogs.kt` mit hochauflösendem QR-Code.
   - Kopplung in `HomeScreen.kt` / `TopBar` integrieren.
4. **Webserver-Start:**
   - Bereitstellung eines lokalen HTTP-Servers für `web-companion/` (Port 8000), um die PWA sofort im Browser öffnen und testen zu können.
