package com.siit.recipeapp.ui.splash;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.siit.recipeapp.MainActivity;
import com.siit.recipeapp.R;

public class SplashFragment extends Fragment {

    // ===== Elementi =====

    private View splashView;
    private ImageView logo;
    private TextView title;
    private TextView credit;

    private boolean restored;

    // Zavrsavanje splash ekrana
    private final Runnable finishRunnable = () -> {

        if (!isAdded() || getView() == null) {
            return;
        }

        ((MainActivity) requireActivity()).finishSplash();
    };

    // Constructor
    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        splashView = view;
        logo = view.findViewById(R.id.iv_splash_logo);
        title = view.findViewById(R.id.tv_splash_wordmark);
        credit = view.findViewById(R.id.tv_splash_credit);
        restored = savedInstanceState != null;

        // ===== GIF =====

        // Ucitavanje animirane ikonice
        Glide.with(this)
                .asGif()
                .load(R.raw.animated_icon)
                .into(logo);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Preskoci animaciju pri obnavljanju fragmenta
        // ili kada su sistemske animacije iskljucene
        if (restored || !ValueAnimator.areAnimatorsEnabled()) {
            splashView.post(finishRunnable);
            return;
        }

        // ===== Animacije =====

        float density = getResources()
                .getDisplayMetrics().density;

        // GIF radi u centru 1 sekundu, pa se pomera ulevo
        logo.animate()
                .translationX(-90f * density)
                .setStartDelay(1000)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Naslov se nakon 1 sekunde izvlaci iza oklagije
        title.animate()
                .alpha(1f)
                .translationX(72f * density)
                .setStartDelay(1000)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Zavrsavanje splash ekrana nakon 3 sekunde
        splashView.postDelayed(finishRunnable, 3000);
    }

    @Override
    public void onPause() {

        // ===== Zaustavljanje animacija =====

        splashView.removeCallbacks(finishRunnable);

        logo.animate().cancel();
        title.animate().cancel();
        credit.animate().cancel();

        // Vrati pocetne pozicije
        logo.setTranslationX(0f);

        title.setTranslationX(45f * getResources().getDisplayMetrics().density);
        title.setAlpha(0f);

        credit.setAlpha(0f);

        super.onPause();
    }

    @Override
    public void onDestroyView() {

        // Oslobodi GIF i reference na elemente
        Glide.with(this).clear(logo);

        splashView = null;
        logo = null;
        title = null;
        credit = null;

        super.onDestroyView();
    }
}
