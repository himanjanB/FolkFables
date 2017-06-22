package com.notify.listviewfacebook.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.notify.listviewfacebook.config.AppConfig;

import java.io.IOException;

/**
 * Created by Himanjan on 06-06-2017.
 */

public class PlayMusic extends Service {

    public final static String FOLK_FABLE_ACTIVITY = AppConfig.FOLK_FABLE_ACTIVITY;
    public final static String FOLK_FABLE_MESSAGE = AppConfig.FOLK_FABLE_MESSAGE;
    public final static String AUDIO_DURATION = AppConfig.AUDIO_DURATION;
    public final static String CURRENT_AUDIO_DURATION = AppConfig.CURRENT_AUDIO_DURATION;
    public final static String STORY_PLAYING_SCENARIO = AppConfig.STORY_PLAYING_SCENARIO;

    private static String TAG = PlayMusic.class.getSimpleName();
    private static MediaPlayer mediaPlayer;
    private static String CURRENT_PLAYING_AUDIO_URL;
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
        if (intent != null) {
            String action = intent.getStringExtra(AppConfig.ACTION);
            Log.i(TAG, "Action is " + action);

            if (action.equalsIgnoreCase(AppConfig.AUDIO_PLAY)) {
                String URL = intent.getStringExtra(AppConfig.AUDIO_URL);
                Log.i(TAG, "URL is " + URL);

                /**
                 * If mediaPlayer is not null and not playing, it is in pause state. We need to simply start it and send
                 * a broadcast to the StartActivity to let know that the audio is playing now.
                 *
                 * Else if mediaPlayer is not null and is playing, user has selected some other story while one story is already playing.
                 * In this case, we need to reset the mediaPlayer to remove the Data source and other details and re-initiate the player
                 * with the new values. And then send the broadcast the StartActivity to let know that the new story is playing.
                 *
                 * If mediaPlayer is null, it means this is the first time the user is running the app. Initialise the mediaPlayer with
                 * the necessary values and send the broadcast to the StartActivity to let know that the new story is playing.
                 */

                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        if (URL.equalsIgnoreCase(CURRENT_PLAYING_AUDIO_URL)) {
                            mediaPlayer.start();
                            sendDetailsForTheAudio(mediaPlayer, AppConfig.PAUSED_NOW_PLAYED);
                        } else {
                            mediaPlayer.reset();
                            mediaPlayer = getMediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(URL);
                                CURRENT_PLAYING_AUDIO_URL = URL;
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.start();

                            sendDetailsForTheAudio(mediaPlayer, AppConfig.NEW_STORY_PLAYING);
                        }
                    } else {
                        if (mediaPlayer.isPlaying()) {
                            Log.i(TAG, "Music is already playing");
                            mediaPlayer.reset();
                        }
                        mediaPlayer = getMediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            mediaPlayer.setDataSource(URL);
                            CURRENT_PLAYING_AUDIO_URL = URL;
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaPlayer.start();

                        sendDetailsForTheAudio(mediaPlayer, AppConfig.NEW_STORY_PLAYING);
                    }
                } else {
                    mediaPlayer = getMediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(URL);
                        CURRENT_PLAYING_AUDIO_URL = URL;
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();

                    sendDetailsForTheAudio(mediaPlayer, AppConfig.FIRST_TIME_STORY_PLAYING);
                }
            } else if (action.equalsIgnoreCase(AppConfig.AUDIO_PAUSE)) {
                if (mediaPlayer != null)
                    mediaPlayer.pause();
            } else if (action.equalsIgnoreCase(AppConfig.AUDIO_FORWARD)) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int forwardByMilliSeconds = intent.getIntExtra(AppConfig.FORWARD_BY, 3000);
                    int currentDuration = mediaPlayer.getCurrentPosition();

                    if (currentDuration + forwardByMilliSeconds < mediaPlayer.getDuration()) {
                        mediaPlayer.seekTo(currentDuration + forwardByMilliSeconds);
                    }
                } else {
                    Log.i(TAG, "Media player is not in playing state");
                }
            } else if (action.equalsIgnoreCase(AppConfig.AUDIO_REWIND)) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int rewindByMilliSeconds = intent.getIntExtra(AppConfig.REWIND_BY, 3000);
                    int currentDuration = mediaPlayer.getCurrentPosition();

                    if (currentDuration - rewindByMilliSeconds > 3000) {
                        mediaPlayer.seekTo(currentDuration - rewindByMilliSeconds);
                    }
                } else {
                    Log.i(TAG, "Media player is not in playing state");
                }
            } else if (action.equalsIgnoreCase(AppConfig.USER_SEEK)) {
                if (mediaPlayer != null) {
                    Log.i(TAG, "Seeking to the user set position");
                    int seekTo = intent.getIntExtra(AppConfig.SEEK_TO, mediaPlayer.getCurrentPosition());
                    mediaPlayer.seekTo(seekTo);
                }
            }

            //This is a callback method that will be called once the audio run is completed.
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(TAG, "On Completion called");
                        mediaPlayer.release();
                        mediaPlayer = null;
                        sendResult(AppConfig.COMPLETE);
                    }
                });
            }
        }
        return START_STICKY;
    }

    /**
     * This is a broadcast method. This sends a Local Broadcast to the StartActivity to know that the Story is now playing.
     *
     * @param mediaPlayer
     */
    private void sendDetailsForTheAudio(MediaPlayer mediaPlayer, String message) {
        Intent intent = new Intent(FOLK_FABLE_ACTIVITY);
        if (mediaPlayer != null) {
            intent.putExtra(AppConfig.ACTION, AppConfig.STORY_START);
            intent.putExtra(AUDIO_DURATION, mediaPlayer.getDuration());
            intent.putExtra(CURRENT_AUDIO_DURATION, mediaPlayer.getCurrentPosition());
            intent.putExtra(STORY_PLAYING_SCENARIO, message);
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    /**
     * This is a broadcast method. This sends a Local Broadcast to the StartActivity to know that the Story is now completed.
     *
     * @param message
     */
    private void sendResult(String message) {
        if (message != null) {
            Intent intent = new Intent(FOLK_FABLE_ACTIVITY);
            intent.putExtra(AppConfig.ACTION, AppConfig.COMPLETE);
            intent.putExtra(FOLK_FABLE_MESSAGE, AppConfig.STORY_COMPLETED_PLAYING);
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    /**
     * This returns a Singleton mediaPlayer reference.
     * @return
     */
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
