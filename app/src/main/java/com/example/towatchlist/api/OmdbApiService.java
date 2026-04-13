package com.example.towatchlist.api;

import com.example.towatchlist.model.Movie;
import com.example.towatchlist.model.SearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbApiService {

    // Wyszukiwanie po tytule
    @GET(".")
    Call<SearchResponse> searchMovies(
            @Query("s") String title,
            @Query("apikey") String apiKey,
            @Query("type") String type,
            @Query("y") String year,
            @Query("page") int page
    );

    // Szczegóły filmu po ID
    @GET(".")
    Call<Movie> getMovieById(
            @Query("i") String imdbId,
            @Query("apikey") String apiKey,
            @Query("plot") String plot
    );

    // Wyszukiwanie po aktorze
    @GET(".")
    Call<SearchResponse> searchByActor(
            @Query("s") String query,
            @Query("apikey") String apiKey
    );
}