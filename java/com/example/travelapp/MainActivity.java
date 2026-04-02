package com.example.travelapp;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button sign_up_button;
    private Button signInButton;
    private FirebaseAuth mAuth;
    private EditText emailEdit, passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Get references to UI components
        emailEdit = findViewById(R.id.editTextTextEmailAddress);
        passwordEdit = findViewById(R.id.editTextTextPassword);
        signInButton = findViewById(R.id.button);
        sign_up_button = findViewById(R.id.button1);


        // Sign in button click listener
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(email, password);
                }
            }
        });

        sign_up_button = findViewById(R.id.button1);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // starting the sign up activity (sign up page) when the button is clicked
                Intent intent = new Intent(MainActivity.this, Sign_up.class);
                startActivity(intent);
                //open the sign up page
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    // Method to handle user sign-in
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success, navigate to home or main activity
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(MainActivity.this, "Sign-in successful", Toast.LENGTH_SHORT).show();

                            // Navigate to the main screen after sign-in
                            Intent intent = new Intent(MainActivity.this, Home.class);  // Replace HomeActivity with your home page activity
                            startActivity(intent);
                            finish();  // Finish this activity to prevent back navigation to sign-in page
                        }
                    } else {
                        // If sign-in fails, display a message to the user
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getWeatherDetails(View view) {
    }
}