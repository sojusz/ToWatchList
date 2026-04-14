package com.example.towatchlist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.towatchlist.model.Movie;
import java.util.List;

@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);

    @Query("SELECT * FROM movies_table")
    LiveData<List<Movie>> getAllMovies();

    @Query("SELECT * FROM movies_table WHERE id = :movieId LIMIT 1")
    Movie getMovieById(int movieId);

    @Query("DELETE FROM movies_table WHERE id = :movieId")
    void deleteById(int movieId);
}