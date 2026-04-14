package com.example.towatchlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.towatchlist.R;
import com.example.towatchlist.api.Constants;
import com.example.towatchlist.api.RetrofitClient;
import com.example.towatchlist.model.Movie;
import com.example.towatchlist.model.WatchmodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies = new ArrayList<>();
    private Context context;
    private OnMovieActionListener listener;
    private int expandedPosition = -1;

    private HashMap<String, String> platformsCache = new HashMap<>();

    public interface OnMovieActionListener {
        void onAddToList(Movie movie);
        void onMarkWatched(Movie movie);
    }

    public MovieAdapter(Context context, OnMovieActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        expandedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        // 1. Prawidłowe pobranie pozycji i obiektu
        int currentPosition = holder.getAdapterPosition();
        Movie movie = movies.get(currentPosition);
        boolean isExpanded = currentPosition == expandedPosition;

        // Collapsed view
        holder.txtTitle.setText(movie.getTitle());
        String posterUrl = movie.getPosterUrl();
        if (posterUrl != null && !posterUrl.equals("N/A") && posterUrl.startsWith("http")) {
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgPoster);
        } else {
            holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Expanded view
        holder.txtTitleBig.setText(movie.getTitle());
        holder.txtYear.setText(movie.getYear() != null ? movie.getYear() : "");

        String rating = movie.getRating();
        holder.txtRating.setText("⭐ " + (rating != null && !rating.equals("N/A") ? rating + "/10" : "brak oceny"));

        holder.txtPlot.setText(movie.getPlot() != null ? movie.getPlot() : "Brak opisu");

        if (posterUrl != null && !posterUrl.equals("N/A") && posterUrl.startsWith("http")) {
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgPosterBig);
        } else {
            holder.imgPosterBig.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Toggle expanded visibility
        holder.layoutCollapsed.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.layoutExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // 2. Obsługa kliknięcia (używamy currentPosition)
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            int prev = expandedPosition;
            if (isExpanded) {
                expandedPosition = -1;
                notifyItemChanged(pos);
            } else {
                expandedPosition = pos;
                if (prev != -1) notifyItemChanged(prev);
                notifyItemChanged(pos);

                // Załaduj szczegóły jeśli brak
                if (movie.getPlot() == null || movie.getPlot().isEmpty()) {
                    loadMovieDetails(movie, holder, pos);
                }
                loadPlatforms(movie, holder);
            }
        });

        holder.btnAddToList.setOnClickListener(v -> listener.onAddToList(movie));
        holder.btnMarkWatched.setOnClickListener(v -> listener.onMarkWatched(movie));
    }

    private void loadPlatforms(Movie movie, MovieViewHolder holder) {
        if (movie.getImdbID() == null) return;

        if (platformsCache.containsKey(movie.getImdbID())) {
            holder.txtPlatforms.setText(platformsCache.get(movie.getImdbID()));
            return;
        }

        holder.txtPlatforms.setText("📺 Ładowanie platform...");

        RetrofitClient.getWatchmodeService().getSources(
                movie.getImdbID(), Constants.WATCHMODE_API_KEY
        ).enqueue(new Callback<List<WatchmodeSource>>() {
            @Override
            public void onResponse(Call<List<WatchmodeSource>> call, Response<List<WatchmodeSource>> response) {
                String text;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    StringBuilder sb = new StringBuilder("📺 ");
                    List<String> names = new ArrayList<>();
                    for (WatchmodeSource src : response.body()) {
                        if (!names.contains(src.getName())) {
                            names.add(src.getName());
                        }
                    }
                    // 3. Bezpieczne łączenie tekstów (zamiast String.join)
                    for (int i = 0; i < names.size(); i++) {
                        sb.append(names.get(i));
                        if (i < names.size() - 1) sb.append(", ");
                    }
                    text = sb.toString();
                } else {
                    text = "📺 Brak informacji o platformach";
                }

                platformsCache.put(movie.getImdbID(), text);
                movie.setStreamingPlatforms(text);
                holder.txtPlatforms.setText(text);
            }

            @Override
            public void onFailure(Call<List<WatchmodeSource>> call, Throwable t) {
                holder.txtPlatforms.setText("📺 Błąd ładowania platform");
            }
        });
    }

    private void loadMovieDetails(Movie movie, MovieViewHolder holder, int position) {
        RetrofitClient.getOmdbService().getMovieById(
                movie.getImdbID(), Constants.OMDB_API_KEY, "full"
        ).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie details = response.body();
                    movie.setPlot(details.getPlot());
                    movie.setRating(details.getRating());
                    movie.setActors(details.getActors());
                    notifyItemChanged(position);
                }
            }
            @Override
            public void onFailure(Call<Movie> call, Throwable t) {}
        });
    }

    public HashMap<String, String> getPlatformsCache() {
        return platformsCache;
    }

    @Override
    public int getItemCount() { return movies.size(); }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutCollapsed, layoutExpanded;
        ImageView imgPoster, imgPosterBig;
        TextView txtTitle, txtTitleBig, txtRating, txtYear, txtPlot, txtPlatforms;
        Button btnAddToList, btnMarkWatched;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCollapsed = itemView.findViewById(R.id.layout_collapsed);
            layoutExpanded = itemView.findViewById(R.id.layout_expanded);
            imgPoster = itemView.findViewById(R.id.img_poster);
            imgPosterBig = itemView.findViewById(R.id.img_poster_big);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtTitleBig = itemView.findViewById(R.id.txt_title_big);
            txtRating = itemView.findViewById(R.id.txt_rating);
            txtYear = itemView.findViewById(R.id.txt_year);
            txtPlot = itemView.findViewById(R.id.txt_plot);
            txtPlatforms = itemView.findViewById(R.id.txt_platforms);
            btnAddToList = itemView.findViewById(R.id.btn_add_to_list);
            btnMarkWatched = itemView.findViewById(R.id.btn_mark_watched);
        }
    }
}