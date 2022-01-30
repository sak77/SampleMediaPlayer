package com.saket.samplemediaplayer

import android.media.session.PlaybackState
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PlayListAdapter (private val clickListener : (MyMediaItem, Int) -> Unit) :
    ListAdapter<MyMediaItem, PlayListAdapter.MediaItemViewHolder>(MediaItemDiffCallback()) {

    //List item layout height/width are set to wrap_content when using databinding....
    class MediaItemViewHolder(val view: View)
        : RecyclerView.ViewHolder(view) {
        private val title : TextView = view.findViewById(R.id.txtTitle)
        private val playbackIcon : ImageView = view.findViewById(R.id.playPause)
        private val stopIcon : ImageView = view.findViewById(R.id.stop)

        fun bind(mediaItem: MyMediaItem, clickListener: (MyMediaItem, Int) -> Unit) {
            title.text = mediaItem.mediaItem.description.title
            mediaItem.playBackState?.let {
                when (it.state) {
                    PlaybackState.STATE_PLAYING -> {
                        playbackIcon.setImageResource(R.drawable.ic_pause)
                        stopIcon.visibility = View.VISIBLE
                    }
                    PlaybackState.STATE_PAUSED -> {
                        playbackIcon.setImageResource(R.drawable.ic_play_arrow)
                        stopIcon.visibility = View.VISIBLE
                    }
                    else -> {
                        playbackIcon.setImageResource(R.drawable.ic_play_arrow)
                        stopIcon.visibility = View.INVISIBLE
                    }
                }
            }
            playbackIcon.setOnClickListener {
                //0 is for play/pause media
                clickListener(mediaItem, 0)
            }
            stopIcon.setOnClickListener {
                //1 is to stop media
                clickListener(mediaItem, 1)
            }
        }

        companion object {
            fun from(parent: ViewGroup) : MediaItemViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val rootView = inflater.inflate(R.layout.layout_mediaitem,parent,false)
                return MediaItemViewHolder(rootView)
            }
        }

    }

    class MediaItemDiffCallback : DiffUtil.ItemCallback<MyMediaItem>() {
        override fun areItemsTheSame(
            oldItem: MyMediaItem,
            newItem: MyMediaItem
        ): Boolean {
            return oldItem.mediaItem.mediaId.equals(newItem.mediaItem.mediaId)
        }

        override fun areContentsTheSame(
            oldItem: MyMediaItem,
            newItem: MyMediaItem
        ): Boolean {
            //Tried to compare playbackstate but did not work..
            //so for now return false..
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        return MediaItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
            holder.bind(getItem(position), clickListener)
    }
}
