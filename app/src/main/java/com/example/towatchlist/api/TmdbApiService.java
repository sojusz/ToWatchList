package com.example.towatchlist.api;

import com.example.towatchlist.model.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApiService {

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreId,
            @Query("primary_release_year") Integer year,
            @Query("with_cast") String actorId,
            @Query("sort_by") String sortBy,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("search/tv")
    Call<MovieResponse> searchTv(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("discover/tv")
    Call<MovieResponse> discoverTv(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreId,
            @Query("first_air_date_year") Integer year,
            @Query("language") String language,
            @Query("sort_by") String sortBy, // <--- DODAJ TO
            @Query("page") int page
    );
}