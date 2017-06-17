package com.notify.listviewfacebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.notify.listviewfacebook.activities.StartActivity;
import com.notify.listviewfacebook.adapter.FeedListAdapter;
import com.notify.listviewfacebook.app.AppController;
import com.notify.listviewfacebook.config.AppConfig;
import com.notify.listviewfacebook.data.FeedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView storyList;
    private FeedListAdapter storyListAdapter;
    private ArrayList<FeedItem> feedItemList;
    private String URL_FEED = AppConfig.JSON_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storyList = (ListView) findViewById(R.id.storyList);
        feedItemList = new ArrayList<>();

        storyListAdapter = new FeedListAdapter(this, feedItemList);
        storyList.setAdapter(storyListAdapter);

        // Click event for single list row
        storyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                FeedItem item = (FeedItem) storyList.getItemAtPosition(position);
                Log.i(TAG, "The audio URL is " + item.getAudioURL());
                Log.i(TAG, "Audio detail is " + item.getName());
                startDownloadingMP3(item.getAudioURL(), item.getName(), feedItemList, position);
            }
        });

        // We first check for cached request
        /*Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URL_FEED);
        if (entry != null) {
            // Fetch the data from cache
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJSONFeed(new JSONObject(data));
                    Log.i(TAG, "Cached JSON Parsing complete");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {*/

        // Making fresh volley request and getting JSON
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                URL_FEED, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                parseJSONFeed(response);
                Log.i(TAG, "JSON Parsing complete");
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error in parsing JSON " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }

    private void startDownloadingMP3(String audioURL, String audioDetail, ArrayList<FeedItem> storyList, int position) {
        Log.i(TAG, "Starting download.. " + audioURL);
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("AUDIO_URL", audioURL);
        intent.putExtra("AUDIO_DETAILS", audioDetail);
        intent.putExtra("AUDIO_LIST", storyList);
        intent.putExtra("POSITION", position);
        startActivity(intent);
    }

    /**
     * Parsing JSON response and passing the data to adapter
     */
    private void parseJSONFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setId(feedObj.getInt("id"));
                item.setName(feedObj.getString("name"));

                // Image might be null sometimes
                String image = feedObj.isNull("image") ? null : feedObj
                        .getString("image");
                item.setImge(image);
                item.setStatus(feedObj.getString("status"));
                item.setProfilePic(feedObj.getString("profilePic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));

                // URL might be null sometimes
                String feedUrl = feedObj.isNull("url") ? null : feedObj
                        .getString("url");
                item.setUrl(feedUrl);
                Log.i(TAG, "Music URL is: " + feedObj.getString("audioURL"));
                item.setAudioURL(feedObj.getString("audioURL"));
                Log.i("LOG", "Adding item");
                feedItemList.add(item);
            }

            // Notify data changes to list adapter
            storyListAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
}