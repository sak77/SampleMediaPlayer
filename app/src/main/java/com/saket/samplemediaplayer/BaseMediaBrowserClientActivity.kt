package com.saket.samplemediaplayer

import androidx.appcompat.app.AppCompatActivity

/*
Base class for MediaBrowserClient Activity.
 */
abstract class BaseMediaBrowserClientActivity : AppCompatActivity() {

    //Function to define UI interactions with MediaController
    abstract fun buildTransportControls()
}