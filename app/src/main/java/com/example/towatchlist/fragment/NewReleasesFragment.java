package com.example.towatchlist.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.towatchlist.R;
import com.example.towatchlist.adapter.MovieAdapter;
import com.example.towatchlist.api.Constants;
import com.example.towatchlist.api.RetrofitClient;
import com.example.towatchlist.database.AppDatabase;
import com.example.towatchlist.model.*;
import java.util.*;
import java.util.concurrent.*;
import retrofit2.*;

public class NewReleasesFragment extends Fragment implements MovieAdapter.OnMovieActionListener {

    private MovieAdapter adapter;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_releases, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_new_releases);
        adapter = new MovieAdapter(requireContext(), this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadNewReleases();
        return view;
    }

    private void loadNewReleases() {
        String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
        RetrofitClient.getOmdbService().searchMovies(
                "2024", Constants.OMDB_API_KEY, null, currentYear, 1
        ).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (!isAdded() || getContext() == null) return; // <-- FIX
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    adapter.setMovies(response.body().getMovies());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return; // <-- FIX
                Toast.makeText(requireContext(), "Błąd ładowania nowości", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToList(Movie movie) {
        executor.execute(() -> {
            movie.setInMyList(true);
            AppDatabase.getInstance(requireContext()).movieDao().insert(movie);
            if (!isAdded() || getActivity() == null) return; // <-- FIX
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Dodano do listy!", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onMarkWatched(Movie movie) {
        executor.execute(() -> {
            movie.setWatched(true);
            movie.setInMyList(true);
            AppDatabase.getInstance(requireContext()).movieDao().insert(movie);
            if (!isAdded() || getActivity() == null) return; // <-- FIX
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Oznaczono jako obejrzany!", Toast.LENGTH_SHORT).show());
        });
    }
}