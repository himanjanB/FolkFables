package com.notify.listviewfacebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.notify.listviewfacebook.R;
import com.notify.listviewfacebook.config.AppConfig;

public class StoryInfo extends AppCompatActivity {
    private TextView nameOfTheStory;
    private TextView totalDuration;
    private TextView uploadedOn;
    private TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_info);

        Intent intent = getIntent();
        String storyName = intent.getStringExtra(AppConfig.CURRENT_AUDIO_SELECTED);
        String fullDuration = intent.getStringExtra(AppConfig.AUDIO_DURATION);
        String uploaded = intent.getStringExtra(AppConfig.UPLOADED_ON);
        String audioDetails = intent.getStringExtra(AppConfig.AUDIO_DETAILS);

        nameOfTheStory = (TextView) findViewById(R.id.nameOfTheStory);
        totalDuration = (TextView) findViewById(R.id.totalDurationInfo);
        uploadedOn = (TextView) findViewById(R.id.uploadedOnInfo);
        details = (TextView) findViewById(R.id.detailsOfStory);

        nameOfTheStory.setText(storyName);
        totalDuration.setText(getResources().getString(R.string.total_duration_info) + ": " + fullDuration);
        uploadedOn.setText(getResources().getString(R.string.uploaded_on) + ": " + uploaded);
        details.setText(audioDetails);
    }
}
