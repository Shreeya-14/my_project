import java.util.*;
import java.util.Locale;

public class IrrigationService {

    public static class IrrigationZone {
        public String name;
        public int xmin, xmax, ymin, ymax;
        public ArrayList<Sensor> sensors;
        public double averageMoisture;
        public int drySensorCount;
        public String status;
        public String action;

        public IrrigationZone(
            String name, int xmin, int xmax, int ymin, int ymax
        ) {
            this.name = name;
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
            this.sensors = new ArrayList<Sensor>();
        }

        public String toJson() {
            return String.format(Locale.US,
                "{\"name\":\"%s\",\"xmin\":%d,\"xmax\":%d," +
                "\"ymin\":%d,\"ymax\":%d,\"sensorCount\":%d," +
                "\"averageMoisture\":%.1f,\"drySensorCount\":%d," +
                "\"status\":\"%s\",\"action\":\"%s\"}",
                name, xmin, xmax, ymin, ymax, sensors.size(),
                averageMoisture, drySensorCount, status, action
            );
        }
    }

    public static ArrayList<IrrigationZone> generateZones(
        ArrayList<Sensor> allSensors
    ) {
        ArrayList<IrrigationZone> zones =
            new ArrayList<IrrigationZone>();

        zones.add(new IrrigationZone(
            "North-West", 0, 50, 50, 100
        ));
        zones.add(new IrrigationZone(
            "North-East", 51, 100, 50, 100
        ));
        zones.add(new IrrigationZone(
            "South-West", 0, 50, 0, 49
        ));
        zones.add(new IrrigationZone(
            "South-East", 51, 100, 0, 49
        ));

        for (Sensor sensor : allSensors) {
            for (IrrigationZone zone : zones) {
                if (sensor.x >= zone.xmin &&
                    sensor.x <= zone.xmax &&
                    sensor.y >= zone.ymin &&
                    sensor.y <= zone.ymax) {

                    zone.sensors.add(sensor);
                    break;
                }
            }
        }

        for (IrrigationZone zone : zones) {
            calculateZoneStatus(zone);
        }

        return zones;
    }

    private static void calculateZoneStatus(IrrigationZone zone) {
        if (zone.sensors.isEmpty()) {
            zone.averageMoisture = 0;
            zone.drySensorCount = 0;
            zone.status = "No data";
            zone.action = "Add sensors to this irrigation zone.";
            return;
        }

        double total = 0;
        int dryCount = 0;

        for (Sensor sensor : zone.sensors) {
            total += sensor.moisture;

            if (sensor.moisture < 35) {
                dryCount++;
            }
        }

        zone.averageMoisture = total / zone.sensors.size();
        zone.drySensorCount = dryCount;

        if (zone.averageMoisture < 35) {
            zone.status = "Irrigate now";
            zone.action =
                "Activate irrigation for 25 minutes, then recheck moisture.";
        } else if (zone.averageMoisture < 60) {
            zone.status = "Monitor";
            zone.action =
                "Monitor this zone. Schedule irrigation only if moisture falls.";
        } else {
            zone.status = "Healthy";
            zone.action =
                "No immediate irrigation needed.";
        }
    }

    public static String getRecommendation(ArrayList<Sensor> sensors) {
        if (sensors.isEmpty()) {
            return "No sensor data is available in the selected zone.";
        }

        double total = 0;

        for (Sensor sensor : sensors) {
            total += sensor.moisture;
        }

        double average = total / sensors.size();

        if (average < 35) {
            return String.format(Locale.US,
                "Irrigate now. Selected-zone average moisture is %.1f%%.",
                average
            );
        }

        if (average < 60) {
            return String.format(Locale.US,
                "Monitor the zone. Selected-zone average moisture is %.1f%%.",
                average
            );
        }

        return String.format(Locale.US,
            "No irrigation is needed. Selected-zone average moisture is %.1f%%.",
            average
        );
    }
}