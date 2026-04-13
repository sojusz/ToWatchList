package com.example.towatchlist.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "movies")
public class Movie {

    @PrimaryKey
    @NonNull
    @SerializedName("imdbID")
    private String imdbID;

    @SerializedName("Title")
    private String title;

    @SerializedName("Year")
    private String year;

    @SerializedName("Poster")
    private String posterUrl;

    @SerializedName("imdbRating")
    private String rating;

    @SerializedName("Plot")
    private String plot;

    @SerializedName("Genre")
    private String genre;

    @SerializedName("Actors")
    private String actors;

    @SerializedName("Type")
    private String type;

    // Dla lokalnej bazy danych
    private boolean isWatched = false;
    private boolean isInMyList = false;
    private String streamingPlatforms = "";

    // Gettery i settery
    @NonNull
    public String getImdbID() { return imdbID; }
    public void setImdbID(@NonNull String imdbID) { this.imdbID = imdbID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isWatched() { return isWatched; }
    public void setWatched(boolean watched) { isWatched = watched; }

    public boolean isInMyList() { return isInMyList; }
    public void setInMyList(boolean inMyList) { isInMyList = inMyList; }

    public String getStreamingPlatforms() { return streamingPlatforms; }
    public void setStreamingPlatforms(String streamingPlatforms) { this.streamingPlatforms = streamingPlatforms; }
}