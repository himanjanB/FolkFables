package com.notify.listviewfacebook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.Utils.Utilities;
import com.notify.listviewfacebook.config.AppConfig;
import com.notify.listviewfacebook.data.FeedItem;
import com.notify.listviewfacebook.services.PlayMusic;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // Declare class member variables

    private static final String ACTION = AppConfig.ACTION;
    private static final String STORY_COMPLETED_PLAYING = AppConfig.STORY_COMPLETED_PLAYING;
    private static String TAG = StartActivity.class.getSimpleName();
    private static int position;
    private static int AUDIO_LIST_SIZE;
    private static int previousStoryID = -1;
    private static String AUDIO_PLAYING = "";
    private static int totalDurationOfTheAudio;
    private static int currentDurationOfTheAudio;
    private static boolean continueAddingSecondsToAudio = false;
    private static SeekBar seekBar;
    private static TextView currentTimeTextView;
    private static TextView fullTimeTextView;
    public BroadcastReceiver broadcastReceiver;
    private ImageView playButton;
    private ImageView pauseButton;
    private ImageView nextButton;
    private ImageView previousButton;
    private ImageView rewindButton;
    private ImageView ffButton;
    private ImageView actionShare;
    private ImageView actionSave;
    private ImageView actionComment;
    private ImageView actionInfo;
    private Handler mHandler = new Handler();
    private TextView audioDetails;
    private TextView currentAudioPlaying;
    private ArrayList<FeedItem> itemList;
    private Utilities utilities;

    /**
     * Background Runnable thread for updating the seek bar in the seek bar
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (continueAddingSecondsToAudio) {
                // Displaying time full time of the audio and completed playing time
                fullTimeTextView.setText(utilities.milliSecondsToTimer(totalDurationOfTheAudio));
                currentTimeTextView.setText(utilities.milliSecondsToTimer(currentDurationOfTheAudio));

                // Updating progress bar
                int progress = utilities.getProgressPercentage(currentDurationOfTheAudio, totalDurationOfTheAudio);
                seekBar.setProgress(progress);

                // Running this thread after 10 milliseconds
                mHandler.postDelayed(this, 10);
            }
        }
    };

    /**
     * Background Runnable thread for updating the time of the audio playing
     */
    private Runnable mUpdateCurrentDuration = new Runnable() {
        @Override
        public void run() {
            if (continueAddingSecondsToAudio) {
                //Log.i(TAG, "Current audio duration is " + currentDurationOfTheAudio);
                // Adding 1000 milliseconds to the current time every second.
                currentDurationOfTheAudio += 1000;

                // Running this thread after 1000 milliseconds
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        // Creating an object for the Utilities class. This is needed to calculate the time conversion for the seek bar.
        utilities = new Utilities();


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(ACTION);

                if (action.equalsIgnoreCase(AppConfig.COMPLETE)) {
                    String message = intent.getStringExtra(PlayMusic.FOLK_FABLE_MESSAGE);
                    if (message != null && message.equalsIgnoreCase(STORY_COMPLETED_PLAYING)) {
                        /**
                         * Set the current duration of the audio to zero and set the boolean value to false
                         * Also handle the visibility of the image views accordingly. The Pause image button
                         * should be invisible and the Play image button should be visible.
                         */
                        currentDurationOfTheAudio = 0;
                        continueAddingSecondsToAudio = false;
                        pauseButton.setVisibility(View.INVISIBLE);
                        playButton.setVisibility(View.VISIBLE);
                    }
                } else if (action.equalsIgnoreCase(AppConfig.STORY_START)) {
                    /**
                     * This is a broadcast receiver method that is triggered when the story has started playing for the first time. In only the
                     * first instance, we need to call the runnable methods (updateCurrentDuration and updateProgressBar). When user clicks on
                     * Next or Previous buttons, we should not call these runnable methods. It will create inconsistency in updating the seek bar.
                     */

                    totalDurationOfTheAudio = intent.getIntExtra(PlayMusic.AUDIO_DURATION, 0);
                    currentDurationOfTheAudio = intent.getIntExtra(PlayMusic.CURRENT_AUDIO_DURATION, 0);

                    // Setting the TextView to the Total Duration of the audio
                    fullTimeTextView.setText(utilities.milliSecondsToTimer(totalDurationOfTheAudio));

                    // Setting the boolean to true and calling the background tasks. This will trigger the runnable tasks to update the seek bar.
                    continueAddingSecondsToAudio = true;
                    Log.i(TAG, "Audio playing scenario is " + intent.getStringExtra(AppConfig.STORY_PLAYING_SCENARIO));

                    updateCurrentDuration();
                    updateProgressBar();

                }
            }
        };

        // These are clickable image views but named as buttons to have clarity on their functionality
        playButton = (ImageView) findViewById(R.id.buttonPlay);
        pauseButton = (ImageView) findViewById(R.id.buttonPause);
        nextButton = (ImageView) findViewById(R.id.buttonNext);
        previousButton = (ImageView) findViewById(R.id.buttonPrevious);
        rewindButton = (ImageView) findViewById(R.id.buttonReverse);
        ffButton = (ImageView) findViewById(R.id.buttonForward);

        // Actions that are available in the music player interface
        actionShare = (ImageView) findViewById(R.id.action_share);
        actionSave = (ImageView) findViewById(R.id.action_save);
        actionComment = (ImageView) findViewById(R.id.action_comment);
        actionInfo = (ImageView) findViewById(R.id.action_info);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        audioDetails = (TextView) findViewById(R.id.storyNameTextView);
        currentAudioPlaying = (TextView) findViewById(R.id.audioCurrentPlayingTextView);
        currentTimeTextView = (TextView) findViewById(R.id.currentTimeTextView);
        fullTimeTextView = (TextView) findViewById(R.id.fullTimeTextView);

        // This TextView is set to show the name of the audio that is currently playing
        Log.i(TAG, "Audio Playing is " + AUDIO_PLAYING);
        if (!AUDIO_PLAYING.equalsIgnoreCase("")) {
            currentAudioPlaying.setText(AUDIO_PLAYING);
        }

        Intent intent = getIntent();
        itemList = (ArrayList<FeedItem>) intent.getSerializableExtra(AppConfig.AUDIO_LIST);
        position = intent.getIntExtra(AppConfig.POSITION, 0);

        AUDIO_LIST_SIZE = itemList.size();
        Log.i(TAG, "The length of the audio list is " + AUDIO_LIST_SIZE);

        audioDetails.setText(itemList.get(position).getName());

        // In the beginning, the Pause button should be invisible.
        if (itemList.get(position).getId() == previousStoryID) {
            playButton.setVisibility(View.INVISIBLE);
        } else {
            pauseButton.setVisibility(View.INVISIBLE);
        }

        // Enable the onClickListener on each of the clickable items in the UI.
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        rewindButton.setOnClickListener(this);
        ffButton.setOnClickListener(this);
        actionShare.setOnClickListener(this);
        actionSave.setOnClickListener(this);
        actionComment.setOnClickListener(this);
        actionInfo.setOnClickListener(this);
    }

    private void updateCurrentDuration() {
        Log.i(TAG, "Updating current duration of the audio");
        mHandler.postDelayed(mUpdateCurrentDuration, 100);
    }

    public void updateProgressBar() {
        Log.i(TAG, "Updating Seek bar");
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler. This function is not yet finished.
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        // TODO This is not done yet.
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress handler. This function is not yet finished.
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO This is not done yet.
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = totalDurationOfTheAudio;
        int currentPosition = utilities.progressToTimer(seekBar.getProgress(), totalDuration);

        // Forward or Rewind to certain seconds
        Intent intent = new Intent(this, PlayMusic.class);
        intent.putExtra(AppConfig.ACTION, AppConfig.USER_SEEK);
        intent.putExtra(AppConfig.SEEK_TO, currentPosition);
        startService(intent);

        // Update timer progress
        updateProgressBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(PlayMusic.FOLK_FABLE_ACTIVITY)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    // Handle all the clicks here.
    @Override
    public void onClick(View v) {
        if (v == playButton) {

            previousStoryID = itemList.get(position).getId();
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);

            audioDetails.setText(itemList.get(position).getName());
            AUDIO_PLAYING = audioDetails.getText().toString();
            currentAudioPlaying.setText(AUDIO_PLAYING);

            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_PLAY);
            intent.putExtra(AppConfig.AUDIO_URL, itemList.get(position).getAudioURL());
            startService(intent);

            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.removeCallbacks(mUpdateCurrentDuration);
            continueAddingSecondsToAudio = true;

        } else if (v == pauseButton) {

            pauseButton.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);

            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_PAUSE);
            startService(intent);

            continueAddingSecondsToAudio = false;

        } else if (v == nextButton) {

            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);

            stopService(new Intent(this, PlayMusic.class));
            /*Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the next position");*/

            position++;
            if (position > AUDIO_LIST_SIZE - 1) {
                position = 0;
            }
            audioDetails.setText(itemList.get(position).getName());
            currentAudioPlaying.setText(audioDetails.getText());
            AUDIO_PLAYING = audioDetails.getText().toString();

            previousStoryID = itemList.get(position).getId();
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.AUDIO_URL, itemList.get(position).getAudioURL());
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_PLAY);
            startService(intent);

            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.removeCallbacks(mUpdateCurrentDuration);
            currentDurationOfTheAudio = 0;

        } else if (v == previousButton) {

            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);

            stopService(new Intent(this, PlayMusic.class));
            /*Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the previous position");*/

            position--;
            if (position < 0) {
                position = AUDIO_LIST_SIZE - 1;
            }
            audioDetails.setText(itemList.get(position).getName());
            currentAudioPlaying.setText(audioDetails.getText());
            AUDIO_PLAYING = audioDetails.getText().toString();

            previousStoryID = itemList.get(position).getId();
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.AUDIO_URL, itemList.get(position).getAudioURL());
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_PLAY);
            startService(intent);

            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.removeCallbacks(mUpdateCurrentDuration);
            currentDurationOfTheAudio = 0;

        } else if (v == rewindButton) {

            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_REWIND);
            intent.putExtra(AppConfig.REWIND_BY, AppConfig.REWIND_BY_MILLISECONDS);
            startService(intent);
            currentDurationOfTheAudio -= AppConfig.REWIND_BY_MILLISECONDS;

        } else if (v == ffButton) {

            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra(AppConfig.ACTION, AppConfig.AUDIO_FORWARD);
            intent.putExtra(AppConfig.FORWARD_BY, AppConfig.FORWARD_BY_MILLISECONDS);
            startService(intent);
            currentDurationOfTheAudio += AppConfig.FORWARD_BY_MILLISECONDS;

        } else if (v == actionShare) {
            Toast.makeText(this, "Sharing story", Toast.LENGTH_SHORT).show();
        } else if (v == actionSave) {
            Toast.makeText(this, "Downloading story", Toast.LENGTH_SHORT).show();
        } else if (v == actionComment) {
            Toast.makeText(this, "Comment on story", Toast.LENGTH_SHORT).show();
        } else if (v == actionInfo) {
            Toast.makeText(this, "Showing Details", Toast.LENGTH_SHORT).show();
        }
    }
}