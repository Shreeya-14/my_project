import java.util.*;

public class KDTree {
    private KDNode root;

    public KDTree(ArrayList<Sensor> sensors) {
        root = build(new ArrayList<Sensor>(sensors), 0);
    }

    private KDNode build(ArrayList<Sensor> points, final int depth) {
        if (points.isEmpty()) {
            return null;
        }

        final int axis = depth % 2;

        Collections.sort(points, new Comparator<Sensor>() {
            public int compare(Sensor a, Sensor b) {
                return axis == 0
                    ? Integer.compare(a.x, b.x)
                    : Integer.compare(a.y, b.y);
            }
        });

        int middle = points.size() / 2;
        KDNode node = new KDNode(points.get(middle), axis);

        node.left = build(
            new ArrayList<Sensor>(points.subList(0, middle)),
            depth + 1
        );

        node.right = build(
            new ArrayList<Sensor>(
                points.subList(middle + 1, points.size())
            ),
            depth + 1
        );

        return node;
    }

    public NearestResult findNearest(int x, int y) {
        NearestResult best = new NearestResult();
        search(root, x, y, best);
        return best;
    }

    private void search(KDNode node, int x, int y, NearestResult best) {
        if (node == null) {
            return;
        }

        best.nodesVisited++;

        double distance = Math.hypot(
            node.sensor.x - x,
            node.sensor.y - y
        );

        if (distance < best.distance) {
            best.distance = distance;
            best.sensor = node.sensor;
        }

        int targetValue = node.axis == 0 ? x : y;
        int splitValue = node.axis == 0
            ? node.sensor.x
            : node.sensor.y;

        KDNode nearBranch = targetValue < splitValue
            ? node.left
            : node.right;

        KDNode farBranch = targetValue < splitValue
            ? node.right
            : node.left;

        search(nearBranch, x, y, best);

        // KD-Tree pruning condition.
        if (Math.abs(targetValue - splitValue) < best.distance) {
            search(farBranch, x, y, best);
        }
    }

    public static class NearestResult {
        public Sensor sensor;
        public double distance = Double.MAX_VALUE;
        public int nodesVisited = 0;
    }
}