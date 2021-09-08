package com.saket.samplemediaplayer

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.saket.samplemediaplayer.databinding.ActivityMainBinding

/*

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
        val playListAdapter = PlayListAdapter { mediaItemClicked ->
            //Toast.makeText(this,mediaItemClicked.description.title,Toast.LENGTH_LONG).show()
            if (mediaItemClicked.isPlayable) {
                mediaBrowserClientWrapper.togglePlayPause()
            }
        }

        binding.playList.adapter = playListAdapter
        mediaBrowserClientWrapper.livePlayListData.observe(this) { mediaItems ->
            playListAdapter.submitList(mediaItems)
        }
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
        /*
        findViewById<ImageView>(R.id.play_pause).apply {
            setOnClickListener {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly
                mediaBrowserClientWrapper.togglePlayPause()
            }
        }
         */

        // Register a Callback to stay in sync
        mediaBrowserClientWrapper.registerMediaControllerCallback()
    }

}