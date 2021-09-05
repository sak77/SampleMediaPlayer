package com.saket.samplemediaplayer

import android.os.Bundle
import android.widget.ImageView

/*

 */
class MainActivity : BaseMediaBrowserClientActivity() {

    private lateinit var playPause : ImageView
    private val mediaBrowserClientWrapper : MediaBrowserClientWrapper by lazy {
            MediaBrowserClientWrapper(this@MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        /*
        connects to the MediaBrowserService.
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
        // Grab the view for the play/pause button
        playPause = findViewById<ImageView>(R.id.play_pause).apply {
            setOnClickListener {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly
                mediaBrowserClientWrapper.togglePlayPause()
            }
        }

        // Register a Callback to stay in sync
        mediaBrowserClientWrapper.registerMediaControllerCallback()
    }

}