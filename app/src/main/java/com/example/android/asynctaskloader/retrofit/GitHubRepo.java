package com.example.android.asynctaskloader.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Gregorio on 11/11/2017.
 */

public class GitHubRepo {

    public GitHubRepo(int id, String name) {
    }

    @SerializedName("id")
    private Integer id;



    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }


}
