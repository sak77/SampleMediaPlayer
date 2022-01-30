package com.saket.samplemediaplayer

import android.os.Bundle
import com.saket.samplemediaplayer.databinding.ActivityMainBinding

/**
 * A simple app that explores the media app architecture for audio apps:
 * MediaBrowserService: MediaSession + MediaItems + MediaPlayer interface
 * MediaBrowser: MediaBrowser + MediaController
 * It has a single media item which is displayed in the app.
 * MyMediaBrowserService implements MediaBrowserService:
 * Holds MediaSession instance, provides callbacks for getRoot and onChildLoaded
 * MediaSession instance holds playbackstate info, MediaMetadata, session token.
 * MediaSession callback listens to onPlay, onPause, onStop commands from MediaController.
 * MediaBrowserClientWrapper class holds MediaController which ties the UI to the MediaSesion.
 */
class MainActivity : BaseMediaBrowserClientActivity() {

    private val mediaBrowserClientWrapper: MediaBrowserClientWrapper by lazy {
        MediaBrowserClientWrapper(this@MainActivity)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val playListAdapter = PlayListAdapter { mediaItemClicked, action ->
            if (action == 0) {
                if (mediaItemClicked.mediaItem.isPlayable) {
                    mediaBrowserClientWrapper.togglePlayPause()
                } else {
                    //Browse content...
                }
            }else {
                //Stop playback
                mediaBrowserClientWrapper.stopPlayback()
            }
        }

        binding.playList.adapter = playListAdapter
        mediaBrowserClientWrapper.livePlayListData.observe(this) { mediaItems ->
            playListAdapter.submitList(mediaItems)
        }

        mediaBrowserClientWrapper.playbackState.observe(this) { playbackState ->
            val mediaItem = playListAdapter.currentList[0]
            mediaItem.playBackState = playbackState
            playListAdapter.submitList(playListAdapter.currentList)
        }
    }

    override fun onStart() {
        super.onStart()
        /*
        Connect to the MediaBrowserService.
        Here's where the magic of MediaBrowserCompat.ConnectionCallback comes in.
        If the connection is successful, the onConnect() callback creates the media controller,
        links it to the media session, links your UI controls to the MediaController,
        and registers the controller to receive callbacks from the media session.
         */
        mediaBrowserClientWrapper.connect()
    }

    override fun onResume() {
        super.onResume()
        //sets the audio stream so your app responds to the volume control on the device.
        //volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        //disconnects your MediaBrowser and unregisters the MediaController.Callback when your activity stops.
        mediaBrowserClientWrapper.disconnect()
    }

    override fun buildTransportControls() {
        // Register a Callback to stay in sync
        mediaBrowserClientWrapper.registerMediaControllerCallback()
    }
}
