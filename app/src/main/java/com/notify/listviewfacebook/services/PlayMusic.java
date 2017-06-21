package com.notify.listviewfacebook.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Himanjan on 06-06-2017.
 */

public class PlayMusic extends Service {
    public final static String FOLK_FABLE_ACTIVITY = "com.notify.listviewfacebook.activities.StartActivity";
    public final static String FOLK_FABLE_MESSAGE = "com.notify.listViewFacebook.startActivity.message";
    private static String TAG = PlayMusic.class.getSimpleName();
    private static MediaPlayer mediaPlayer;
    LocalBroadcastManager localBroadcastManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
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
                sendResult("Complete");
            }
        });
        return START_STICKY;
    }

    private void sendResult(String message) {
        Intent intent = new Intent(FOLK_FABLE_ACTIVITY);
        if (message != null) {
            intent.putExtra(FOLK_FABLE_MESSAGE, message);
            localBroadcastManager.sendBroadcast(intent);
        }
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            //Media player needs to be re initialized to null to avoid Illegal State Exception.
            mediaPlayer = null;
        }
    }
}
