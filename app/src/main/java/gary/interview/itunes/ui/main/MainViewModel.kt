package gary.interview.itunes.ui.main

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gary.interview.itunes.R
import gary.interview.itunes.api.MusicDataApi
import gary.interview.itunes.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(private val itunesApi: MusicDataApi) : ViewModel() {

    val tracks = MutableLiveData<Array<MusicDataApi.Track>>()
    val loading = MutableLiveData<Boolean>()
    val previewTrack = MutableLiveData<MusicDataApi.Track>()
    val error = SingleLiveEvent<Int>()

    private var hotTracks: Array<MusicDataApi.Track>? = null
    private var player: MediaPlayer? = null
    private var currentPreviewJob: Job? = null

    fun loadHotTracks() {
        CoroutineScope(Dispatchers.IO).launch {


            loading.postValue(true)
            try {
                hotTracks = itunesApi.getHotTracks().await().feed.results
                tracks.postValue(hotTracks)
            } catch (e: Throwable) {
                error.postValue(R.string.please_check_network)
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun onQueryTextChange(query: String): Boolean {
        if (query.isEmpty()) {
            tracks.postValue(hotTracks)
        }
        return false
    }

    fun onQueryTextSubmit(query: String?): Boolean {
        if (query == null) {
            return false
        }

        CoroutineScope(Dispatchers.IO).launch {
            loading.postValue(true)
            try {
                tracks.postValue(
                    itunesApi.search(query).await().results
                )
            } catch (e: Throwable) {
                error.postValue(R.string.please_check_network)
            } finally {
                loading.postValue(false)
            }
        }

        return true
    }

    fun onTrackItemClick(position: Int, track: MusicDataApi.Track) {
        currentPreviewJob?.cancel()
        currentPreviewJob = CoroutineScope(Dispatchers.IO).launch {

            val rsp = itunesApi.getTrackDetail(track.id).await()

            if (rsp.results.isEmpty()) {
                error.postValue(R.string.no_preview_available)
                return@launch
            }

            synchronized(this@MainViewModel) {
                releasePlayer()
                play(rsp.results[0].previewUrl)
                previewTrack.postValue(track)
            }
        }
    }

    fun stopPlay() {
        synchronized(this@MainViewModel) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }

    private fun play(fileUrl: String) {
        player = MediaPlayer().apply {
            setDataSource(fileUrl)
            setOnCompletionListener {
                previewTrack.postValue(null)
            }
            prepare()
            start()
        }
    }
}
