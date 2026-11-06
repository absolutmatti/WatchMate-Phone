package com.example

import com.example.data.local.WatchlistItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistLogicTest {

    @Test
    fun testEpisodeAdvancementLogic() {
        val seasonMap = mapOf(1 to 8, 2 to 10)
        var season = 1
        var episode = 7
        val totalSeasons = 2

        // S01 E07 -> S01 E08
        if (episode < (seasonMap[season] ?: 10)) {
            episode++
        }
        assertEquals(8, episode)
        assertEquals(1, season)

        // S01 E08 -> S02 E01
        var isFinished = false
        if (episode < (seasonMap[season] ?: 10)) {
            episode++
        } else if (season < totalSeasons) {
            season++
            episode = 1
        } else {
            isFinished = true
        }
        assertEquals(2, season)
        assertEquals(1, episode)
        assertFalse(isFinished)

        // Jump to end: S02 E10
        season = 2
        episode = 10
        if (episode < (seasonMap[season] ?: 10)) {
            episode++
        } else if (season < totalSeasons) {
            season++
            episode = 1
        } else {
            isFinished = true
        }
        assertTrue(isFinished)
    }

    @Test
    fun testSortingRules() {
        val now = 1000000L
        val itemWatchingOld = WatchlistItem(
            id = 1,
            mediaType = "tv",
            title = "Watching Old",
            status = WatchlistItem.STATUS_WATCHING,
            addedAt = 100L,
            lastWatchedAt = now - 5000L
        )
        val itemWatchingRecent = WatchlistItem(
            id = 2,
            mediaType = "tv",
            title = "Watching Recent",
            status = WatchlistItem.STATUS_WATCHING,
            addedAt = 200L,
            lastWatchedAt = now - 1000L
        )
        val itemPlanRecent = WatchlistItem(
            id = 3,
            mediaType = "movie",
            title = "Plan Recent",
            status = WatchlistItem.STATUS_PLAN_TO_WATCH,
            addedAt = 500L,
            lastWatchedAt = 0
        )
        val itemPlanOld = WatchlistItem(
            id = 4,
            mediaType = "movie",
            title = "Plan Old",
            status = WatchlistItem.STATUS_PLAN_TO_WATCH,
            addedAt = 150L,
            lastWatchedAt = 0
        )

        val list = listOf(itemPlanOld, itemWatchingOld, itemPlanRecent, itemWatchingRecent)

        // Watchlist sorting rule:
        // Watching titles pinned at top sorted by lastWatchedAt DESC,
        // followed by Plan To Watch sorted by addedAt DESC
        val watching = list.filter { it.status == WatchlistItem.STATUS_WATCHING }
            .sortedByDescending { it.lastWatchedAt }
        val other = list.filter { it.status != WatchlistItem.STATUS_WATCHING }
            .sortedByDescending { it.addedAt }

        val sorted = watching + other

        assertEquals("Watching Recent", sorted[0].title)
        assertEquals("Watching Old", sorted[1].title)
        assertEquals("Plan Recent", sorted[2].title)
        assertEquals("Plan Old", sorted[3].title)
    }

    @Test
    fun testTabFilteringRules() {
        val items = listOf(
            WatchlistItem(id = 1, mediaType = "movie", title = "Movie 1", status = WatchlistItem.STATUS_PLAN_TO_WATCH),
            WatchlistItem(id = 2, mediaType = "tv", title = "Series 1", status = WatchlistItem.STATUS_WATCHING),
            WatchlistItem(id = 3, mediaType = "movie", title = "Movie Watched", status = WatchlistItem.STATUS_WATCHED),
            WatchlistItem(id = 4, mediaType = "tv", title = "Series Dropped", status = WatchlistItem.STATUS_DROPPED)
        )

        // "Alle" tab excludes WATCHED and DROPPED
        val allTab = items.filter {
            it.status != WatchlistItem.STATUS_WATCHED && it.status != WatchlistItem.STATUS_DROPPED
        }
        assertEquals(2, allTab.size)
        assertTrue(allTab.any { it.id == 1L })
        assertTrue(allTab.any { it.id == 2L })

        // "Gesehen" tab includes only WATCHED
        val watchedTab = items.filter { it.status == WatchlistItem.STATUS_WATCHED }
        assertEquals(1, watchedTab.size)
        assertEquals("Movie Watched", watchedTab[0].title)

        // "Abgebrochen" tab includes only DROPPED
        val droppedTab = items.filter { it.status == WatchlistItem.STATUS_DROPPED }
        assertEquals(1, droppedTab.size)
        assertEquals("Series Dropped", droppedTab[0].title)
    }
}
