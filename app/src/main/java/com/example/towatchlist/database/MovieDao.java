package com.example.towatchlist.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.towatchlist.model.Movie;
import java.util.List;

@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);

    @Query("SELECT * FROM movies WHERE isInMyList = 1")
    LiveData<List<Movie>> getMyList();

    @Query("SELECT * FROM movies WHERE isWatched = 1")
    LiveData<List<Movie>> getWatched();

    @Query("SELECT * FROM movies WHERE imdbID = :id")
    Movie getById(String id);

    @Update
    void update(Movie movie);
}