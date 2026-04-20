package com.example.towatchlist.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.towatchlist.R;
import com.example.towatchlist.adapter.MovieAdapter;
import com.example.towatchlist.api.Constants;
import com.example.towatchlist.api.RetrofitClient;
import com.example.towatchlist.database.MovieDatabase; // Zmienione z AppDatabase
import com.example.towatchlist.model.Movie;
import com.example.towatchlist.model.MovieResponse;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewReleasesFragment extends Fragment implements MovieAdapter.OnMovieActionListener {

    private MovieAdapter adapter;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
//    private boolean showingWatched = false;
    private int currentPage = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_releases, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_new_releases);
        adapter = new MovieAdapter(requireContext(), this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadMovies();
        return view;
    }

    private void loadMovies() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        RetrofitClient.getTmdbService()
                .discoverMovies(Constants.TMDB_API_KEY, null, currentYear, null, "popularity.desc", "pl-PL", currentPage)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null) {
                            adapter.setMovies(response.body().getResults());
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Błąd: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onDeleteFromList(Movie movie) {
        executor.execute(() -> {
            MovieDatabase.getDatabase(requireContext()).movieDao().delete(movie);
        });
    }

    @Override
    public void onAddToList(Movie movie) {
        executor.execute(() -> {
            MovieDatabase.getDatabase(requireContext()).movieDao().insert(movie);
        });
    }

    @Override
    public void onMarkWatched(Movie movie) {
        executor.execute(() -> {
            movie.setWatched(true);
            MovieDatabase.getDatabase(requireContext()).movieDao().insert(movie);

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Oznaczono jako obejrzany!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}