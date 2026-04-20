package com.example.towatchlist.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.towatchlist.api.Constants;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "movies_table") // TO JEST KLUCZOWE
public class Movie implements Serializable {

    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("name")
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private double rating;

    @SerializedName("overview")
    private String plot;

    private boolean isWatched = false;

    private String streamingPlatforms;

    public Movie(int id, String title, String name, String posterPath, String releaseDate, double rating, String plot) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.plot = plot;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else if (name != null && !name.isEmpty()) {
            return name;
        }
        return "Brak tytułu";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) return null;
        return Constants.TMDB_IMAGE_BASE_URL + posterPath;
    }
    public String getYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "N/A";
    }
    public String getStreamingPlatforms() { return streamingPlatforms; }
    public boolean isWatched() { return isWatched; }
    public void setWatched(boolean watched) { isWatched = watched; }
    public void setStreamingPlatforms(String streamingPlatforms) { this.streamingPlatforms = streamingPlatforms; }

}