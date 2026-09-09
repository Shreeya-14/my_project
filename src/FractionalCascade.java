import java.util.*;

public class FractionalCascade {

    /*
     * Each catalog entry stores a sensor reading.
     * bridgeToNext is the linked position in the next catalog.
     */
    private static class CatalogEntry {
        Sensor sensor;
        int moisture;
        int bridgeToNext;

        CatalogEntry(Sensor sensor) {
            this.sensor = sensor;
            this.moisture = sensor.moisture;
            this.bridgeToNext = -1;
        }
    }

    private ArrayList<CatalogEntry> mainCatalog;
    private ArrayList<CatalogEntry> northCatalog;
    private ArrayList<CatalogEntry> southCatalog;

    public FractionalCascade(ArrayList<Sensor> sensors) {
        ArrayList<Sensor> northSensors = new ArrayList<Sensor>();
        ArrayList<Sensor> southSensors = new ArrayList<Sensor>();

        for (Sensor sensor : sensors) {
            if (sensor.y >= 50) {
                northSensors.add(sensor);
            } else {
                southSensors.add(sensor);
            }
        }

        /*
         * Build bottom catalog first.
         * Then build augmented catalogs with sampled entries from
         * the catalog below them.
         */
        southCatalog = baseCatalog(southSensors);

        northCatalog = buildAugmentedCatalog(
            baseCatalog(northSensors),
            southCatalog
        );

        mainCatalog = buildAugmentedCatalog(
            baseCatalog(sensors),
            northCatalog
        );
    }

    private ArrayList<CatalogEntry> baseCatalog(
        ArrayList<Sensor> sensors
    ) {
        ArrayList<CatalogEntry> catalog =
            new ArrayList<CatalogEntry>();

        for (Sensor sensor : sensors) {
            catalog.add(new CatalogEntry(sensor));
        }

        sortCatalog(catalog);

        return catalog;
    }

    /*
     * Fractional Cascading idea:
     * Merge the real catalog with every second entry of the next catalog.
     */
    private ArrayList<CatalogEntry> buildAugmentedCatalog(
        ArrayList<CatalogEntry> base,
        ArrayList<CatalogEntry> next
    ) {
        ArrayList<CatalogEntry> augmented =
            new ArrayList<CatalogEntry>();

        for (CatalogEntry entry : base) {
            augmented.add(new CatalogEntry(entry.sensor));
        }

        for (int i = 0; i < next.size(); i += 2) {
            augmented.add(new CatalogEntry(next.get(i).sensor));
        }

        sortCatalog(augmented);

        /*
         * Store bridge positions from this catalog to the next catalog.
         */
        for (CatalogEntry entry : augmented) {
            entry.bridgeToNext = lowerBound(next, entry.moisture);
        }

        return augmented;
    }

    private void sortCatalog(ArrayList<CatalogEntry> catalog) {
        Collections.sort(catalog, new Comparator<CatalogEntry>() {
            public int compare(CatalogEntry a, CatalogEntry b) {
                return Integer.compare(a.moisture, b.moisture);
            }
        });
    }

    /*
     * One normal binary search.
     * Used only for the first catalog.
     */
    private int lowerBound(
        ArrayList<CatalogEntry> catalog,
        int target
    ) {
        int low = 0;
        int high = catalog.size();

        while (low < high) {
            int middle = (low + high) / 2;

            if (catalog.get(middle).moisture < target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }

        return Math.min(low, catalog.size() - 1);
    }

    /*
     * Uses a bridge pointer and only local corrections.
     * There is no second full binary search.
     */
    private int followBridge(
        ArrayList<CatalogEntry> currentCatalog,
        int currentPosition,
        ArrayList<CatalogEntry> nextCatalog,
        int target
    ) {
        int nextPosition =
            currentCatalog.get(currentPosition).bridgeToNext;

        if (nextPosition >= nextCatalog.size()) {
            nextPosition = nextCatalog.size() - 1;
        }

        /*
         * Correct nearby positions.
         * In true fractional cascading, sampling keeps this correction small.
         */
        while (nextPosition > 0 &&
               nextCatalog.get(nextPosition - 1).moisture >= target) {
            nextPosition--;
        }

        while (nextPosition < nextCatalog.size() - 1 &&
               nextCatalog.get(nextPosition).moisture < target) {
            nextPosition++;
        }

        return nextPosition;
    }

    public CascadeResult search(int targetMoisture) {
        CascadeResult result = new CascadeResult();

        /*
         * First and only binary search.
         */
        result.mainPosition =
            lowerBound(mainCatalog, targetMoisture);

        /*
         * Subsequent catalogs are reached through bridge pointers.
         */
        result.northPosition = followBridge(
            mainCatalog,
            result.mainPosition,
            northCatalog,
            targetMoisture
        );

        result.southPosition = followBridge(
            northCatalog,
            result.northPosition,
            southCatalog,
            targetMoisture
        );

        result.mainSensor =
            mainCatalog.get(result.mainPosition).sensor;

        result.northSensor =
            northCatalog.get(result.northPosition).sensor;

        result.southSensor =
            southCatalog.get(result.southPosition).sensor;

        return result;
    }

    public static class CascadeResult {
        public int mainPosition;
        public int northPosition;
        public int southPosition;

        public Sensor mainSensor;
        public Sensor northSensor;
        public Sensor southSensor;
    }
}