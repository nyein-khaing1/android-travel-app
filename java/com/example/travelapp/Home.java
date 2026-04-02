package com.example.travelapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelapp.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {
private ActivityHomeBinding bind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bind.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, Flight1Activity.class));
            }
        });


        // Add click listener for the weather icon
        bind.weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Weather activity
                startActivity(new Intent(Home.this, Weather.class));
            }
        });

    }
}
