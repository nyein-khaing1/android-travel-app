package com.example.travelapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Weather extends AppCompatActivity {
    // UI Components
    private EditText etCity, etCountry;
    private TextView tvResult, tvCurrentTemp, tvLocation;
    private ImageView weatherIcon;
    private View divider;

    // API Constants
    private static final String TAG = "WeatherApp";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "e53301e27efa0b66d05045d91b2742d3";

    // Formatter for temperature values
    private final DecimalFormat df = new DecimalFormat("#.##");

    // Volley request queue
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);

        // Initialize UI components
        initializeViews();

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    //Initialize all the views used in the activity
    private void initializeViews() {
        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        tvResult = findViewById(R.id.tvResult);

        // Find additional UI components if present in the layout
        try {
            tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
            tvLocation = findViewById(R.id.tvLocation);
            weatherIcon = findViewById(R.id.weatherIcon);
            divider = findViewById(R.id.divider);
        } catch (Exception e) {
            Log.w(TAG, "Some UI components are not found in the layout. Using basic layout.", e);
        }
    }


     // Handle the weather request when the button is clicked
    public void getWeatherDetails(View view) {
        // Clear previous results
        if (tvResult != null) {
            tvResult.setText("");
        }

        // Hide additional UI components if they exist
        hideAdditionalComponents();

        // Get input values
        String city = etCity != null ? etCity.getText().toString().trim() : "";
        String country = etCountry != null ? etCountry.getText().toString().trim() : "";

        // Validate input
        if (city.isEmpty()) {
            showMessage("Please enter a city name");
            return;
        }

        // Show loading message
        if (tvResult != null) {
            tvResult.setText("Fetching weather data...");
        }

        // Build URL
        String requestUrl = buildWeatherRequestUrl(city, country);

        // Make API Request
        makeWeatherApiRequest(requestUrl);
    }


     // Hide additional UI components if they exist

    private void hideAdditionalComponents() {
        if (tvCurrentTemp != null) tvCurrentTemp.setVisibility(View.GONE);
        if (tvLocation != null) tvLocation.setVisibility(View.GONE);
        if (weatherIcon != null) weatherIcon.setVisibility(View.GONE);
        if (divider != null) divider.setVisibility(View.GONE);
    }


     //Show a message to the user

    private void showMessage(String message) {
        if (tvResult != null) {
            tvResult.setText(message);
            tvResult.setTextColor(Color.RED);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


     //Build the weather API request URL

    private String buildWeatherRequestUrl(String city, String country) {
        if (!country.isEmpty()) {
            return WEATHER_API_URL + "?q=" + city + "," + country + "&appid=" + API_KEY;
        } else {
            return WEATHER_API_URL + "?q=" + city + "&appid=" + API_KEY;
        }
    }


     // Make the API request to get weather data

    private void makeWeatherApiRequest(String url) {
        // Create a string request with proper error handling
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,  // Use GET instead of POST for better cache behavior
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleApiError(error);
                    }
                }
        );

        // Add the request to the queue
        if (requestQueue != null) {
            requestQueue.add(stringRequest);
        } else {
            showMessage("Network request failed. Please try again.");
        }
    }


     // Handle the API response
    private void handleApiResponse(String response) {
        if (response == null || response.isEmpty()) {
            showMessage("Received empty response from server");
            return;
        }

        try {
            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response);

            // Extract weather data
            String cityName = jsonResponse.optString("name", "Unknown City");

            JSONObject sysObj = jsonResponse.optJSONObject("sys");
            String countryName = sysObj != null ? sysObj.optString("country", "") : "";

            JSONArray weatherArray = jsonResponse.optJSONArray("weather");
            String description = "";
            if (weatherArray != null && weatherArray.length() > 0) {
                JSONObject weatherObj = weatherArray.optJSONObject(0);
                if (weatherObj != null) {
                    description = weatherObj.optString("description", "No description available");
                    // Capitalize first letter of description
                    if (!description.isEmpty()) {
                        description = description.substring(0, 1).toUpperCase() + description.substring(1);
                    }
                }
            }

            JSONObject mainObj = jsonResponse.optJSONObject("main");
            double temp = 0;
            double feelsLike = 0;
            int pressure = 0;
            int humidity = 0;

            if (mainObj != null) {
                temp = mainObj.optDouble("temp", 273.15) - 273.15; // Convert from Kelvin
                feelsLike = mainObj.optDouble("feels_like", 273.15) - 273.15;
                pressure = mainObj.optInt("pressure", 0);
                humidity = mainObj.optInt("humidity", 0);
            }

            JSONObject windObj = jsonResponse.optJSONObject("wind");
            double windSpeed = windObj != null ? windObj.optDouble("speed", 0) : 0;

            JSONObject cloudsObj = jsonResponse.optJSONObject("clouds");
            int cloudiness = cloudsObj != null ? cloudsObj.optInt("all", 0) : 0;

            // Display weather data
            displayWeatherData(cityName, countryName, temp, feelsLike, humidity, pressure,
                    description, windSpeed, cloudiness);

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response", e);
            showMessage("Error processing weather data. Please try again.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            showMessage("An unexpected error occurred. Please try again.");
        }
    }


     //Handle API error response

    private void handleApiError(VolleyError error) {
        Log.e(TAG, "API Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), error);

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;

            if (statusCode == 404) {
                showMessage("City not found. Please check the spelling and try again.");
            } else if (statusCode >= 500) {
                showMessage("Weather service is temporarily unavailable. Please try again later.");
            } else {
                showMessage("Error: " + statusCode + ". Please try again.");
            }
        } else if (!isNetworkAvailable()) {
            showMessage("No internet connection. Please check your network settings.");
        } else {
            showMessage("Unable to connect to weather service. Please try again.");
        }
    }


     // Check if network is available
    private boolean isNetworkAvailable() {
        // This is a simplified check. In a real app, you should use ConnectivityManager
        return true;
    }


     // Display the weather data in the UI
    private void displayWeatherData(String city, String country, double temp, double feelsLike,
                                    int humidity, int pressure, String description,
                                    double windSpeed, int cloudiness) {

        // Update the main result text
        if (tvResult != null) {
            StringBuilder output = new StringBuilder();
            output.append("Current weather of ").append(city);

            if (!country.isEmpty()) {
                output.append(" (").append(country).append(")");
            }

            output.append("\n Temp: ").append(df.format(temp)).append(" °C")
                    .append("\n Feels Like: ").append(df.format(feelsLike)).append(" °C")
                    .append("\n Humidity: ").append(humidity).append("%")
                    .append("\n Description: ").append(description)
                    .append("\n Wind Speed: ").append(windSpeed).append(" m/s (meters per second)")
                    .append("\n Cloudiness: ").append(cloudiness).append("%")
                    .append("\n Pressure: ").append(pressure).append(" hPa");

            tvResult.setText(output.toString());
            tvResult.setTextColor(Color.rgb(68, 134, 199));
        }

        // Update additional UI components if they exist
        updateAdditionalComponents(city, country, temp);
    }

     //Update additional UI components with weather data
    private void updateAdditionalComponents(String city, String country, double temp) {
        if (tvCurrentTemp != null) {
            tvCurrentTemp.setText(df.format(temp) + "°C");
            tvCurrentTemp.setVisibility(View.VISIBLE);
        }

        if (tvLocation != null) {
            String location = city;
            if (!country.isEmpty()) {
                location += ", " + country;
            }
            tvLocation.setText(location);
            tvLocation.setVisibility(View.VISIBLE);
        }

        if (divider != null) {
            divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        // Cancel any pending requests
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
        super.onDestroy();
    }
}