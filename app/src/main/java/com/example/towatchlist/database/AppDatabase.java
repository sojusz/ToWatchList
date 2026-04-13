package com.example.towatchlist.database;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.towatchlist.model.Movie;
import java.util.List;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract MovieDao movieDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "towatchlist_db"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    // Metody pomocnicze delegujące do DAO
    public LiveData<List<Movie>> getMyList() {
        return movieDao().getMyList();
    }

    public LiveData<List<Movie>> getWatched() {
        return movieDao().getWatched();
    }
}