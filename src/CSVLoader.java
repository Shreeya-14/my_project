import java.io.*;
import java.util.*;

public class CSVLoader {
    public static ArrayList<Sensor> loadSensors(String path)
        throws IOException {

        ArrayList<Sensor> sensors = new ArrayList<Sensor>();
        BufferedReader reader = new BufferedReader(new FileReader(path));

        reader.readLine(); // Skip CSV header.

        String line;
        while ((line = reader.readLine()) != null) {
            String[] v = line.split(",");

            if (v.length != 10) {
                continue;
            }

            sensors.add(new Sensor(
                v[0],
                Integer.parseInt(v[1]),
                Integer.parseInt(v[2]),
                Integer.parseInt(v[3]),
                Double.parseDouble(v[4]),
                Integer.parseInt(v[5]),
                Integer.parseInt(v[6]),
                Integer.parseInt(v[7]),
                v[8],
                v[9]
            ));
        }

        reader.close();
        return sensors;
    }
}