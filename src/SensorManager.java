import java.util.*;

public class SensorManager {
    private ArrayList<Sensor> sensors;
    private KDTree kdTree;
    private RangeIndex rangeIndex;
    private FractionalCascade fractionalCascade;
    private int nextSensorNumber;

    public SensorManager(ArrayList<Sensor> sensors) {
        this.sensors = sensors;
        nextSensorNumber = sensors.size() + 1;
        rebuildIndexes();
    }

    public void rebuildIndexes() {
        kdTree = new KDTree(sensors);
        rangeIndex = new RangeIndex(sensors);
        fractionalCascade = new FractionalCascade(sensors);
    }

    public ArrayList<Sensor> getAllSensors() {
        return sensors;
    }

    public KDTree.NearestResult nearestSensor(int x, int y) {
        return kdTree.findNearest(x, y);
    }

    public RangeIndex.RangeResult rangeSearch(
        int xmin, int xmax, int ymin, int ymax
    ) {
        return rangeIndex.search(xmin, xmax, ymin, ymax);
    }

    public FractionalCascade.CascadeResult fractionalSearch(
        int moisture
    ) {
        return fractionalCascade.search(moisture);
    }

    public ArrayList<IrrigationService.IrrigationZone> getZones() {
        return IrrigationService.generateZones(sensors);
    }

    public FertilizerService.SoilReport getSoilReport() {
        return FertilizerService.analyse(sensors);
    }

    public Sensor addRandomSensor() {
        Random random = new Random();

        Sensor sensor = new Sensor(
            String.format("S-%02d", nextSensorNumber++),
            5 + random.nextInt(91),
            5 + random.nextInt(91),
            20 + random.nextInt(66),
            5.7 + random.nextDouble() * 1.3,
            20 + random.nextInt(16),
            45 + random.nextInt(41),
            15 + random.nextInt(66),
            "Moderate",
            "Medium"
        );

        sensors.add(sensor);
        rebuildIndexes();

        return sensor;
    }

    public boolean removeSensor(String id) {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).id.equals(id)) {
                sensors.remove(i);
                rebuildIndexes();
                return true;
            }
        }

        return false;
    }
}