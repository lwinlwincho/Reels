package com.example.reels

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.reels.databinding.ListVideoBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.video.VideoSize

class VideoAdapter(
    var context: Context,
    var videos: ArrayList<Video>,
    var videoPrepareListener: OnVideoPreparedListener
) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ListVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        return VideoViewHolder(binding, context, videoPrepareListener)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.setData(videos[position])
    }


    class VideoViewHolder(
        val binding: ListVideoBinding,
        var context: Context,
        var videoPrepareListener: OnVideoPreparedListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var exoplayer: ExoPlayer
        private lateinit var mediaSource: MediaSource

        fun setData(video: Video) {
            binding.tvTitle.text = video.title
            setVideoPath(video.url)
        }

        fun setVideoPath(url: String) {
            exoplayer = ExoPlayer.Builder(context).build()

            exoplayer.addListener(object : Player.Listener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        binding.pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(context, "Cannot Play this video", Toast.LENGTH_LONG).show()
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
//                                 val videoRatio = videoSize.width / videoSize.height.toFloat()
//                                val screenRatio = binding.videoView.width / binding.videoView.height.toFloat()
//                                val scale = videoRatio / screenRatio
//                                if (scale >= 1f) {
//                                    binding.videoView.scaleX = scale
//                                } else {
//                                    binding.videoView.scaleY = 1f / scale
//                                }
//                }
                    val videoWidth = videoSize.width
                    val videoHeight = videoSize.height
                    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                    val layout = binding.playerView.layoutParams
                    layout.width = screenWidth
                    layout.height = ((videoHeight.toFloat() / videoWidth.toFloat()) * screenWidth.toFloat()).toInt()
                    binding.playerView.layoutParams = layout
                }
            })

            binding.playerView.player = exoplayer
            exoplayer.seekTo(0)
            exoplayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(context)
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(url))
            )

            exoplayer.setMediaSource(mediaSource)
            exoplayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoplayer.playWhenReady = true
                exoplayer.play()
            }

            videoPrepareListener.onVideoPrepared(ExoPlayerItem(exoplayer, absoluteAdapterPosition))

        }
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayer: ExoPlayerItem)
    }
}