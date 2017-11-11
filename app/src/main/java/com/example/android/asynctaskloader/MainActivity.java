/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.asynctaskloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.asynctaskloader.retrofit.GitHubClient;
import com.example.android.asynctaskloader.retrofit.GitHubRepo;
import com.example.android.asynctaskloader.utilities.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// TODO (1) implement LoaderManager.LoaderCallbacks<String> on MainActivity
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    String API_BASE_URL = "https://api.github.com";

    final static String sortBy = "stars";

    /* A constant to save and restore the URL that is being displayed */
    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    // TODO (28) Remove the key for storing the search results JSON
    /* A constant to save and restore the JSON that is being displayed */
    private static final String SEARCH_RESULTS_RAW_JSON = "results";

    // TODO (2) Create a constant int to uniquely identify your loader. Call it GITHUB_SEARCH_LOADER
    private static final int GITHUB_SEARCH_LOADER = 101;

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private GitHubClient gitHubClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);

        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);

            // TODO (26) Remove the code that retrieves the JSON
            String rawJsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS_RAW_JSON);

            mUrlDisplayTextView.setText(queryUrl);
            // TODO (25) Remove the code that displays the JSON
            mSearchResultsTextView.setText(rawJsonSearchResults);
        }

    }

    /**
     * This method retrieves the search text from the EditText, constructs the
     * URL (using {@link NetworkUtils}) for the github repository you'd like to find, displays
     * that URL in a TextView, and finally request that an AsyncTaskLoader performs the GET request.
     */
    private void makeGithubSearchQuery() {

        final String githubQuery = mSearchBoxEditText.getText().toString();

        Log.d(LOG_TAG, "GitHub Query is : " + githubQuery);


        // TODO (17) If no search was entered, indicate that there isn't anything to search for and return
        if (TextUtils.isEmpty(githubQuery)) {
            mUrlDisplayTextView.setText("No query entered, nothing to search for.");
            return;

        } else {

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson));

            Retrofit retrofit = builder.client(httpClient.build()).build();

            // Create a very simple REST adapter which points the GitHub API endpoint.
            gitHubClient = retrofit.create(GitHubClient.class);

            // Fetch a list of the Github repositories.
           // Call<List<GitHubRepo>> call = gitHubClient.reposForUser(githubQuery);

            Call<List<GitHubRepo>> call = gitHubClient.reposFromName(githubQuery, sortBy);

            // Execute the call asynchronously. Get a positive or negative callback.
            call.enqueue(new Callback<List<GitHubRepo>>() {
                @Override
                public void onResponse(Call<List<GitHubRepo>> call, Response<List<GitHubRepo>> response) {

                    Log.d(MainActivity.class.getSimpleName(), "Retrofit Response: " + response.code());

                    if (response.isSuccessful()) {

                        showJsonDataView();

                        List<GitHubRepo> repos = response.body();

                        for (GitHubRepo gitHubRepo : repos) {

                            Log.d(MainActivity.class.getSimpleName(), gitHubRepo.getName());
                            Log.d(MainActivity.class.getSimpleName(), String.valueOf(gitHubRepo.getId()));

                            String id = String.valueOf(gitHubRepo.getId());
                            String name = gitHubRepo.getName();
                            String url = gitHubRepo.getUrl();

                            mUrlDisplayTextView.setText(response.toString());

                            mSearchResultsTextView.append("\n" + id + "\n" + name + "\n" + url);
                            //Toast.makeText(MainActivity.this, id + name, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Retrofit response is null", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<List<GitHubRepo>> call, Throwable t) {

                    // the network call was a failure
                    // TODO: handle error
                    showErrorMessage();
                    t.getStackTrace();
                    Toast.makeText(MainActivity.this, "Error Connecting", Toast.LENGTH_LONG).show();
                }

            });
        }
    }

    /**
     * This method will make the View for the JSON data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showJsonDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the JSON data is visible */
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String queryUrl = mUrlDisplayTextView.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);

    }
}