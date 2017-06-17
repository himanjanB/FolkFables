package com.notify.listviewfacebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.data.FeedItem;
import com.notify.listviewfacebook.services.PlayMusic;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = StartActivity.class.getSimpleName();

    private static int position;
    private static int AUDIO_LIST_SIZE;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button previousButton;
    private TextView audioDetails;
    private ArrayList<FeedItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        playButton = (Button) findViewById(R.id.buttonPlay);
        pauseButton = (Button) findViewById(R.id.buttonPause);
        nextButton = (Button) findViewById(R.id.buttonNext);
        previousButton = (Button) findViewById(R.id.buttonPrevious);
        audioDetails = (TextView) findViewById(R.id.audioPlaying);

        pauseButton.setEnabled(false);

        Intent intent = getIntent();
        itemList = (ArrayList<FeedItem>) intent.getSerializableExtra("AUDIO_LIST");
        position = intent.getIntExtra("POSITION", 0);

        AUDIO_LIST_SIZE = itemList.size();
        Log.i(TAG, "The length of the audio list is " + AUDIO_LIST_SIZE);

        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == playButton) {
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            audioDetails.setText(itemList.get(position).getName());
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        } else if (v == pauseButton) {
            pauseButton.setEnabled(false);
            playButton.setEnabled(true);
            stopService(new Intent(this, PlayMusic.class));
        } else if (v == nextButton) {
            stopService(new Intent(this, PlayMusic.class));
            Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the next position");
            position++;
            if (position > AUDIO_LIST_SIZE - 1) {
                position = 0;
            }
            audioDetails.setText(itemList.get(position).getName());
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        } else if (v == previousButton) {
            stopService(new Intent(this, PlayMusic.class));
            Log.i(TAG, "The current audio that was playing is " + itemList.get(position).getAudioURL());
            Log.i(TAG, "Moving to the previous position");
            position--;
            if (position < 0) {
                position = AUDIO_LIST_SIZE - 1;
            }
            audioDetails.setText(itemList.get(position).getName());
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", itemList.get(position).getAudioURL());
            startService(intent);
        }
    }
}