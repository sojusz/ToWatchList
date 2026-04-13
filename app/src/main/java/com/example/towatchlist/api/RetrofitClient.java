package com.example.towatchlist.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit omdbRetrofit;
    private static Retrofit watchmodeRetrofit;

    public static OmdbApiService getOmdbService() {
        if (omdbRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            omdbRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.OMDB_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return omdbRetrofit.create(OmdbApiService.class);
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