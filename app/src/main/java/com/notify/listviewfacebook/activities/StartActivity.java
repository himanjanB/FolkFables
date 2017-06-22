package com.notify.listviewfacebook.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.Utils.Utilities;
import com.notify.listviewfacebook.config.AppConfig;
import com.notify.listviewfacebook.data.FeedItem;
import com.notify.listviewfacebook.services.PlayMusic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // Declare class member variables

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String ACTION = AppConfig.ACTION;
    private static final String STORY_COMPLETED_PLAYING = AppConfig.STORY_COMPLETED_PLAYING;
    private static String TAG = StartActivity.class.getSimpleName();
    private static int position;
    private static int AUDIO_LIST_SIZE;
    private static int previousStoryID = -1;
    private static String AUDIO_PLAYING = "";
    private static int totalDurationOfTheAudio;
    private static int currentDurationOfTheAudio;
    private static int totalSizeOfTheDownloadedFile = 0;
    private static int downloadedSize = 0;
    private static boolean continueAddingSecondsToAudio = false;
    private static SeekBar seekBar;
    private static TextView currentTimeTextView;
    private static TextView fullTimeTextView;
    private static ArrayList<FeedItem> staticItemList;
    public BroadcastReceiver broadcastReceiver;
    private ProgressBar downloadProgressBar;
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

        updateNecessaryDetails();

        // This TextView is set to show the name of the audio that is currently playing
        Log.i(TAG, "Audio Playing is " + AUDIO_PLAYING);
        if (!AUDIO_PLAYING.equalsIgnoreCase("")) {
            currentAudioPlaying.setText(AUDIO_PLAYING);
        }

        Intent intent = getIntent();
        itemList = (ArrayList<FeedItem>) intent.getSerializableExtra(AppConfig.AUDIO_LIST);
        position = intent.getIntExtra(AppConfig.POSITION, 0);

        if (itemList != null) {
            staticItemList = itemList;
        } else {
            itemList = staticItemList;
        }

        AUDIO_LIST_SIZE = itemList.size();
        Log.i(TAG, "The length of the audio list is " + AUDIO_LIST_SIZE);

        audioDetails.setText(itemList.get(position).getName());

        // In the beginning, the Pause button should be invisible.
        if (itemList.get(position).getId() == previousStoryID) {
            if (continueAddingSecondsToAudio)
                playButton.setVisibility(View.INVISIBLE);
            else
                pauseButton.setVisibility(View.INVISIBLE);
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
            updateNecessaryDetails();

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
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String message = AppConfig.SHARE_MESSAGE + itemList.get(position).getAudioURL();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Folk Fables");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
            Log.i(TAG, "Starting the chooser");
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        } else if (v == actionSave) {
            new Thread(new Runnable() {
                public void run() {
                    downloadFile();
                }
            }).start();
            Toast.makeText(this, "Downloading story", Toast.LENGTH_SHORT).show();
        } else if (v == actionComment) {
            Toast.makeText(this, "Comment on story", Toast.LENGTH_SHORT).show();
        } else if (v == actionInfo) {
            Intent intent = new Intent(this, StoryInfo.class);
            intent.putExtra(AppConfig.CURRENT_AUDIO_SELECTED, itemList.get(position).getName());
            intent.putExtra(AppConfig.AUDIO_DURATION, itemList.get(position).getDuration());
            intent.putExtra(AppConfig.UPLOADED_ON, convertTimeStamp(itemList.get(position).getTimeStamp()));
            intent.putExtra(AppConfig.AUDIO_DETAILS, itemList.get(position).getStatus());
            startActivity(intent);
        }
    }

    private String convertTimeStamp(String timeStamp) {
        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(timeStamp),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        return timeAgo.toString();
    }

    private void updateNecessaryDetails() {
        Log.i(TAG, "Audio paused. Updating necessary details");

        if (totalDurationOfTheAudio != 0) {
            // Displaying time full time of the audio and completed playing time
            fullTimeTextView.setText(utilities.milliSecondsToTimer(totalDurationOfTheAudio));
            currentTimeTextView.setText(utilities.milliSecondsToTimer(currentDurationOfTheAudio));

            // Updating progress bar
            int progress = utilities.getProgressPercentage(currentDurationOfTheAudio, totalDurationOfTheAudio);
            seekBar.setProgress(progress);
        }
    }

    private void downloadFile() {
        try {
            URL story_URL = new URL(itemList.get(position).getAudioURL());
            HttpURLConnection connection = (HttpURLConnection) story_URL.openConnection();

            connection.setRequestMethod(AppConfig.URL_REQUEST_GET);
            connection.setDoOutput(true);

            connection.connect();

            // Request permission when it is not granted.
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission: WRITE_EXTERNAL_STORAGE: NOT granted!");
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            // Set the path in the device where the audio file will be saved.
            File root = Environment.getExternalStorageDirectory();
            Log.i(TAG, "The root directory is " + root);

            File audioSavedFolder = new File(Environment.getExternalStorageDirectory() + "/FolkFables");
            boolean success = true;

            if (!audioSavedFolder.exists()) {
                success = audioSavedFolder.mkdirs();
            }

            if (success) {
                Log.i(TAG, "Directory created");
            } else {
                Log.i(TAG, "Directory not created");
            }

            File file = new File(audioSavedFolder, itemList.get(position).getName() + ".mp3");
            Log.i(TAG, "The name of the downloaded file is " + file.toString());

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            InputStream inputStream = connection.getInputStream();

            totalSizeOfTheDownloadedFile = connection.getContentLength();
            Log.i(TAG, "Size of the downloaded file is " + totalSizeOfTheDownloadedFile);

            /*runOnUiThread(new Runnable() {
                public void run() {
                    downloadProgressBar.setMax(totalSizeOfTheDownloadedFile);
                }
            });*/

            // Create a buffer.
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                // update the progressbar //
                /*runOnUiThread(new Runnable() {
                    public void run() {
                        downloadProgressBar.setProgress(downloadedSize);
                        float per = ((float)downloadedSize/totalSizeOfTheDownloadedFile) * 100;
                        //cur_val.setText("Downloaded " + downloadedSize + "KB / " + totalSizeOfTheDownloadedFile + "KB (" + (int)per + "%)" );
                    }
                });*/
            }

            // Close the output stream when download completes
            fileOutputStream.close();

            /*runOnUiThread(new Runnable() {
                public void run() {
                    // pb.dismiss(); // if you want close it..
                }
            });*/

        } catch (final MalformedURLException e) {
            Log.e(TAG, "Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            Log.e(TAG, "Error : IOException " + e);
            e.printStackTrace();
        } catch (final Exception e) {
            Log.e(TAG, "Error : Please check your internet connection " + e);
        }
    }
}