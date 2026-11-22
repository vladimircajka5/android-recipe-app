package com.siit.recipeapp.ui.animation;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.siit.recipeapp.R;

public final class EntranceMotion {

    // ===== Elementi =====

    private static final int[] CIRCLE_IDS = {
            R.id.view_register_circle_1,
            R.id.view_register_circle_2,
            R.id.view_register_circle_3,
            R.id.view_register_circle_4
    };

    private static final int[] LOGIN_IDS = {
            R.id.tv_login_title,
            R.id.tv_login_subtitle,
            R.id.til_login_identifier,
            R.id.til_login_password,
            R.id.tv_login_forgot_password,
            R.id.btn_login_lsumbit,
            R.id.btn_login_guest,
            R.id.btn_login_register
    };

    private static final int[] REGISTER_IDS = {
            R.id.tv_register_title,
            R.id.tv_register_subtitle,
            R.id.til_register_username,
            R.id.til_register_email,
            R.id.til_register_password,
            R.id.btn_register_back,
            R.id.btn_register_sumbit
    };

    private EntranceMotion() {
    }

    // ===== Funkcionalnosti =====

    // Animacija Login ekrana
    public static void playLogin(View root) {
        play(root, LOGIN_IDS);
    }

    // Animacija Register ekrana
    public static void playRegister(View root) {
        play(root, REGISTER_IDS);
    }

    // Zaustavljanje animacija
    public static void stopLogin(View root) {
        stop(root, LOGIN_IDS);
    }

    public static void stopRegister(View root) {
        stop(root, REGISTER_IDS);
    }

    // ===== Privatne metode =====

    // Pokretanje animacija ekrana
    private static void play(View root, int[] contentIds) {

        // Postuj sistemsko iskljucivanje animacija
        if (!ValueAnimator.areAnimatorsEnabled()) {
            return;
        }

        // Krugovi - zoom in i fade in
        for (int i = 0; i < CIRCLE_IDS.length; i++) {

            View circle = root.findViewById(CIRCLE_IDS[i]);

            if (circle == null) {
                continue;
            }

            float originalAlpha = circle.getAlpha();

            circle.setAlpha(0f);
            circle.setScaleX(0.65f);
            circle.setScaleY(0.65f);

            circle.animate()
                    .alpha(originalAlpha)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(i * 75L)
                    .setDuration(520)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Komponente - fade in i blagi pomak nagore
        float offset = 14f * root.getResources()
                .getDisplayMetrics().density;

        for (int i = 0; i < contentIds.length; i++) {

            View element = root.findViewById(contentIds[i]);

            if (element == null) {
                continue;
            }

            element.setAlpha(0f);
            element.setTranslationY(offset);

            element.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(90L + i * 55L)
                    .setDuration(360)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    // Zaustavljanje animacija pri zatvaranju ekrana
    private static void stop(View root, int[] contentIds) {

        for (int id : CIRCLE_IDS) {
            View circle = root.findViewById(id);

            if (circle != null) {
                circle.animate().cancel();
            }
        }

        for (int id : contentIds) {
            View element = root.findViewById(id);

            if (element != null) {
                element.animate().cancel();
            }
        }
    }
}
