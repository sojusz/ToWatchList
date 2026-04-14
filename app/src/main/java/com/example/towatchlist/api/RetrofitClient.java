package com.example.towatchlist.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static Retrofit watchmodeRetrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static TmdbApiService getTmdbService() {
        return getClient().create(TmdbApiService.class);
    }

    public static WatchmodeApiService getWatchmodeService() {
        if (watchmodeRetrofit == null) {
            watchmodeRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.WATCHMODE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return watchmodeRetrofit.create(WatchmodeApiService.class);
    }
}