package com.example.towatchlist.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {
    // TMDb zwraca listę filmów pod kluczem "results"
    @SerializedName("results")
    private List<Movie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public List<Movie> getResults() {
        return results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }
}