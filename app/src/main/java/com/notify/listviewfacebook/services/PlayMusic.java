package com.notify.listviewfacebook.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Himanjan on 06-06-2017.
 */

public class PlayMusic extends Service {
    private static String TAG = PlayMusic.class.getSimpleName();
    private static MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String URL = intent.getStringExtra("AUDIO_URL");
        Log.i(TAG, "URL is " + URL);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i(TAG, "Music is already playing");
            mediaPlayer.reset();
        }
        mediaPlayer = getMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(URL);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

        //This is a callback method that will be called once the audio run is completed.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "On Completion called");
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
        return START_STICKY;
    }

    private MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            return new MediaPlayer();
        } else {
            return mediaPlayer;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        //Media player needs to be re initialized to null to avoid Illegal State Exception.
        mediaPlayer = null;
    }
}
