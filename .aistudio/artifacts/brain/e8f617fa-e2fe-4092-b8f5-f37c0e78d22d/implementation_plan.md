# WatchMate TV — Mobile Web-Companion & Cloud-Sync

Architektur- und Umsetzungsplan für die mobile Erfassung von Film- und Serien-Empfehlungen per Smartphone-Browser und die nahtlose Echtzeit-Synchronisation mit dem Android TV über Firebase Firestore und Google Sign-In.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Basierend auf deinen Antworten wurden folgende architektonische Entscheidungen getroffen:

- **Bestätigte Entscheidung 1 (Eingabeweg)**: Web-App im mobilen Smartphone-Browser ohne Notwendigkeit einer separaten App-Installation.
- **Bestätigte Entscheidung 2 (Synchronisation)**: Cloud-Synchronisation über Firebase Firestore, abgesichert über das Google-Konto.
- **Offene Entscheidung / Empfehlung (Lokaler Offline-Cache)**: *(Empfohlen)* Die bestehende lokale Room-Datenbank bleibt als lokaler Offline-Cache auf dem TV aktiv und synchronisiert sich bidirektional mit Cloud Firestore, sodass die App auch ohne aktive Internetverbindung sofort blitzschnell startet und bedienbar bleibt.

---

## 1. Overview & Core Concept

- **Was die Funktion bietet**: Nutzer können unterwegs im Smartphone-Browser schnell nach Filmen und Serien suchen (über die TMDb-API) und diese mit einem Fingertipp zur persönlichen Watchlist hinzufügen. Zu Hause auf dem Android TV (Google TV) taucht der neu hinzugefügte Titel sofort in Echtzeit in der Kachelansicht auf.
- **Zielgruppe & Kontext**: TV-Zuschauer, die unterwegs von Freunden, Arbeitskollegen oder Social Media Film- und Serientipps erhalten und diese sofort ohne Notizzettel-Umweg direkt auf den Fernseher übertragen wollen.
- **Kernwert**: Beseitigt den Medienbruch zwischen „unterwegs Empfehlung bekommen“ und „abends auf der Couch suchen“.

---

## 2. User Experience & Visual Design

### 2.1 TV-Erlebnis (Google TV / Android TV)

1. **Schnelle Kopplung per QR-Code**:
   - Im Einstellungsmenü oder per Taste auf der Fernbedienung öffnet sich ein „Mobil koppeln“-Dialog.
   - Zeigt einen prominenten QR-Code und eine kurze URL zum Öffnen auf dem Smartphone.
   - Status-Anzeige: Zeigt an, ob das Google-Konto verbunden ist und wie viele Geräte synchronisiert sind.
2. **Echtzeit-Feedback**:
   - Wenn unterwegs ein Titel über das Handy hinzugefügt wird, erscheint auf dem TV dezent eine animierte Benachrichtigung (z. B. *„Dune: Part Two wurde von deinem Smartphone zur Watchlist hinzugefügt“*).
   - Der Eintrag reiht sich mit einer sanften Einblend-Animation direkt in der passenden Tab-Kategorie („Alle“ / „Serien“ / „Filme“) ein.

### 2.2 Mobile Web-Companion (Smartphone-Browser)

1. **Startseite & Schnelleingabe**:
   - Schlankes, touch-optimiertes Dark-Mode-Design im Stil von WatchMate TV (Farben: `#060A18`, Akzente: Cyan/Neon-Blau `#38BDF8` und Kinorot `#E11D48`).
   - Große Suchleiste ganz oben mit Sofortsuche über TMDb (Debounce 300ms).
   - Sofortige Anzeige von Filmplakat, Erscheinungsjahr, Altersfreigabe und verfügbaren Streaminganbietern (Netflix, Prime, Disney+ etc.).
2. **Hinzufügen mit einem Tap**:
   - Schnellwahltasten: „Auf die Watchlist“ (Status: Plan to Watch) oder „Jetzt anschauen“ (Watching).
   - Bei Serien: Automatische Übernahme der Staffel- und Episodenanzahl.
3. **Bestätigungs-Feedback**:
   - Haptisches Feedback (sofern vom Browser unterstützt) und visuelle Bestätigung: *„Gespeichert! Sofort auf deinem TV verfügbar.“*

---

## 3. Key Product Decisions & Trade-Offs

- **Entscheidung 1: Web-Companion vs. Native Mobile-App**:
  - *Gewählter Ansatz*: Mobile Web-App (PWA-kompatibel).
  - *Begründung*: Keine Installation aus dem Play Store oder Sideloading auf dem Smartphone nötig. Link oder QR-Code scannen, mit Google anmelden, fertig. Funktioniert auf jedem Smartphone (Android & iOS).
  - *Verworfene Alternative*: Native Smartphone-App (hoher Pflegeaufwand für zwei getrennte APKs, Hürde durch manuelle Installation).

- **Entscheidung 2: Hybride Datenhaltung (Room Local Cache + Firestore Cloud)**:
  - *Gewählter Ansatz*: Room als schneller On-Device-Cache auf dem TV + Cloud Firestore als Single Source of Truth.
  - *Begründung*: Der Fernseher behält seine extrem schnellen Boot- und Ladezeiten (auch im Standby oder bei schlechtem WLAN). Bei Netzwerkausfall bleibt die Watchlist voll les- und bearbeitbar und synchronisiert sich nach.
  - *Verworfene Alternative*: Rein Cloud-basiertes Firestore ohne lokalen SQLite-Cache (hätte Latenzen bei jedem TV-Start erzeugt).

- **Entscheidung 3: Authentifizierung mit Google Sign-In**:
  - *Gewählter Ansatz*: Google Sign-In über Jetpack `CredentialManager` auf dem TV und Standard-Google-Auth im Web-Companion.
  - *Begründung*: Auf Google TV ist ohnehin ein Google-Konto hinterlegt. Das Smartphone nutzt dasselbe Google-Konto für eine 1-Klick-Anmeldung.
  - *Verworfene Alternative*: E-Mail/Passwort-Eingabe (sehr umständlich mit der TV-Fernbedienung).

---

## 4. Technical Architecture & Data Strategy

### 4.1 Systemarchitektur & Datenfluss

```
┌───────────────────────────────────────────────────────────────┐
│                    Unterwegs (Smartphone)                     │
│                                                               │
│   Mobile Web-App (Browser)                                    │
│   ├── Google Sign-In (Firebase Auth)                          │
│   ├── TMDb API Direct Search                                  │
│   └── Firestore Write: /users/{userId}/watchlist/{mediaId}    │
└──────────────────────────────┬────────────────────────────────┘
                               │ HTTPS / WebSocket
                               ▼
┌───────────────────────────────────────────────────────────────┐
│                   Google Cloud / Firebase                     │
│                                                               │
│   Cloud Firestore                                             │
│   └── Collection: /users/{userId}/watchlist/{itemId}          │
│       ├── Security Rules: request.auth.uid == userId          │
│       └── Real-time Snapshot Event Stream                     │
└──────────────────────────────┬────────────────────────────────┘
                               │ Real-time Snapshot Listener
                               ▼
┌───────────────────────────────────────────────────────────────┐
│                    Zuhause (Android TV)                       │
│                                                               │
│   WatchMate TV App                                            │
│   ├── Google Sign-In (CredentialManager)                      │
│   ├── WatchlistSyncRepository (Firestore Listener)            │
│   ├── Room Local Database (WatchlistDao / Cache)              │
│   └── Jetpack Compose UI (Echtzeit-Update auf dem TV)         │
└───────────────────────────────────────────────────────────────┘
```

### 4.2 Datenmodell (`/users/{userId}/watchlist/{itemId}`)

```json
{
  "id": 1022789,
  "mediaType": "movie",
  "title": "Dune: Part Two",
  "posterPath": "/5gZZk6w00Y7iFzB3jYdY6n61G3.jpg",
  "backdropPath": "/xOMo8BRK7PfcJv9JCnx7s5hj0x2.jpg",
  "overview": "Paul Atreides vereinigt sich mit Chani...",
  "releaseDate": "2024-02-27",
  "voteAverage": 8.2,
  "status": "PLAN_TO_WATCH",
  "currentSeason": 1,
  "currentEpisode": 1,
  "totalSeasons": 0,
  "seasonEpisodeCount": "{}",
  "addedAt": 1730847600000,
  "lastWatchedAt": 0,
  "streamingProviders": "[\"prime\",\"wow\"]",
  "source": "mobile_web",
  "createdAt": "SERVER_TIMESTAMP",
  "updatedAt": "SERVER_TIMESTAMP"
}
```

### 4.3 Sicherheitsregeln (`firestore.rules`)

- Zero-Trust-Prinzip: Nur authentifizierte Benutzer können auf ihren eigenen Datenpfad `/users/{userId}/watchlist/{itemId}` zugreifen.
- Validierung aller Pflichtfelder (`title`, `mediaType`, `status`, `id`).

---

## 5. Implementierungsschritte

1. **Firebase Backend-Bereitstellung**:
   - Provisionierung von Cloud Firestore & Firebase Auth über das Firebase-RPC-System.
   - Konfiguration der `firestore.rules` und Bereitstellung der Google Sign-In Authentifizierung.
2. **Android TV Client Integration**:
   - Aktivieren der vorkonfigurierten Firebase Firestore- & Auth-Abhängigkeiten in `app/build.gradle.kts`.
   - Implementierung von `FirestoreSyncRepository`, das Änderungen bidirektional mit der lokalen Room-Datenbank synchronisiert.
   - Integration des QR-Code-Dialogs in den TV-Einstellungen zur bequemen Smartphone-Kopplung.
3. **Mobile Web-Companion**:
   - Bereitstellung einer leichten, hochgradig responsiven Single-Page-Web-App (gehostet via Firebase Hosting oder integrierte Web-Ressource).
   - Google Sign-In Button, TMDb-Suchfeld und 1-Tap Watchlist-Add.
4. **End-to-End Verifikation & APK-Generierung**:
   - Lokale Tests gegen den Firestore-Emulator (`RuleTest`).
   - Bauen der finalen APK und automatisches Ablegen als `WatchMate-TV.apk` im Repository-Hauptordner.
