package edu.bluejack24_2.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private Weather[] weather;

    @SerializedName("wind")
    private Wind wind;

    // --- Getters ---
    public String getName() { return name; }
    public Main getMain() { return main; }
    public Weather[] getWeather() { return weather; }
    public Wind getWind() { return wind; }

    public static class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp() { return temp; }
        public int getHumidity() { return humidity; }

        // Setter buat testing dummy data
        public void setTemp(double temp) { this.temp = temp; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getDescription() { return description; }
        public String getIcon() { return icon; }

        // Setter buat testing
        public void setDescription(String description) { this.description = description; }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        public double getSpeed() { return speed; }

        // Setter buat testing
        public void setSpeed(double speed) { this.speed = speed; }
    }

    // Setters Utama (Untuk Dummy Data di MainActivity)
    public void setName(String name) { this.name = name; }
    public void setMain(Main main) { this.main = main; }
    public void setWeather(Weather[] weather) { this.weather = weather; }
    public void setWind(Wind wind) { this.wind = wind; }
}