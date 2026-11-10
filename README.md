# 📺 WatchMate TV

> **Die smarte, lokale Film- und Serien-Watchlist für Android TV & Google TV.**  
> Entwickelt mit modernstem **Kotlin & Jetpack Compose for TV**, nativer Fernbedienungs-Navigation (D-Pad), TMDb-Echtzeitsuche und automatischer Erkennung deiner Streaming-Abonnements (Netflix, Prime, Disney+, WOW u.v.m.).

---

![Android TV](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Google%20TV-blue?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-green.svg)

---

## 🌟 Highlights & Funktionen

- 🎮 **100% D-Pad-optimiert (Fernbedienung):** Speziell für die Couch-Experience auf TV-Bildschirmen (z. B. Philips OLED, Sony Bravia, Chromecast mit Google TV, Nvidia Shield). Flüssige Spring-Animationen, prominente Gold-Rahmen und Z-Achsen-Fokus ohne abgeschnittene Kanten.
- 🎬 **TMDb-Suche & Metadaten:** Durchsuche Millionen von Filmen und TV-Serien mit weltweiten Metadaten: Offizielle deutsche Inhaltsangaben, Veröffentlichungsjahre, Bewertungen, hochauflösende Filmplakate und Trailer (YouTube).
- 🍿 **Streaming-Anbieter & Abo-Erkennung:**
  - Automatische Anzeige, wo ein Film oder eine Serie in deiner Region (**Deutschland 🇩🇪, Österreich 🇦🇹, Schweiz 🇨🇭**) gestreamt werden kann.
  - **Eigene Abonnements filtern:** Markiere deine aktiven Anbieter (Netflix, Amazon Prime Video, Disney+, WOW / Sky, Apple TV+, Paramount+, RTL+, Joyn, MagentaTV, Crunchyroll). WatchMate TV hebt passende Flatrates farblich hervor.
- 🚀 **Direkter App-Start (Deep Links):** Öffne den ausgewählten Titel mit einem Klick direkt in der installierten Streaming-App auf deinem TV.
- 📺 **Serien- & Episoden-Tracker:**
  - Verfolge deinen aktuellen Stand (z. B. *Staffel 2, Episode 5*).
  - Ein Klick auf *"Nächste Episode"* zählt automatisch weiter und wechselt nahtlos zur nächsten Staffel.
  - Dialog zur Fertigstellung beim Erreichen des Serienfinales.
- 🎲 **"Überrasch mich" & Filter:** Finde blitzschnell den passenden Film für den Abend nach Genre, maximaler Laufzeit (z. B. < 90 Min.) oder lass den Zufallsgenerator entscheiden.
- 🔒 **100% Lokal & Datenschutzfreundlich:** Alle Listen und der Serienfortschritt werden in einer lokalen **Room-SQLite-Datenbank** auf deinem TV gespeichert. Kein Login, kein Tracking, kein Cloud-Zwang.
- 📥 **Beispieldaten mit 1 Klick:** Starte sofort durch – auf Knopfdruck lässt sich eine kuratierte Liste beliebter Titel (*Severance, Stranger Things, Dune 2, Arcane, Inception, Breaking Bad*) laden.

---

## 📸 Screenshots

| Hauptübersicht & Fokus-Navigation | Filter & Streaming-Anbieter |
| :---: | :---: |
| ![WatchMate TV Home](public/assets/aistudio/IMG_4139_caf99b71.jpeg) | ![WatchMate TV Einstellungen](public/assets/aistudio/IMG_4140_ef6d04bb.jpeg) |

---

## 📥 Installation (APK Download)

Die vorab kompilierte Installationsdatei befindet sich direkt im Hauptverzeichnis dieses Repositories:

➡️ **[`WatchMate-TV.apk`](./WatchMate-TV.apk)** (oder [`app-debug.apk`](./app-debug.apk))

### Installation auf Android TV / Google TV:
1. Lade die Datei `WatchMate-TV.apk` herunter.
2. Installiere die APK auf deinem Android TV über eine der folgenden Methoden:
   - **Per USB-Stick:** Kopiere die Datei auf einen USB-Stick und öffne sie auf dem TV mit einem Datei-Manager (z. B. *File Commander* oder *AnExplorer*).
   - **Per App "Send Files to TV":** Übertrage die APK vom Smartphone/PC direkt an den TV.
   - **Per ADB (Kabellos / USB):**
     ```bash
     adb connect <TV-IP-ADRESSE>:5555
     adb install -r WatchMate-TV.apk
     ```

---

## 📱 Mobile Web-Companion (PWA)

Der **WatchMate TV Web-Companion** ist eine mobile Progressive Web App (PWA), mit der du bequem vom Smartphone aus nach Filmen & Serien suchen und diese mit einem einzigen Klick direkt auf deine Android TV Watchlist senden kannst.

🔗 **Live auf GitHub Pages:** [`https://absolutmatti.github.io/WatchMate-Phone/web-companion/`](https://absolutmatti.github.io/WatchMate-Phone/web-companion/)  
📂 **Quellcode im Repository:** [`/web-companion/`](./web-companion/)

### 🚀 Highlights des Web-Companions:
- 📱 **Kein App-Store-Download nötig:** Sofort im mobilen Browser (Safari, Chrome, Firefox) als PWA auf dem Homescreen installierbar.
- 🎬 **Kinoreife Detailansicht:** Klick auf eine Film- oder Serien-Karte öffnet das hochauflösende Backdrop-Szenenbild, deutsche Inhaltsangabe, Genres, Altersfreigabe (FSK), Laufzeit bzw. Staffelanzahl und direkte YouTube-Trailer-Verlinkung.
- 🔄 **Status auf dem TV ändern:** Wähle direkt in der Web-App zwischen *Noch nicht gesehen*, *Gerade am ansehen*, *Gesehen* und *Abgebrochen* – der Status synchronisiert sich in Echtzeit mit deinem Android TV!
- 📺 **Intelligenter Episoden-Tracker:** Verfolge und steuere deine Serienfolgen (*+1 Folge* / *-1 Folge*) bequem vom Sofa aus auf dem Smartphone.
- ⚡ **TMDb-Echtzeitsuche:** Schnelle Suche nach Filmen und TV-Serien mit weltweiten Metadaten und deutscher FSK-Einstufung.
- 🍿 **Streaming-Anbieter (DE 🇩🇪, AT 🇦🇹, CH 🇨🇭):** Zeigt sofort an, wo ein Titel (im Abo, kostenlos oder zum Leihen/Kaufen) verfügbar ist.
- 🔄 **Echtzeit-Synchronisation (Firebase Firestore):** Synchronisiert Änderungen nahtlos zwischen Android TV und deinem Smartphone.
- 📶 **Offline-Fähig:** Funktioniert dank Service Worker auch bei schlechter Internetverbindung.

---

### 📲 TV-Kopplung per QR-Code

1. Öffne auf dem Android TV die **WatchMate TV** App.
2. Klicke in der oberen Leiste auf das Smartphone-Symbol 📱 (oder gehe in die **Einstellungen ⚙️ ➔ Web-Companion Kopplung**).
3. Richte deine Smartphone-Kamera auf den angezeigten QR-Code.
4. Der QR-Code übergibt automatisch deinen TMDb-API-Key und deine Streaming-Region (`?tmdb_key=...&region=DE`) an die Web-App, sodass du sofort loslegen kannst!

---

### 🔐 Google-Login & Firebase Authorized Domains (Wichtig für GitHub Pages)

Damit der Cloud-Sync zwischen TV und Smartphone funktioniert, melden sich beide Geräte mit demselben Google-Konto an.

> [!IMPORTANT]
> **Einmalige Freischaltung für GitHub Pages:**  
> Aus Sicherheitsgründen erlaubt Google OAuth nur Anmeldungen von autorisierten Domains. Wenn du die Web-App über GitHub Pages (`absolutmatti.github.io`) aufrufst, muss die Domain einmalig in deiner Firebase Console freigeschaltet werden:
>
> 1. Öffne die [Firebase Console](https://console.firebase.google.com).
> 2. Wähle dein Projekt (`gen-lang-client-0275433631`).
> 3. Navigiere zu **Build** ➔ **Authentication** ➔ Tab **Settings** (Einstellungen).
> 4. Klicke links auf **Authorized domains** (Autorisierte Domains).
> 5. Klicke auf **Add domain** (Domain hinzufügen) und trage `absolutmatti.github.io` ein.
> 6. Klicke auf **Save**. Ab jetzt funktioniert der Google-Login auf deinem Smartphone reibungslos!

---

## 🛠️ Tech-Stack & Architektur

Die App wurde von Grund auf nach modernen Android- und Google-TV-Best-Practices entwickelt:

- **Architektur:** MVVM (Model-View-ViewModel) mit sauberer Trennung von UI, Repository und Data-Sources.
- **UI Toolkit:** Jetpack Compose (Material 3 for TV) mit `tv-foundation` und responsiven LazyGrids.
- **Asynchronität:** Kotlin Coroutines & `StateFlow` / `collectAsStateWithLifecycle`.
- **Lokale Datenbank:** Android Jetpack Room (SQLite) mit automatischen Migrationen.
- **Netzwerk & API:** Retrofit 2 mit Moshi-JSON-Converter und OkHttp-Logging.
- **Bildanzeige:** Coil (AsyncImage) mit integriertem Bitmap-Caching.
- **Konfiguration:** Secrets Gradle Plugin für API-Keys via `.env` und `.env.example`.

---

## ⚙️ TMDb API-Konfiguration (Optional)

WatchMate TV enthält bereits vorkonfigurierte Beispieldaten und einen kuratierten Offline-Katalog. Für unbegrenzte weltweite Live-Suchen und tagesaktuelle Streaming-Rechte:

1. Hole dir einen kostenlosen API-Key auf [themoviedb.org](https://www.themoviedb.org/).
2. Trage ihn entweder direkt in der TV-App unter **Einstellungen ⚙️ ➔ TMDb API-Konfiguration** ein oder lege ihn vor dem Bauen in einer `.env`-Datei an:
   ```properties
   TMDB_API_KEY=dein_api_key_hier
   TMDB_READ_ACCESS_TOKEN=dein_access_token_hier
   ```

---

## 💻 Selbst bauen (Entwicklung)

### Voraussetzungen
- Android Studio Ladybug / Meerkat oder neuer
- JDK 17 oder JDK 21
- Android SDK 36 (minSdk 24)

### Build per Befehlszeile:
```bash
# Debug-Build erstellen
gradle assembleDebug

# Tests ausführen
gradle :app:testDebugUnitTest
```
Die erzeugte APK liegt anschließend unter `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Lizenz

Dieses Projekt ist unter der [MIT License](LICENSE) lizenziert.
Metadaten und Bilder werden über die [TMDb API](https://www.themoviedb.org/) bereitgestellt (dieses Produkt nutzt die TMDb API, wird aber von TMDb weder unterstützt noch zertifiziert).
