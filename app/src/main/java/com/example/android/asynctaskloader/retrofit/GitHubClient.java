package com.example.android.asynctaskloader.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Gregorio on 11/11/2017.
 */

public interface GitHubClient {

    @GET("/repositories")
    Call <List<GitHubRepo>> reposFromName(@Query("q") String query, @Query("sort") String sort);


    @GET("/users/{user}/repos")
    Call<List<GitHubRepo>> reposForUser(@Path("user") String user);
}
