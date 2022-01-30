package com.saket.samplemediaplayer

import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/*
This class separates the MediaBrowser client code from that
of the MediaBrowserClient Activity.

It includes:
MediaBrowser connection callback to listen and act on different connection states.
MediaBrowser subscription callback to load children data from mediabrowser service.
MediaController callbacks to listen for changes in mediasession.
 */
class MediaBrowserClientWrapper(val activity: BaseMediaBrowserClientActivity) {
    private lateinit var mediaBrowser: MediaBrowserCompat

    /*
    Using backing property for MutableLiveData prevents some
    external class from inadvertently changing value
    of this live data.
     */
    private val _livePlayList = MutableLiveData<MutableList<MyMediaItem>>()
    val livePlayListData: LiveData<MutableList<MyMediaItem>>
    get() = _livePlayList

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean>
    get() = _isConnected

    private val _playbackState = MutableLiveData<PlaybackStateCompat>()
    val playbackState: LiveData<PlaybackStateCompat>
    get() = _playbackState

    val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
        .build()

    val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

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
            /*
            Sets a MediaControllerCompat in the activity for later retrieval via
            getMediaController(Activity).
             */
            MediaControllerCompat.setMediaController(activity, mediaController)
            }

            // Finish building the UI
            activity.buildTransportControls()

            //returns response from onGetRoot()
            val root = mediaBrowser.root

            //Load children..
            mediaBrowser.subscribe(root, subscriptionCallbacks)
            _isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            // The Service has crashed. Disable transport controls until it automatically reconnects
            println("onConnectionSuspended")
            _isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            // The Service has refused our connection
            println("onConnectionFailed")
            _isConnected.postValue(false)
        }
    }

    private val subscriptionCallbacks = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            val mediaList = children
                .map {
                    MyMediaItem(it, null)
                }.toMutableList()
            _livePlayList.postValue(mediaList)
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
            ComponentName(activity, MyMediaBrowserService::class.java),
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

    fun registerMediaControllerCallback() {
        val mediaController = MediaControllerCompat.getMediaController(activity)
        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            /*
            calling mediaSession?.setMetadata(metadata) in MyMediaBrowserService
            calls this method. And magically also sets metadata for
            mediaController instance. So need to do any additional work...
             */
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }
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

    fun stopPlayback() {
        val mediaController = MediaControllerCompat.getMediaController(activity)
        mediaController.transportControls.stop()
    }
}
