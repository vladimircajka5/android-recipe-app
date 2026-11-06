package com.siit.recipeapp.ui.recipe;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.siit.recipeapp.R;

public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeViewHolder> {

    // Svojstva
    private final boolean[] favorites = {false, false};

    // Constructor
    public RecipeCardAdapter() {
    }

    // Kreiranje kartice
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);

        return new RecipeViewHolder(view);
    }

    // Popunjavanje kartice
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {

        // Privremeni podaci za prikaz dve kartice
        if (position == 0) {
            holder.tvTitle.setText("Homemade Pancakes");
            holder.tvDescription.setText("Soft and fluffy pancakes with fresh fruit.");
        } else {
            holder.tvTitle.setText("Berry Pancakes");
            holder.tvDescription.setText("Delicious homemade pancakes with sweet berries.");
        }

        holder.ivImage.setImageResource(R.drawable.recipe_sample_pancakes);

        // Promena statusa favorita
        holder.btnFavorite.setOnClickListener(v -> {

            int index = holder.getBindingAdapterPosition();

            if (index == RecyclerView.NO_POSITION) {
                return;
            }

            favorites[index] = !favorites[index];
            updateFavorite(holder, index);
        });

        updateFavorite(holder, position);
    }

    // Broj kartica
    @Override
    public int getItemCount() {
        return 2;
    }

    // Azuriranje izgleda srceta
    private void updateFavorite(RecipeViewHolder holder, int position) {

        if (favorites[position]) {
            holder.btnFavorite.setImageResource(R.drawable.ic_recipe_favorite_filled);

            int primary = MaterialColors.getColor(holder.itemView,
                    androidx.appcompat.R.attr.colorPrimary);

            holder.btnFavorite.setImageTintList(ColorStateList.valueOf(primary));

            holder.btnFavorite.setContentDescription(
                    holder.itemView.getContext().getString(R.string.recipe_card_remove_favorite)
            );

        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_recipe_favorite_outline);

            int onSurface = MaterialColors.getColor(holder.itemView,
                    com.google.android.material.R.attr.colorOnSurface);

            holder.btnFavorite.setImageTintList(ColorStateList.valueOf(onSurface));

            holder.btnFavorite.setContentDescription(
                    holder.itemView.getContext().getString(R.string.recipe_card_add_favorite)
            );
        }
    }

    // Elementi kartice
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvTitle;
        TextView tvDescription;
        ImageButton btnFavorite;

        // Constructor
        public RecipeViewHolder(@NonNull View view) {
            super(view);

            ivImage = view.findViewById(R.id.iv_recipe_card_image);
            tvTitle = view.findViewById(R.id.tv_recipe_card_title);
            tvDescription = view.findViewById(R.id.tv_recipe_card_description);
            btnFavorite = view.findViewById(R.id.btn_recipe_card_favorite);
        }
    }
}
