package bearmaps;
import java.util.List;

public class NaivePointSet implements PointSet {
    private List<Point> points;

    public NaivePointSet(List<Point> points) {
        this.points = points;
    }

    @Override
    public Point nearest(double x, double y) {
        Point goal = new Point(x,y);
        double minDist = 99999999;
        Point closest = new Point(0,0);
        for (Point actual: this.points) {
            double act_dist = Point.distance(actual, goal);
            if (act_dist < minDist) {
                minDist = act_dist;
                closest = actual;
            }
        }
        return closest;
    }

}
