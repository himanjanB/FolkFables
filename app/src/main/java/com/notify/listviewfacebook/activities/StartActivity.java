package com.notify.listviewfacebook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.data.FeedItem;
import com.notify.listviewfacebook.services.PlayMusic;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = StartActivity.class.getSimpleName();

    private static int position;
    private static int AUDIO_LIST_SIZE;
    private static String AUDIO_PLAYING = "";
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

    private TextView profileName;
    private TextView audioDetails;
    private ArrayList<FeedItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(PlayMusic.FOLK_FABLE_MESSAGE);
                if (message.equalsIgnoreCase("Complete")) {
                    pauseButton.setVisibility(View.INVISIBLE);
                    playButton.setVisibility(View.VISIBLE);
                }
            }
        };

        playButton = (ImageView) findViewById(R.id.buttonPlay);
        pauseButton = (ImageView) findViewById(R.id.buttonPause);
        nextButton = (ImageView) findViewById(R.id.buttonNext);
        previousButton = (ImageView) findViewById(R.id.buttonPrevious);
        rewindButton = (ImageView) findViewById(R.id.buttonReverse);
        ffButton = (ImageView) findViewById(R.id.buttonForward);
        actionShare = (ImageView) findViewById(R.id.action_share);
        actionSave = (ImageView) findViewById(R.id.action_save);
        actionComment = (ImageView) findViewById(R.id.action_comment);
        actionInfo = (ImageView) findViewById(R.id.action_info);

        audioDetails = (TextView) findViewById(R.id.storyNameTextView);
        profileName = (TextView) findViewById(R.id.profileNameTextView);

        Log.i(TAG, "Audio Playing is " + AUDIO_PLAYING);
        if (!AUDIO_PLAYING.equalsIgnoreCase("")) {
            audioDetails.setText(AUDIO_PLAYING);
        }

        pauseButton.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        itemList = (ArrayList<FeedItem>) intent.getSerializableExtra("AUDIO_LIST");
        position = intent.getIntExtra("POSITION", 0);

        AUDIO_LIST_SIZE = itemList.size();
        Log.i(TAG, "The length of the audio list is " + AUDIO_LIST_SIZE);

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

    @Override
    public void onClick(View v) {
        if (v == playButton) {
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            audioDetails.setText(itemList.get(position).getName());
            AUDIO_PLAYING = audioDetails.getText().toString();
            profileName.setText("Folk Fables");
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        } else if (v == pauseButton) {
            pauseButton.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            stopService(new Intent(this, PlayMusic.class));
        } else if (v == nextButton) {
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            stopService(new Intent(this, PlayMusic.class));
            Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the next position");
            position++;
            if (position > AUDIO_LIST_SIZE - 1) {
                position = 0;
            }
            audioDetails.setText(itemList.get(position).getName());
            AUDIO_PLAYING = audioDetails.getText().toString();
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        } else if (v == previousButton) {
            playButton.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            stopService(new Intent(this, PlayMusic.class));
            Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the previous position");
            position--;
            if (position < 0) {
                position = AUDIO_LIST_SIZE - 1;
            }
            audioDetails.setText(itemList.get(position).getName());
            AUDIO_PLAYING = audioDetails.getText().toString();
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        } else if (v == rewindButton) {
            Toast.makeText(this, "Rewinding", Toast.LENGTH_SHORT).show();
        } else if (v == ffButton) {
            Toast.makeText(this, "Forwarding", Toast.LENGTH_SHORT).show();
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