package com.saket.samplemediaplayer

import android.media.browse.MediaBrowser
import android.media.browse.MediaBrowser.MediaItem.FLAG_PLAYABLE
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

class SampleMusicService : MediaBrowserServiceCompat() {

    private val LOG_TAG: String = "SampleMusicService"

    /*
        Media Session contains info like-
        Media Meta-data
        Current Playback state
        Callbacks from Media Buttons and Transport Controls
        Session token
         */
    private var mediaSession : MediaSessionCompat? = null

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    override fun onCreate() {
        super.onCreate()
        /*
        Use onCreate to -
        1. Initialize media session
        2. Set media session callback
        3. Set media session token
         */
        mediaSession = MediaSessionCompat(baseContext,LOG_TAG).apply {

            /*
            Enable callbacks from MediaButtons and TransportControls

            Deprecated:
            This flag is no longer used. All media sessions are expected to handle media button events
            and transport controls now.
            For backward compatibility, these flags will be always set.

                setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            */

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(MySessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
    }

    /*
    Called after MediaBrowser client invokes connect()

    If onGetRoot returns null, then connection is refused to the MediaBrowser client.
    Else, a non-null value is returned that represents the root ID of the content hierarchy.

    To allow clients to connect to your MediaSession without browsing, onGetRoot() must still
    return a non-null BrowserRoot, but the root ID should represent an empty content hierarchy.

    The onGetRoot() method should quickly return a non-null value. User authentication and other
    slow processes should not run in onGetRoot(). Most business logic should be handled in the
    onLoadChildren() method, described in the next section.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("com.saket.samplemediaplayer.samplemusicservice", null)
    }

    /*
    After the client connects, it can traverse the content hierarchy by making repeated calls to
    MediaBrowserCompat.subscribe() to build a local representation of the UI.

    The subscribe() method sends the callback onLoadChildren() to the service, which returns
    a list of MediaBrowser.MediaItem objects.
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        val duration = 1000L
        val songDuration = Bundle()
        songDuration.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

        val mediaDescription = MediaDescriptionCompat.Builder()
            .setTitle("Bear Necessities")
            .setDescription("Jungle Book")
            .setMediaId("21345")
            .setMediaUri(Uri.parse(""))
            .setExtras(songDuration)
            .build()

        val testMediaItem = MediaBrowserCompat.MediaItem(mediaDescription, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)

        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>().toMutableList()

        mediaItems.add(testMediaItem)

        result.sendResult(mediaItems)
    }

    class MySessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
        }

        override fun onPause() {
            super.onPause()
        }

        override fun onStop() {
            super.onStop()
        }
    }
}