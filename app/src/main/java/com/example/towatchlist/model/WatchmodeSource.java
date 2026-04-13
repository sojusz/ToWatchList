package com.example.towatchlist.model;

import com.google.gson.annotations.SerializedName;

public class WatchmodeSource {
    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("web_url")
    private String webUrl;

    public String getName() { return name; }
    public String getType() { return type; }
    public String getWebUrl() { return webUrl; }
}