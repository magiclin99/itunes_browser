package gary.interview.itunes.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import gary.interview.itunes.R
import gary.interview.itunes.api.MusicDataApi
import gary.interview.itunes.databinding.ActivityMainBinding
import gary.interview.itunes.databinding.TrackItemBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@BindingAdapter("imageUrl")
fun bindImageUrl(view: ImageView, url: String) {
    Glide.with(view)
        .load(url)
        .into(view)
}

@BindingAdapter("tracks")
fun bindTracks(view: RecyclerView, tracks: Array<MusicDataApi.Track>?) {
    if (tracks == null) {
        return
    }

    view.adapter?.let {
        (it as MainActivity.TrackAdapter).dataSource = tracks
        it.notifyDataSetChanged()
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.setLifecycleOwner(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TrackAdapter(viewModel, emptyArray())
        binding.model = viewModel

        viewModel.loadHotTracks()

        snackBar = Snackbar
            .make(binding.root, "", Snackbar.LENGTH_INDEFINITE)
            .setAction("Stop") { viewModel.stopPlay() }

        binding.searchView.apply {
            setIconifiedByDefault(false)
            clearFocus()
        }

        viewModel.previewTrack.observe(this, Observer { track ->
            track?.run {
                snackBar.apply {
                    setText(track.name)
                    show()
                }
            } ?: snackBar.apply { dismiss() }
        })

        viewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    class TrackHolder(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root)

    class TrackAdapter(
        private val viewModel: MainViewModel,
        var dataSource: Array<MusicDataApi.Track>
    ) : RecyclerView.Adapter<TrackHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
            return TrackHolder(
                TrackItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = dataSource.size

        override fun onBindViewHolder(holder: TrackHolder, position: Int) {
            holder.binding.model = viewModel
            holder.binding.position = position
        }
    }
}
