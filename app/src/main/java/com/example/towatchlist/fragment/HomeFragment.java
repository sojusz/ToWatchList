package com.example.towatchlist.fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.towatchlist.database.MovieDatabase;
import com.example.towatchlist.model.Movie;
import com.example.towatchlist.model.MovieResponse;
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

    private int currentPage = 1;
    private int totalPages = 1;
    private String selectedType = "movie";
    private String selectedSort = "popularity.desc";
    private String selectedPlatform = "";
    private String selectedGenreId = "";
    private String selectedYear = "";
    private boolean showingWatched = false;

    private ImageButton btnPrevPage, btnNextPage;
    private TextView txtPageInfo;
    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private TextInputEditText etSearch;
    private Spinner spinnerYear;
    private LinearLayout llGenreFilters;
    private Button btnSortBest, btnSortWorst;
    private SwitchCompat switchDarkMode;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] GENRES = {"Akcja", "Komedia", "Dramat", "Horror", "Sci-Fi", "Thriller"};
    private static final String[] GENRE_IDS = {"28", "35", "18", "27", "878", "53"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvMovies = view.findViewById(R.id.rv_movies);
        etSearch = view.findViewById(R.id.et_search);
        spinnerYear = view.findViewById(R.id.spinner_year);
        llGenreFilters = view.findViewById(R.id.ll_genre_filters);
        btnSortBest = view.findViewById(R.id.btn_sort_best);
        btnSortWorst = view.findViewById(R.id.btn_sort_worst);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        btnPrevPage = view.findViewById(R.id.btn_prev_page);
        btnNextPage = view.findViewById(R.id.btn_next_page);
        txtPageInfo = view.findViewById(R.id.txt_page_info);

        adapter = new MovieAdapter(requireContext(), this);
        rvMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMovies.setAdapter(adapter);

        setupTypeFilters(view);
        setupYearSpinner();
        setupDarkModeSwitch();
        setupGenreFilters();
        setupSearch();
        setupPagination();
        setupSorting();
        setupTypeFilters(view);
        setupPlatformFilters(view);

        fetchMovies();

        return view;
    }

    private void setupTypeFilters(View view) {
        LinearLayout llTypeFilters = view.findViewById(R.id.ll_type_filters);
        if (llTypeFilters == null) return;

        llTypeFilters.removeAllViews();

        // Definiujemy typy
        String[] types = {"Filmy", "Seriale"};
        String[] typeKeys = {"movie", "tv"};

        for (int i = 0; i < types.length; i++) {
            final String key = typeKeys[i];
            Button btn = new Button(requireContext());
            btn.setText(types[i]);

            // Wygląd przycisku (taki sam jak w Twoim XML)
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(selectedType.equals(key) ? R.color.blue_accent : R.color.navy)));
            btn.setTextColor(requireContext().getColor(R.color.white));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                selectedType = key;
                currentPage = 1;
                fetchMovies();
                setupTypeFilters(view); // Odśwież kolory
            });

            llTypeFilters.addView(btn);
        }
    }

    private void setupPlatformFilters(View view) {
        LinearLayout llPlatformFilters = view.findViewById(R.id.ll_platform_filters);
        if (llPlatformFilters == null) return;

        llPlatformFilters.removeAllViews();
        String[] platforms = {"Netflix", "Disney Plus", "HBO", "Amazon", "Apple TV"};
        for (String platform : platforms) {
            Button btn = new Button(requireContext());
            btn.setText(platform);
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            btn.setForeground(requireContext().getDrawable(outValue.resourceId));
            boolean isSelected = selectedPlatform.equals(platform);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(isSelected ? R.color.blue_accent : R.color.navy)));
            btn.setTextColor(requireContext().getColor(R.color.white));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                if (selectedPlatform.equals(platform)) {
                    selectedPlatform = "";
                } else {
                    selectedPlatform = platform;
                }
                applyPlatformFilter();
                setupPlatformFilters(view);
            });
            llPlatformFilters.addView(btn);
        }
    }

    private void applyPlatformFilter() {
        if (selectedPlatform.isEmpty()) {
            fetchMovies();
            return;
        }

        List<Movie> allMovies = adapter.getMovies();
        List<Movie> filteredList = new ArrayList<>();

        for (Movie m : allMovies) {
            if (m.getStreamingPlatforms() != null &&
                    m.getStreamingPlatforms().toLowerCase().contains(selectedPlatform.toLowerCase())) {
                filteredList.add(m);
            }
        }

        // Wyświetlamy tylko pasujące filmy
        adapter.setMovies(filteredList);
    }

    @Override
    public void onDeleteFromList(Movie movie) {
        executor.execute(() -> {
            MovieDatabase.getDatabase(requireContext()).movieDao().delete(movie);
            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Usunięto z listy", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupSorting() {
        View.OnClickListener sortListener = v -> {
            String clickedSort = (v.getId() == R.id.btn_sort_best) ? "vote_average.desc" : "vote_average.asc";

            // Logika TOGGLE: Jeśli kliknięty jest już aktywny, wyłączamy go (wracamy do popularności)
            if (selectedSort.equals(clickedSort)) {
                selectedSort = "popularity.desc";
            } else {
                selectedSort = clickedSort;
            }

            currentPage = 1;
            updateSortButtonColors(); // Zmiana kolorów
            fetchMovies();
        };

        btnSortBest.setOnClickListener(sortListener);
        btnSortWorst.setOnClickListener(sortListener);
    }

    private void updateSortButtonColors() {
        // Najlepsze
        int bestColor = selectedSort.equals("vote_average.desc") ? R.color.blue_accent : R.color.navy;
        btnSortBest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(bestColor)));

        // Najgorsze
        int worstColor = selectedSort.equals("vote_average.asc") ? R.color.blue_accent : R.color.navy;
        btnSortWorst.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(worstColor)));
    }

    private void fetchMovies() {
        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            searchMovies(query, currentPage);
        } else {
            discoverMovies();
        }
    }

    private void searchMovies(String query, int page) {
        Call<MovieResponse> call = selectedType.equals("movie") ?
                RetrofitClient.getTmdbService().searchMovies(Constants.TMDB_API_KEY, query, "pl-PL", currentPage) :
                RetrofitClient.getTmdbService().searchTv(Constants.TMDB_API_KEY, query, "pl-PL", currentPage);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                handleResponse(response);
            }
            @Override public void onFailure(Call<MovieResponse> call, Throwable t) { showError(); }
        });
    }

    private void discoverMovies() {
        Integer yearInt = selectedYear.isEmpty() || selectedYear.equals("Wszystkie lata") ? null : Integer.parseInt(selectedYear);

        Call<MovieResponse> call;
        if (selectedType.equals("movie")) {
            call = RetrofitClient.getTmdbService().discoverMovies(
                    Constants.TMDB_API_KEY, selectedGenreId, yearInt, null, selectedSort, "pl-PL", currentPage);
        } else {
            call = RetrofitClient.getTmdbService().discoverTv(
                    Constants.TMDB_API_KEY, selectedGenreId, yearInt, "pl-PL", currentPage);
        }

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                handleResponse(response);
            }
            @Override public void onFailure(Call<MovieResponse> call, Throwable t) { showError(); }
        });
    }

    private void handleResponse(Response<MovieResponse> response) {
        if (isAdded() && response.isSuccessful() && response.body() != null) {
            adapter.setMovies(response.body().getResults());
            totalPages = response.body().getTotalPages();
            txtPageInfo.setText("Strona " + currentPage + " z " + totalPages);
            btnPrevPage.setEnabled(currentPage > 1);
            btnNextPage.setEnabled(currentPage < totalPages);
        }
    }

    private void setupGenreFilters() {
        llGenreFilters.removeAllViews();
        for (int i = 0; i < GENRES.length; i++) {
            final String id = GENRE_IDS[i];
            final String label = GENRES[i];
            Button btn = new Button(requireContext());
            btn.setText(label);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(selectedGenreId.equals(id) ? R.color.blue_accent : R.color.navy)));
            btn.setTextColor(requireContext().getColor(R.color.white));

            btn.setOnClickListener(v -> {
                selectedGenreId = selectedGenreId.equals(id) ? "" : id;
                currentPage = 1;
                fetchMovies();
                setupGenreFilters();
            });
            llGenreFilters.addView(btn);
        }
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentPage = 1;
                fetchMovies();
                return true;
            }
            return false;
        });
    }

    private void setupPagination() {
        btnPrevPage.setOnClickListener(v -> { if (currentPage > 1) { currentPage--; fetchMovies(); } });
        btnNextPage.setOnClickListener(v -> { if (currentPage < totalPages) { currentPage++; fetchMovies(); } });
    }

    private void setupYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add("Wszystkie lata");
        for (int y = currentYear; y >= 1970; y--) years.add(String.valueOf(y));

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedYear = pos == 0 ? "" : years.get(pos);
                currentPage = 1;
                fetchMovies();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupDarkModeSwitch() {
        boolean isNightMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES;
        switchDarkMode.setChecked(isNightMode);
        switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            AppCompatDelegate.setDefaultNightMode(checked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    @Override
    public void onAddToList(Movie movie) {
        executor.execute(() -> {
            MovieDatabase.getDatabase(requireContext()).movieDao().insert(movie);
            if (isAdded()) requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Dodano!", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onMarkWatched(Movie movie) {
        executor.execute(() -> {
            movie.setWatched(true);
            MovieDatabase.getDatabase(requireContext()).movieDao().insert(movie);
            if (isAdded()) requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Obejrzano!", Toast.LENGTH_SHORT).show());
        });
    }

    private void showError() {
        if (isAdded()) Toast.makeText(requireContext(), "Błąd sieci", Toast.LENGTH_SHORT).show();
    }
}