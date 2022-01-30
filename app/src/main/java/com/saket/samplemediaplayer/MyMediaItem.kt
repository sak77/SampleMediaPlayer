package com.saket.samplemediaplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat

data class MyMediaItem(val mediaItem: MediaBrowserCompat.MediaItem,
                       var playBackState : PlaybackStateCompat?) {
}