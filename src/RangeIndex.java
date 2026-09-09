import java.util.*;

public class RangeIndex {

    private RangeNode root;

    /*
     * A primary X-tree node.
     * Its secondaryCatalog is sorted by Y-coordinate.
     */
    private static class RangeNode {
        Sensor pivot;
        int minX;
        int maxX;

        RangeNode left;
        RangeNode right;

        ArrayList<Sensor> secondaryCatalog;

        RangeNode(Sensor pivot) {
            this.pivot = pivot;
            this.minX = pivot.x;
            this.maxX = pivot.x;
            this.secondaryCatalog = new ArrayList<Sensor>();
        }
    }

    public RangeIndex(ArrayList<Sensor> sensors) {
        ArrayList<Sensor> sortedSensors =
            new ArrayList<Sensor>(sensors);

        Collections.sort(sortedSensors, new Comparator<Sensor>() {
            public int compare(Sensor a, Sensor b) {
                return Integer.compare(a.x, b.x);
            }
        });

        root = buildRangeTree(sortedSensors);
    }

    /*
     * Builds a balanced primary tree using X coordinates.
     * Every node additionally receives a Y-sorted catalog.
     */
    private RangeNode buildRangeTree(ArrayList<Sensor> points) {
        if (points.isEmpty()) {
            return null;
        }

        int middle = points.size() / 2;
        RangeNode node = new RangeNode(points.get(middle));

        ArrayList<Sensor> leftPoints =
            new ArrayList<Sensor>(points.subList(0, middle));

        ArrayList<Sensor> rightPoints =
            new ArrayList<Sensor>(
                points.subList(middle + 1, points.size())
            );

        node.left = buildRangeTree(leftPoints);
        node.right = buildRangeTree(rightPoints);

        // Secondary Y catalog for this X-subtree.
        node.secondaryCatalog = new ArrayList<Sensor>(points);

        Collections.sort(node.secondaryCatalog,
            new Comparator<Sensor>() {
                public int compare(Sensor a, Sensor b) {
                    return Integer.compare(a.y, b.y);
                }
            }
        );

        if (node.left != null) {
            node.minX = Math.min(node.minX, node.left.minX);
            node.maxX = Math.max(node.maxX, node.left.maxX);
        }

        if (node.right != null) {
            node.minX = Math.min(node.minX, node.right.minX);
            node.maxX = Math.max(node.maxX, node.right.maxX);
        }

        return node;
    }

    /*
     * Orthogonal rectangle query:
     * xmin <= x <= xmax and ymin <= y <= ymax
     */
    public RangeResult search(int xmin, int xmax, int ymin, int ymax) {
        RangeResult result = new RangeResult();

        rangeQuery(root, xmin, xmax, ymin, ymax, result);

        return result;
    }

    private void rangeQuery(
        RangeNode node,
        int xmin,
        int xmax,
        int ymin,
        int ymax,
        RangeResult result
    ) {
        if (node == null) {
            return;
        }

        // This whole subtree is outside the X interval.
        if (node.maxX < xmin || node.minX > xmax) {
            return;
        }

        /*
         * This complete X-subtree lies inside the requested X-range.
         * Use its secondary Y catalog directly.
         */
        if (node.minX >= xmin && node.maxX <= xmax) {
            result.catalogsVisited++;
            searchSecondaryCatalog(
                node.secondaryCatalog,
                ymin,
                ymax,
                result.sensors
            );
            return;
        }

        /*
         * Partial overlap:
         * examine the pivot and recursively continue to children.
         */
        if (node.pivot.x >= xmin &&
            node.pivot.x <= xmax &&
            node.pivot.y >= ymin &&
            node.pivot.y <= ymax) {

            result.sensors.add(node.pivot);
        }

        rangeQuery(node.left, xmin, xmax, ymin, ymax, result);
        rangeQuery(node.right, xmin, xmax, ymin, ymax, result);
    }

    /*
     * Binary-search the secondary Y catalog.
     */
    private void searchSecondaryCatalog(
        ArrayList<Sensor> catalog,
        int ymin,
        int ymax,
        ArrayList<Sensor> output
    ) {
        int start = lowerBoundY(catalog, ymin);

        for (int i = start; i < catalog.size(); i++) {
            Sensor sensor = catalog.get(i);

            if (sensor.y > ymax) {
                break;
            }

            output.add(sensor);
        }
    }

    private int lowerBoundY(ArrayList<Sensor> catalog, int targetY) {
        int low = 0;
        int high = catalog.size();

        while (low < high) {
            int middle = (low + high) / 2;

            if (catalog.get(middle).y < targetY) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }

        return low;
    }

    public static class RangeResult {
        public ArrayList<Sensor> sensors;
        public int candidatesChecked;
        public int catalogsVisited;

        public RangeResult() {
            sensors = new ArrayList<Sensor>();
            candidatesChecked = 0;
            catalogsVisited = 0;
        }
    }
}