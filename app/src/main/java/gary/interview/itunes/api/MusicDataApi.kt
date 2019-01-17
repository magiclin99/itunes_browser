package gary.interview.itunes.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MusicDataApi {

    private val rssApi: RssApi

    private var itunesApi: ItunesApi

    init {
        val okHttp = OkHttpClient()

        val mapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        rssApi = Retrofit.Builder()
            .client(okHttp)
            .baseUrl("https://rss.itunes.apple.com/api/v1/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(RssApi::class.java)

        itunesApi = Retrofit.Builder()
            .client(okHttp)
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(ItunesApi::class.java)

    }

    fun getHotTracks() = rssApi.getHotTrack()

    fun getTrackDetail(id: String) = itunesApi.getDetail(id)

    fun search(keyword: String) = itunesApi.search(keyword)

    interface RssApi {
        @GET("tw/itunes-music/hot-tracks/all/50/explicit.json")
        fun getHotTrack(): Deferred<GetHotTrackResponse>
    }

    interface ItunesApi {
        @GET("lookup")
        fun getDetail(@Query("id") id: String): Deferred<GetDetailResponse>

        @GET("/search?media=music&attribute=songTerm&country=tw")
        fun search(@Query("term") keyword: String): Deferred<SearchResponse>
    }

    class GetHotTrackResponse {
        lateinit var feed: Feed
    }

    class Feed {
        lateinit var results: Array<Track>
    }

    class Track {
        lateinit var id: String
        lateinit var artistName: String
        lateinit var name: String
        lateinit var artworkUrl100: String
        lateinit var previewUrl: String

        fun setTrackId(id: String) {
            this.id = id
        }

        fun setTrackName(name: String) {
            this.name = name
        }
    }

    class SearchResponse {
        lateinit var results: Array<Track>
    }

    class GetDetailResponse {
        lateinit var results: Array<Track>
    }


}