package com.notify.listviewfacebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.services.PlayMusic;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button previousButton;
    private Button forwardButton;
    private Button reverseButton;
    private static String AUDIO_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        playButton = (Button) findViewById(R.id.buttonPlay);
        pauseButton = (Button) findViewById(R.id.buttonPause);
        nextButton = (Button) findViewById(R.id.buttonNext);
        previousButton = (Button) findViewById(R.id.buttonPrevious);
        forwardButton = (Button) findViewById(R.id.buttonForward);
        reverseButton = (Button) findViewById(R.id.buttonReverse);

        pauseButton.setEnabled(false);

        Intent intent = getIntent();
        AUDIO_URL = intent.getStringExtra("AUDIO_URL");

        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
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
            Intent intent = new Intent(this, PlayMusic.class);
            intent.putExtra("AUDIO_URL", AUDIO_URL);
            startService(intent);
        } else if (v == pauseButton) {
            pauseButton.setEnabled(false);
            playButton.setEnabled(true);

            stopService(new Intent(this, PlayMusic.class));
        }
    }
}