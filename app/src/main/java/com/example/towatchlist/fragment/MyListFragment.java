package com.example.towatchlist.fragment;

import android.os.Bundle;
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
import com.example.towatchlist.database.MovieDatabase;
import com.example.towatchlist.model.Movie;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        if (tabLayout != null) {
            if (tabLayout.getTabCount() == 0) {
                tabLayout.addTab(tabLayout.newTab().setText("Do obejrzenia"));
                tabLayout.addTab(tabLayout.newTab().setText("Obejrzane"));
            }
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    showingWatched = tab.getPosition() == 1;
                    loadList();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        loadList();
        return view;
    }

    private void loadList() {
        adapter.setFromMyList(true);
        MovieDatabase.getDatabase(requireContext()).movieDao().getAllMovies()
                .observe(getViewLifecycleOwner(), movies -> {
                    List<Movie> filteredList = new ArrayList<>();
                    for (Movie m : movies) {
                        if (m.isWatched() == showingWatched) {
                            filteredList.add(m);
                        }
                    }
                    adapter.setMovies(filteredList);
                });
    }

    @Override
    public void onAddToList(Movie movie) {
        // Film już jest w bazie, możemy go np. usunąć jeśli użytkownik kliknie ponownie
    }

    @Override
    public void onDeleteFromList(Movie movie) {
        executor.execute(() -> {
            MovieDatabase.getDatabase(requireContext()).movieDao().delete(movie);
        });
    }

    @Override
    public void onMarkWatched(Movie movie) {
        executor.execute(() -> {
            movie.setWatched(true);
            MovieDatabase.getDatabase(requireContext()).movieDao().insert(movie);
        });
    }
}