package com.saket.samplemediaplayer

import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saket.samplemediaplayer.databinding.LayoutMediaitemBinding
import java.util.function.Consumer

class PlayListAdapter (val clickListener : Consumer<MediaBrowserCompat.MediaItem>) :
    ListAdapter<MediaBrowserCompat.MediaItem, PlayListAdapter.MediaItemViewHolder>(MediaItemDiffCallback()) {

    class MediaItemViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {
        private var title : TextView = view.findViewById(R.id.txtTitle)

        fun bind(mediaItem: MediaBrowserCompat.MediaItem, clickListener: Consumer<MediaBrowserCompat.MediaItem>) {
            title.text = mediaItem.description.title
            title.setOnClickListener {
                clickListener.accept(mediaItem)
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

    //List item layout height/width are set to wrap_content when using databinding....
    /*
    class MediaItemViewHolder(private val layoutMediaitemBinding: LayoutMediaitemBinding)
        : RecyclerView.ViewHolder(layoutMediaitemBinding.root) {

        fun bind(mediaItem: MediaBrowserCompat.MediaItem) {
            layoutMediaitemBinding.mediaItem = mediaItem
        }

        companion object {
            fun from(parent: ViewGroup) : MediaItemViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutMediaitemBinding.inflate(inflater)
                return MediaItemViewHolder(binding)
            }
        }
    }
*/


    class MediaItemDiffCallback : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
        override fun areItemsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean {
            return oldItem.mediaId.equals(newItem.mediaId)
        }

        override fun areContentsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean {
            return oldItem.description.title == newItem.description.title
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        return MediaItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
            holder.bind(getItem(position), clickListener)
    }
}