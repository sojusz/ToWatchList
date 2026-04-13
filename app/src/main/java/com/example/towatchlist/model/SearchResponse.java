package com.example.towatchlist.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {
    @SerializedName("Search")
    private List<Movie> movies;

    @SerializedName("totalResults")
    private String totalResults;

    @SerializedName("Response")
    private String response;

    public List<Movie> getMovies() { return movies; }
    public String getTotalResults() { return totalResults; }
    public String getResponse() { return response; }
}