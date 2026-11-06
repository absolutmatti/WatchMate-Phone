package com.example.data.remote

import com.example.data.local.WatchlistItem

object SampleDataProvider {

    fun getInitialWatchlist(): List<WatchlistItem> {
        val now = System.currentTimeMillis()
        return listOf(
            // TV Series: Severance (WATCHING - should be pinned at top)
            WatchlistItem(
                id = 95557,
                mediaType = "tv",
                title = "Severance",
                originalTitle = "Severance",
                overview = "Mark führt ein Team von Büroangestellten an, deren Erinnerungen chirurgisch zwischen ihrem Arbeits- und Privatleben aufgeteilt wurden. Als ein geheimnisvoller Kollege außerhalb der Arbeit auftaucht, beginnt eine Entdeckungsreise nach der Wahrheit.",
                posterPath = "/lE5p0fGqO3W5JgT5w7WcZ4Y9vT1.jpg",
                backdropPath = "/7q4hA9r7yL6K0V3l9V7z1Oq8B9F.jpg",
                releaseYear = "2022",
                voteAverage = 8.4,
                numberOfSeasons = 2,
                numberOfEpisodes = 19,
                genres = "Sci-Fi, Mystery, Drama, Thriller",
                status = WatchlistItem.STATUS_WATCHING,
                currentSeason = 1,
                currentEpisode = 4,
                seasonEpisodeCountsJson = """{"1":9,"2":10}""",
                addedAt = now - 86400000L * 5,
                lastWatchedAt = now - 3600000L * 2, // 2 hours ago -> Top watching
                trailerYoutubeKey = "xEQP4VVuyrY",
                primaryProviderName = "Apple TV+",
                primaryProviderLogo = "/6uhKBfmtzFqOcLousVO0ZNVdaSg.jpg",
                providersJson = """[{"name":"Apple TV+","logo":"/6uhKBfmtzFqOcLousVO0ZNVdaSg.jpg","type":"flatrate"},{"name":"Amazon Prime Video","logo":"/gMZdpavHmxFNnLpMHwVxfqeux2g.png","type":"flatrate"}]"""
            ),

            // TV Series: Stranger Things (WATCHING)
            WatchlistItem(
                id = 66732,
                mediaType = "tv",
                title = "Stranger Things",
                originalTitle = "Stranger Things",
                overview = "Als ein Junge spurlos verschwindet, kommen in einer Kleinstadt geheime Regierungsexperimente, übernatürliche Kräfte und ein seltsames kleines Mädchen ans Licht.",
                posterPath = "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
                backdropPath = "/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
                releaseYear = "2016",
                voteAverage = 8.6,
                numberOfSeasons = 4,
                numberOfEpisodes = 34,
                genres = "Sci-Fi, Drama, Mystery",
                status = WatchlistItem.STATUS_WATCHING,
                currentSeason = 2,
                currentEpisode = 5,
                seasonEpisodeCountsJson = """{"1":8,"2":9,"3":8,"4":9}""",
                addedAt = now - 86400000L * 10,
                lastWatchedAt = now - 3600000L * 8,
                trailerYoutubeKey = "b9EkMc79ZSU",
                primaryProviderName = "Netflix",
                primaryProviderLogo = "/rK1KljqmbvO9HQa1PBFLILWah72.png",
                providersJson = """[{"name":"Netflix","logo":"/rK1KljqmbvO9HQa1PBFLILWah72.png","type":"flatrate"}]"""
            ),

            // Movie: Dune: Part Two (PLAN_TO_WATCH)
            WatchlistItem(
                id = 693134,
                mediaType = "movie",
                title = "Dune: Part Two",
                originalTitle = "Dune: Part Two",
                overview = "Paul Atreides verbündet sich mit Chani und den Fremen auf dem Rachefeldzug gegen die Verschwörer, die seine Familie vernichtet haben. Angesichts der Wahl zwischen der Liebe seines Lebens und dem Schicksal des Universums versucht er, eine schreckliche Zukunft zu verhindern.",
                posterPath = "/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
                backdropPath = "/xOMo8BRK7PfcJv9JCnx7s520fff.jpg",
                releaseYear = "2024",
                voteAverage = 8.2,
                runtime = 166,
                genres = "Sci-Fi, Abenteuer",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                addedAt = now - 86400000L * 2,
                lastWatchedAt = 0,
                trailerYoutubeKey = "Way9Dexny3w",
                primaryProviderName = "Netflix",
                primaryProviderLogo = "/rK1KljqmbvO9HQa1PBFLILWah72.png",
                providersJson = """[{"name":"Netflix","logo":"/rK1KljqmbvO9HQa1PBFLILWah72.png","type":"flatrate"},{"name":"RTL+","logo":"/er0P3HwuVruKgnOqF8CoNdAAPPD.png","type":"flatrate"},{"name":"Amazon Prime Video","logo":"/gMZdpavHmxFNnLpMHwVxfqeux2g.png","type":"flatrate"},{"name":"Apple TV Store","logo":"/qdEGArH3lKfFnAtYXMkSYk5wxuG.png","type":"buy_rent"},{"name":"Amazon Video","logo":"/jn6TLbtaTZntTRX9UYucHJvpQx1.png","type":"buy_rent"},{"name":"YouTube","logo":"/5Maob4o5w8oZnNeYpCDyVFD3M7X.png","type":"buy_rent"}]"""
            ),

            // Movie: Oppenheimer (PLAN_TO_WATCH)
            WatchlistItem(
                id = 872585,
                mediaType = "movie",
                title = "Oppenheimer",
                originalTitle = "Oppenheimer",
                overview = "Die Geschichte des brillanten Physikers J. Robert Oppenheimer und seiner bahnbrechenden Rolle bei der Entwicklung der Atombombe im Rahmen des Manhattan-Projekts während des Zweiten Weltkriegs.",
                posterPath = "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                backdropPath = "/nb3xI8XI3w4pMVZ38VijbsyBqP4.jpg",
                releaseYear = "2023",
                voteAverage = 8.1,
                runtime = 180,
                genres = "Drama, Historie",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                addedAt = now - 86400000L * 3,
                lastWatchedAt = 0,
                trailerYoutubeKey = "uYPbbksJxIg",
                primaryProviderName = "WOW / Sky",
                primaryProviderLogo = "/6uhKBfmtzFqOcLousVO0ZNVdaSg.jpg",
                providersJson = """[{"name":"WOW / Sky","logo":"/6uhKBfmtzFqOcLousVO0ZNVdaSg.jpg","type":"flatrate"},{"name":"Amazon Prime Video","logo":"/gMZdpavHmxFNnLpMHwVxfqeux2g.png","type":"flatrate"},{"name":"Apple TV Store","logo":"/qdEGArH3lKfFnAtYXMkSYk5wxuG.png","type":"buy_rent"},{"name":"Amazon Video","logo":"/jn6TLbtaTZntTRX9UYucHJvpQx1.png","type":"buy_rent"}]"""
            ),

            // TV Series: Arcane (PLAN_TO_WATCH)
            WatchlistItem(
                id = 94605,
                mediaType = "tv",
                title = "Arcane",
                originalTitle = "Arcane",
                overview = "Vor dem Hintergrund des schwelenden Konflikts zwischen der reichen Utopie Piltover und der unterdrückten Unterstadt Zhaun kämpfen zwei Schwestern auf entgegengesetzten Seiten eines Krieges zwischen magischen Technologien und unvereinbaren Überzeugungen.",
                posterPath = "/fqldf2t8ztc9aiwn3k6mlX3tvRT.jpg",
                backdropPath = "/7tNtpZ07m585nZg19w3C8dKx96S.jpg",
                releaseYear = "2021",
                voteAverage = 9.0,
                numberOfSeasons = 2,
                numberOfEpisodes = 18,
                genres = "Animation, Sci-Fi, Action",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                currentSeason = 1,
                currentEpisode = 0,
                seasonEpisodeCountsJson = """{"1":9,"2":9}""",
                addedAt = now - 86400000L * 4,
                lastWatchedAt = 0,
                trailerYoutubeKey = "fXmAurh012s",
                primaryProviderName = "Netflix",
                primaryProviderLogo = "/rK1KljqmbvO9HQa1PBFLILWah72.png",
                providersJson = """[{"name":"Netflix","logo":"/rK1KljqmbvO9HQa1PBFLILWah72.png","type":"flatrate"}]"""
            ),

            // Movie: Inception (WATCHED)
            WatchlistItem(
                id = 27205,
                mediaType = "movie",
                title = "Inception",
                originalTitle = "Inception",
                overview = "Dom Cobb ist ein geschickter Dieb, der die wertvollen Geheimnisse anderer aus den Tiefen des Unterbewusstseins während der Traumphase stiehlt. Nun erhält er die Chance auf eine letzte Mission zur Vergebung: Nicht einen Gedanken stehlen, sondern einen einpflanzen.",
                posterPath = "/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                backdropPath = "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
                releaseYear = "2010",
                voteAverage = 8.4,
                runtime = 148,
                genres = "Action, Sci-Fi, Abenteuer",
                status = WatchlistItem.STATUS_WATCHED,
                addedAt = now - 86400000L * 20,
                lastWatchedAt = now - 86400000L * 15,
                trailerYoutubeKey = "YoHD9XEInc0",
                primaryProviderName = "Amazon Prime Video",
                primaryProviderLogo = "/gMZdpavHmxFNnLpMHwVxfqeux2g.png",
                providersJson = """[{"name":"Amazon Prime Video","logo":"/gMZdpavHmxFNnLpMHwVxfqeux2g.png","type":"flatrate"},{"name":"RTL+","logo":"/er0P3HwuVruKgnOqF8CoNdAAPPD.png","type":"flatrate"},{"name":"Apple TV Store","logo":"/qdEGArH3lKfFnAtYXMkSYk5wxuG.png","type":"buy_rent"},{"name":"Amazon Video","logo":"/jn6TLbtaTZntTRX9UYucHJvpQx1.png","type":"buy_rent"},{"name":"YouTube","logo":"/5Maob4o5w8oZnNeYpCDyVFD3M7X.png","type":"buy_rent"}]"""
            ),

            // TV Series: Breaking Bad (WATCHED)
            WatchlistItem(
                id = 1396,
                mediaType = "tv",
                title = "Breaking Bad",
                originalTitle = "Breaking Bad",
                overview = "Ein an Lungenkrebs erkrankter Chemielehrer beginnt zusammen mit einem ehemaligen Schüler Crystal Meth zu kochen, um die finanzielle Zukunft seiner Familie abzusichern.",
                posterPath = "/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
                backdropPath = "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
                releaseYear = "2008",
                voteAverage = 8.9,
                numberOfSeasons = 5,
                numberOfEpisodes = 62,
                genres = "Drama, Krimi",
                status = WatchlistItem.STATUS_WATCHED,
                currentSeason = 5,
                currentEpisode = 16,
                seasonEpisodeCountsJson = """{"1":7,"2":13,"3":13,"4":13,"5":16}""",
                addedAt = now - 86400000L * 30,
                lastWatchedAt = now - 86400000L * 25,
                trailerYoutubeKey = "HhesaQXLuRY",
                primaryProviderName = "Netflix",
                primaryProviderLogo = "/rK1KljqmbvO9HQa1PBFLILWah72.png",
                providersJson = """[{"name":"Netflix","logo":"/rK1KljqmbvO9HQa1PBFLILWah72.png","type":"flatrate"},{"name":"Apple TV Store","logo":"/qdEGArH3lKfFnAtYXMkSYk5wxuG.png","type":"buy_rent"},{"name":"Amazon Video","logo":"/jn6TLbtaTZntTRX9UYucHJvpQx1.png","type":"buy_rent"}]"""
            )
        )
    }

    // Curated search fallback if no API key or search fails
    fun searchSampleCatalog(query: String): List<WatchlistItem> {
        val q = query.trim().lowercase()
        val all = getInitialWatchlist() + listOf(
            WatchlistItem(
                id = 157336,
                mediaType = "movie",
                title = "Interstellar",
                originalTitle = "Interstellar",
                overview = "Eine Gruppe von Forschern bricht durch ein neu entdecktes Wurmloch auf, um das Überleben der Menschheit auf neuen Welten zu sichern.",
                posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                backdropPath = "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                releaseYear = "2014",
                voteAverage = 8.4,
                runtime = 169,
                genres = "Abenteuer, Drama, Sci-Fi",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                trailerYoutubeKey = "zSWdZVtXT7E",
                primaryProviderName = "Amazon Prime Video"
            ),
            WatchlistItem(
                id = 100088,
                mediaType = "tv",
                title = "The Last of Us",
                originalTitle = "The Last of Us",
                overview = "Zwanzig Jahre nach dem Ausbruch eines tödlichen Pilzbefalls muss ein abgebrühter Überlebender ein 14-jähriges Mädchen durch eine postapokalyptische USA schmuggeln.",
                posterPath = "/uKvVjHNqB5VmOrdxqAt2V7JMrHG.jpg",
                backdropPath = "/uDgy6hyPd82kOHh6I95FLtLnj6p.jpg",
                releaseYear = "2023",
                voteAverage = 8.6,
                numberOfSeasons = 1,
                numberOfEpisodes = 9,
                genres = "Drama, Sci-Fi, Abenteuer",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                currentSeason = 1,
                currentEpisode = 0,
                seasonEpisodeCountsJson = """{"1":9}""",
                trailerYoutubeKey = "uLtkt8BonwM",
                primaryProviderName = "WOW / Sky"
            ),
            WatchlistItem(
                id = 70523,
                mediaType = "tv",
                title = "Dark",
                originalTitle = "Dark",
                overview = "Das Verschwinden zweier Kinder in einer deutschen Kleinstadt bringt die brüchigen Beziehungen und Doppelleben von vier Familien ans Licht.",
                posterPath = "/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg",
                backdropPath = "/3lBDg3i6nn5R2NKICJ7KBvH50bX.jpg",
                releaseYear = "2017",
                voteAverage = 8.4,
                numberOfSeasons = 3,
                numberOfEpisodes = 26,
                genres = "Sci-Fi, Mystery, Drama",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                currentSeason = 1,
                currentEpisode = 0,
                seasonEpisodeCountsJson = """{"1":10,"2":8,"3":8}""",
                trailerYoutubeKey = "rrwycJ08PSA",
                primaryProviderName = "Netflix"
            ),
            WatchlistItem(
                id = 106379,
                mediaType = "tv",
                title = "Fallout",
                originalTitle = "Fallout",
                overview = "In einer zukünftigen, postapokalyptischen Version von Los Angeles nach einem Atomkrieg müssen die Bürger in unterirdischen Bunkern leben, um sich vor Strahlung und Mutanten zu schützen.",
                posterPath = "/AnsInterface93.jpg",
                backdropPath = "/2meX1nMdScFOoV4370rqHWIOqaY.jpg",
                releaseYear = "2024",
                voteAverage = 8.3,
                numberOfSeasons = 1,
                numberOfEpisodes = 8,
                genres = "Action, Abenteuer, Sci-Fi",
                status = WatchlistItem.STATUS_PLAN_TO_WATCH,
                currentSeason = 1,
                currentEpisode = 0,
                seasonEpisodeCountsJson = """{"1":8}""",
                trailerYoutubeKey = "V-mugKDQDlg",
                primaryProviderName = "Amazon Prime Video"
            )
        )
        return all.filter {
            it.title.lowercase().contains(q) ||
                    (it.originalTitle?.lowercase()?.contains(q) == true) ||
                    it.genres.lowercase().contains(q)
        }
    }
}
