public class KDNode {
    public Sensor sensor;
    public int axis; // 0 = x-axis, 1 = y-axis
    public KDNode left;
    public KDNode right;

    public KDNode(Sensor sensor, int axis) {
        this.sensor = sensor;
        this.axis = axis;
    }
}