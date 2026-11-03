package com.siit.recipeapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.siit.recipeapp.ui.auth.LoginFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Android Studio Default
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Postavi LoginFragment u odeljak(container) njegov
        if (savedInstanceState == null) { // da ne menja ako vec postoji(pri rotaciji na primer)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }

    }
}