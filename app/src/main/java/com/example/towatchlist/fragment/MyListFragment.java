package com.example.towatchlist.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.towatchlist.R;
import com.example.towatchlist.adapter.MovieAdapter;
import com.example.towatchlist.database.AppDatabase;
import com.example.towatchlist.model.Movie;
import com.google.android.material.tabs.TabLayout;
import java.util.*;
import java.util.concurrent.*;

public class MyListFragment extends Fragment implements MovieAdapter.OnMovieActionListener {

    private MovieAdapter adapter;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean showingWatched = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_list, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_my_list);
        adapter = new MovieAdapter(requireContext(), this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_list);
        tabLayout.addTab(tabLayout.newTab().setText("Do obejrzenia"));
        tabLayout.addTab(tabLayout.newTab().setText("Obejrzane"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingWatched = tab.getPosition() == 1;
                loadList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadList();
        return view;
    }

    private void loadList() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        if (showingWatched) {
            db.getWatched().observe(getViewLifecycleOwner(), movies -> adapter.setMovies(movies));
        } else {
            db.getMyList().observe(getViewLifecycleOwner(), movies -> {
                List<Movie> toWatch = new ArrayList<>();
                for (Movie m : movies) {
                    if (!m.isWatched()) toWatch.add(m);
                }
                adapter.setMovies(toWatch);
            });
        }
    }

    @Override
    public void onAddToList(Movie movie) { /* już na liście */ }

    @Override
    public void onMarkWatched(Movie movie) {
        executor.execute(() -> {
            movie.setWatched(true);
            AppDatabase.getInstance(requireContext()).movieDao().update(movie);
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Oznaczono jako obejrzany!", Toast.LENGTH_SHORT).show());
        });
    }
}