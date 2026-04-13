package com.example.towatchlist.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.towatchlist.R;
import com.example.towatchlist.adapter.MovieAdapter;
import com.example.towatchlist.api.Constants;
import com.example.towatchlist.api.RetrofitClient;
import com.example.towatchlist.database.AppDatabase;
import com.example.towatchlist.model.Movie;
import com.example.towatchlist.model.SearchResponse;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MovieAdapter.OnMovieActionListener {

    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private TextInputEditText etSearch, etActor;
    private Spinner spinnerYear;
    private LinearLayout llGenreFilters, llPlatformFilters, llTypeFilters;
    private Button btnSortBest, btnSortWorst;
    private SwitchCompat switchDarkMode;

    private String selectedGenre = "";
    private String selectedPlatform = "";
    private String selectedType = "movie";
    private String selectedYear = "";
    private boolean sortAscending = false;
    private boolean isSortingActive = false;

    // FIX: flaga blokująca pętlę switcha
    private boolean isSwitchUpdating = false;

    private List<Movie> currentMovies = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] GENRES = {"Akcja", "Komedia", "Dramat", "Horror", "Sci-Fi",
            "Thriller", "Romans", "Animacja", "Dokumentalny", "Fantasy"};
    private static final String[] GENRE_KEYS = {"action", "comedy", "drama", "horror", "sci-fi",
            "thriller", "romance", "animation", "documentary", "fantasy"};
    private static final String[] PLATFORMS = {"Netflix", "HBO", "Disney+", "Amazon", "Apple TV"};
    private static final String[] TYPES = {"Film", "Serial"};
    private static final String[] TYPE_KEYS = {"movie", "series"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvMovies = view.findViewById(R.id.rv_movies);
        etSearch = view.findViewById(R.id.et_search);
        etActor = view.findViewById(R.id.et_actor);
        spinnerYear = view.findViewById(R.id.spinner_year);
        llGenreFilters = view.findViewById(R.id.ll_genre_filters);
        llPlatformFilters = view.findViewById(R.id.ll_platform_filters);
        llTypeFilters = view.findViewById(R.id.ll_type_filters);
        btnSortBest = view.findViewById(R.id.btn_sort_best);
        btnSortWorst = view.findViewById(R.id.btn_sort_worst);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        adapter = new MovieAdapter(requireContext(), this);
        rvMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMovies.setAdapter(adapter);

        setupYearSpinner();
        setupDarkModeSwitch();
        setupFilters();
        setupSearch();
        setupSorting();

        return view;
    }

    // ── FIX: Spinner z latami ──────────────────────────────────────────────
    private void setupYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add("Wszystkie lata");
        for (int y = currentYear; y >= 1970; y--) {
            years.add(String.valueOf(y));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Domyślnie ustaw aktualny rok (pozycja 1)
        spinnerYear.setSelection(1);
        selectedYear = String.valueOf(currentYear);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true; // ignoruj pierwsze automatyczne wywołanie

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                    // Załaduj filmy z aktualnym rokiem przy starcie
                    selectedYear = String.valueOf(currentYear);
                    searchMovies(getSearchQuery(), selectedYear);
                    return;
                }
                selectedYear = position == 0 ? "" : years.get(position);
                searchMovies(getSearchQuery(), selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── FIX: Dark mode switch bez pętli ───────────────────────────────────
    private void setupDarkModeSwitch() {
        boolean isNightMode = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        isSwitchUpdating = true;
        switchDarkMode.setChecked(isNightMode);
        isSwitchUpdating = false;

        switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            if (isSwitchUpdating) return;
            isSwitchUpdating = true;
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            isSwitchUpdating = false;
        });
    }

    // ── FIX: Sortowanie z podświetleniem ──────────────────────────────────
    private void setupSorting() {
        btnSortBest.setOnClickListener(v -> {
            if (isSortingActive && !sortAscending) {
                // Odznacz
                isSortingActive = false;
                setButtonActive(btnSortBest, false);
                adapter.setMovies(currentMovies);
                return;
            }
            sortAscending = false;
            isSortingActive = true;
            setButtonActive(btnSortBest, true);
            setButtonActive(btnSortWorst, false);
            sortMovies();
        });

        btnSortWorst.setOnClickListener(v -> {
            if (isSortingActive && sortAscending) {
                // Odznacz
                isSortingActive = false;
                setButtonActive(btnSortWorst, false);
                adapter.setMovies(currentMovies);
                return;
            }
            sortAscending = true;
            isSortingActive = true;
            setButtonActive(btnSortWorst, true);
            setButtonActive(btnSortBest, false);
            sortMovies();
        });
    }

    private void setButtonActive(Button btn, boolean active) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                requireContext().getColor(active ? R.color.blue_accent : R.color.navy)));
    }

    private void sortMovies() {
        if (currentMovies.isEmpty()) return;
        List<Movie> sorted = new ArrayList<>(currentMovies);
        sorted.sort((a, b) -> {
            try {
                double ra = Double.parseDouble(a.getRating() != null ? a.getRating() : "0");
                double rb = Double.parseDouble(b.getRating() != null ? b.getRating() : "0");
                return sortAscending ? Double.compare(ra, rb) : Double.compare(rb, ra);
            } catch (NumberFormatException e) {
                return 0;
            }
        });
        adapter.setMovies(sorted);
    }

    private String getSearchQuery() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        String actor = etActor.getText() != null ? etActor.getText().toString().trim() : "";
        return !actor.isEmpty() ? actor : (!query.isEmpty() ? query : "film");
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMovies(getSearchQuery(), selectedYear);
                return true;
            }
            return false;
        });

        etActor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchMovies(getSearchQuery(), selectedYear);
                return true;
            }
            return false;
        });
    }

    private void setupFilters() {
        // Typ
        for (int i = 0; i < TYPES.length; i++) {
            final String key = TYPE_KEYS[i];
            final String label = TYPES[i];
            Button btn = createFilterButton(label);
            btn.setOnClickListener(v -> {
                selectedType = key;
                resetButtonColors(llTypeFilters);
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.blue_accent)));
                searchMovies(getSearchQuery(), selectedYear);
            });
            llTypeFilters.addView(btn);
        }

        // Gatunki
        for (int i = 0; i < GENRES.length; i++) {
            final String genre = GENRE_KEYS[i];
            final String label = GENRES[i];
            Button btn = createFilterButton(label);
            btn.setOnClickListener(v -> {
                selectedGenre = selectedGenre.equals(genre) ? "" : genre;
                resetButtonColors(llGenreFilters);
                if (!selectedGenre.isEmpty()) {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            requireContext().getColor(R.color.blue_accent)));
                }
                searchMovies(getSearchQuery(), selectedYear);
            });
            llGenreFilters.addView(btn);
        }

        // Platformy
        for (String platform : PLATFORMS) {
            Button btn = createFilterButton(platform);
            btn.setOnClickListener(v -> {
                selectedPlatform = selectedPlatform.equals(platform) ? "" : platform;
                resetButtonColors(llPlatformFilters);
                if (!selectedPlatform.isEmpty()) {
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            requireContext().getColor(R.color.blue_accent)));
                }
                filterByPlatform();
            });
            llPlatformFilters.addView(btn);
        }
    }

    private Button createFilterButton(String label) {
        Button btn = new Button(requireContext());
        btn.setText(label);
        btn.setTextSize(11f);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.navy)));
        btn.setTextColor(requireContext().getColor(R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(params);
        return btn;
    }

    private void resetButtonColors(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if (v instanceof Button) {
                ((Button) v).setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.navy)));
            }
        }
    }

    private void filterByPlatform() {
        if (selectedPlatform.isEmpty()) {
            adapter.setMovies(currentMovies);
        } else {
            List<Movie> filtered = new ArrayList<>();
            for (Movie m : currentMovies) {
                if (m.getStreamingPlatforms() != null &&
                        m.getStreamingPlatforms().toLowerCase()
                                .contains(selectedPlatform.toLowerCase())) {
                    filtered.add(m);
                }
            }
            adapter.setMovies(filtered);
        }
    }

    private void searchMovies(String query, String year) {
        String fullQuery = query;
        if (!selectedGenre.isEmpty()) {
            fullQuery = query + " " + selectedGenre;
        }

        RetrofitClient.getOmdbService().searchMovies(
                fullQuery,
                Constants.OMDB_API_KEY,
                selectedType,
                year.isEmpty() ? null : year,
                1
        ).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    currentMovies = response.body().getMovies();
                    adapter.setMovies(currentMovies);
                    // Zastosuj sortowanie jeśli aktywne
                    if (isSortingActive) sortMovies();
                } else {
                    Toast.makeText(requireContext(), "Brak wyników", Toast.LENGTH_SHORT).show();
                    adapter.setMovies(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Błąd sieci: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToList(Movie movie) {
        executor.execute(() -> {
            movie.setInMyList(true);
            AppDatabase.getInstance(requireContext()).movieDao().insert(movie);
            if (!isAdded() || getActivity() == null) return;
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
            if (!isAdded() || getActivity() == null) return;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Oznaczono jako obejrzany!",
                            Toast.LENGTH_SHORT).show());
        });
    }
}