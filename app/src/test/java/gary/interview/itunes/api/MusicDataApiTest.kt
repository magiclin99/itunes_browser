package itunes.gary.interview.itunes.api

import gary.interview.itunes.api.MusicDataApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicDataApiTest {

    @Test
    fun testGetHotTrack() {

        runBlocking {
            val result = MusicDataApi().getHotTracks().await()
            assertEquals(50, result.feed.results.size)
        }

    }
}