package com.example.towatchlist.api;

import com.example.towatchlist.model.WatchmodeSource;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;

public interface WatchmodeApiService {

    @GET("title/{id}/sources/")
    Call<List<WatchmodeSource>> getSources(
            @Path("id") String imdbId,
            @Query("apiKey") String apiKey
    );
}