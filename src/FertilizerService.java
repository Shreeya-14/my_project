import java.util.*;
import java.util.Locale;

public class FertilizerService {

    public static class SoilReport {
        public double averagePH;
        public double averageNutrients;
        public int lowNutrientCount;
        public String phStatus;
        public String fertilizerSuggestion;
        public String soilMessage;

        public String toJson() {
            return String.format(Locale.US,
                "{\"averagePH\":%.2f,\"averageNutrients\":%.1f," +
                "\"lowNutrientCount\":%d,\"phStatus\":\"%s\"," +
                "\"fertilizerSuggestion\":\"%s\",\"soilMessage\":\"%s\"}",
                averagePH, averageNutrients, lowNutrientCount,
                phStatus, fertilizerSuggestion, soilMessage
            );
        }
    }

    public static SoilReport analyse(ArrayList<Sensor> sensors) {
        SoilReport report = new SoilReport();

        if (sensors.isEmpty()) {
            report.phStatus = "No data";
            report.fertilizerSuggestion = "No recommendation available.";
            report.soilMessage = "Add field sensor readings first.";
            return report;
        }

        double totalPH = 0;
        double totalNutrients = 0;
        int lowNutrients = 0;

        for (Sensor sensor : sensors) {
            totalPH += sensor.ph;
            totalNutrients += sensor.nutrients;

            if (sensor.nutrients < 30) {
                lowNutrients++;
            }
        }

        report.averagePH = totalPH / sensors.size();
        report.averageNutrients = totalNutrients / sensors.size();
        report.lowNutrientCount = lowNutrients;

        if (report.averagePH < 6.0) {
            report.phStatus = "Acidic";
            report.soilMessage =
                "Soil pH is below the preferred range.";
        } else if (report.averagePH > 7.2) {
            report.phStatus = "Alkaline";
            report.soilMessage =
                "Soil pH is above the preferred range.";
        } else {
            report.phStatus = "Balanced";
            report.soilMessage =
                "Average pH is within a generally suitable range.";
        }

        if (report.averageNutrients < 30) {
            report.fertilizerSuggestion =
                "Apply a balanced NPK fertilizer and organic compost.";
        } else if (report.averageNutrients < 50) {
            report.fertilizerSuggestion =
                "Use a moderate nutrient supplement; monitor NPK levels.";
        } else {
            report.fertilizerSuggestion =
                "Nutrient level is healthy; use only maintenance fertilization.";
        }

        return report;
    }
}