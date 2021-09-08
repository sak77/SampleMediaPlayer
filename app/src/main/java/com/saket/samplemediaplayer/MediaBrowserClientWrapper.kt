package com.saket.samplemediaplayer

import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData

/*
The purpose of this class is to separate the mediaBrowser client code from that
of the MediaBrowserClient Activity.

All this code was originally in the MainActivity, but i felt it will be good to
separate it out here...
 */
class MediaBrowserClientWrapper(val activity: BaseMediaBrowserClientActivity) {
    private lateinit var mediaBrowser: MediaBrowserCompat

    val livePlayListData = MutableLiveData<MutableList<MediaBrowserCompat.MediaItem>>()

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    activity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(activity, mediaController)
            }

            // Finish building the UI
            activity.buildTransportControls()

            //returns response from onGetRoot()
            val root = mediaBrowser.root

            //Load children..
            mediaBrowser.subscribe(root, subscriptionCallbacks)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            // The Service has crashed. Disable transport controls until it automatically reconnects
            println("onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            // The Service has refused our connection
            println("onConnectionFailed")
        }
    }

    private val subscriptionCallbacks = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            println(parentId)
            livePlayListData.value = children
            /*
            children.apply {
                if (count() > 0) {
                    //Populate list with mediaitems
                    val firstMediaItem = get(0)
                    println(firstMediaItem.mediaId)
                }
            }
             */
        }

        override fun onError(parentId: String) {
            super.onError(parentId)
            println("onError called")
        }
    }

    init {
        /*
        constructs a MediaBrowserCompat. Pass in the name of your MediaBrowserService and
        the MediaBrowserCompat.ConnectionCallback that you've defined
         */
        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(activity, SampleMusicService::class.java),
            connectionCallbacks, null
        )
    }


    fun connect() {
        mediaBrowser.connect()
    }

    fun disconnect() {
        MediaControllerCompat.getMediaController(activity)
            .unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    fun togglePlayPause() {
        val mediaController = MediaControllerCompat.getMediaController(activity)

        val pbState = mediaController.playbackState.state
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.transportControls.pause()
        } else {
            mediaController.transportControls.play()
        }

    }

    fun registerMediaControllerCallback() {
        val mediaController = MediaControllerCompat.getMediaController(activity)
        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            println(metadata?.description?.title)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            println(state?.playbackState)

        }
    }

}