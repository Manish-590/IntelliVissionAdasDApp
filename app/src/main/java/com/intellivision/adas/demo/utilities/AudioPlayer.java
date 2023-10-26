package com.intellivision.adas.demo.utilities;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * This is a singleton class and is responsible for playing predefined audio clip from raw resources
 * folder once the recognition is successful.
 * 
 */
public class AudioPlayer {
    private static AudioPlayer _instance;

    private MediaPlayer _mediaPlayer;

    /**
     * Private Constructor
     */
    private AudioPlayer() {

    }

    /**
     * Method to get the initialized instance of type {@link AudioPlayer}
     * 
     * @return the initialized instance of type {@link AudioPlayer}
     */
    public static AudioPlayer getInstance() {
        if ( _instance == null ) {
            _instance = new AudioPlayer( );
        }
        return _instance;
    }

    /**
     * Method to play the audio file from input resourceId
     * 
     * @param context
     *            the Application context
     * @param resourceId
     *            the resource id (mostly a raw resource)
     */
    public void play( Context context, int resourceId ) {
        if ( _mediaPlayer == null ) {
            _mediaPlayer = MediaPlayer.create( context, resourceId );
        }
        _mediaPlayer.start( );
    }

    /**
     * Method to check if player is currently playing
     * 
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        if ( _mediaPlayer != null ) {
            return _mediaPlayer.isPlaying( );
        }
        return false;
    }

    /**
     * Method to stop playing
     */
    public void stop() {
        try {
            if ( _mediaPlayer != null && _mediaPlayer.isPlaying( ) ) {
                _mediaPlayer.stop( );
                _mediaPlayer.release( );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
    }
}
