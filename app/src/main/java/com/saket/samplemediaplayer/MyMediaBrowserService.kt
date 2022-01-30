package com.saket.samplemediaplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

class MyMediaBrowserService : MediaBrowserServiceCompat() {

    private val LOG_TAG: String = "MyMediaBrowserService"
    val mediaItems = emptyList<MediaBrowserCompat.MediaItem>().toMutableList()

    /*
    Media Session contains info like-
    Media Meta-data
    Current Playback state
    Callbacks from Media Buttons and Transport Controls
    Session token
     */
    private var mediaSession: MediaSessionCompat? = null

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    override fun onCreate() {
        super.onCreate()
        /*
        Use onCreate to initialize MediaSession instance:
        1. Set playback state
        2. Set media session callback
        3. Set media session token
         */
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

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
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
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

        val testMediaItem = MediaBrowserCompat.MediaItem(
            mediaDescription,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
        mediaItems.add(testMediaItem)
        result.sendResult(mediaItems)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Saket MyMediaBrowserService started...")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Saket service destroyed...")
    }

    inner class MySessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            //Request audio focus for playback, this registers audiofocusChangeListener

            //start service
            startService(Intent(baseContext, MyMediaBrowserService::class.java))
            //set session is active (and update metadata and state)
            mediaSession?.isActive = true
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0F)
            /*
            Update playback state of mediasession object invokes
            onPlaybackStateChanged() in MediaControllerCompat.Callback() instance.
            It is recommended to reuse PlaybackStateCompat.builder when creating new
            playbackState.
             */
            mediaSession?.setPlaybackState(stateBuilder.build())
            //Get current playing media Item...
            val mediaItem = mediaItems[0]
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaItem.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                    mediaItem.description.title as String?
                )
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                    mediaItem.description.description as String?
                )
                .build()
            mediaSession?.setMetadata(metadata)

            //Start player..custom logic

            //Register BECOME_NOISY BroadcastReceiver
            /*
            When audio output switches back to the built-in speaker the system broadcasts an
            ACTION_AUDIO_BECOMING_NOISY intent. You should create a BroadcastReceiver that
            listens for this intent whenever you’re playing audio. Your receiver should look
            like this:
            private class BecomingNoisyReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                        // Pause the playback
                    }
                }
            }
            For more details refer:
            https://developer.android.com/guide/topics/media-apps/volume-and-earphones#becoming-noisy
             */

            //Put service in foreground and put notification.
        }

        override fun onPause() {
            super.onPause()
            //Update metadata and state
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 0F)
            mediaSession?.setPlaybackState(stateBuilder.build())

            //Pause player
            //unregister BECOME_NOISY BroadcastReceiver

            //Take service out of foreground, but retain the notification
        }

        override fun onStop() {
            super.onStop()
            //Abandon Audio focus
            //Stop service
            stopSelf()
            //Set session to inactive state (update metadata and state)
            mediaSession?.isActive = false
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 0F)
            mediaSession?.setPlaybackState(stateBuilder.build())
            mediaSession?.setMetadata(null)
            //Stop player
            //Take service out of foreground
        }
    }
}