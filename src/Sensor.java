import java.util.Locale;

public class Sensor {
    public String id;
    public int x, y, moisture, temperature, humidity, nutrients;
    public double ph;
    public String cropHealth, waterAvailable;

    public Sensor(String id, int x, int y, int moisture, double ph,
                  int temperature, int humidity, int nutrients,
                  String cropHealth, String waterAvailable) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.moisture = moisture;
        this.ph = ph;
        this.temperature = temperature;
        this.humidity = humidity;
        this.nutrients = nutrients;
        this.cropHealth = cropHealth;
        this.waterAvailable = waterAvailable;
    }

    public String toJson() {
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"x\":%d,\"y\":%d,\"moisture\":%d," +
            "\"ph\":%.1f,\"temperature\":%d,\"humidity\":%d," +
            "\"nutrients\":%d,\"cropHealth\":\"%s\"," +
            "\"waterAvailable\":\"%s\"}",
            id, x, y, moisture, ph, temperature, humidity,
            nutrients, cropHealth, waterAvailable
        );
    }
}