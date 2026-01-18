package bearmaps;

import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.introcs.Stopwatch;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KDTreeTest {
    public static void main (String[] args) {

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            int randomX = StdRandom.uniform(-1000, 1000);
            int randomY = StdRandom.uniform(-1000, 1000);
            Point p = new Point(randomX, randomY);
            points.add(p);
        }

        KDTree kd = new KDTree(points);
        NaivePointSet ns = new NaivePointSet(points);
        KDTreeTest.timingNearestTest(kd, ns);
    }




    @Test
    public void randomizedNearestTest() {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            int randomX = StdRandom.uniform(-1000, 1000);
            int randomY = StdRandom.uniform(-1000, 1000);
            Point p = new Point(randomX, randomY);
            points.add(p);
        }

        KDTree kd = new KDTree(points);
        NaivePointSet ns = new NaivePointSet(points);
        for (int i = 0; i < 300; i++) {
            int randomX = StdRandom.uniform(-1000, 1000);
            int randomY = StdRandom.uniform(-1000, 1000);
            Point nsNearest = ns.nearest(randomX,randomY);
            Point kdNearest = kd.nearest(randomX,randomY);
            String error = "NaivePointSet found " + nsNearest.toString() + " to be the nearest point whereas KDTree found " + kdNearest.toString();
            assertEquals(error, nsNearest, kdNearest);

        }
    }


    public static void timingNearestTest(KDTree kd, NaivePointSet ns) {
        List<Integer> timingTest = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        for (int x = 1000; x <= 2000000 ;x += x) {

            Stopwatch sw = new Stopwatch();
            for (int y = x; y <= 2*x; y++) {
                int randomX = StdRandom.uniform(-1000, 1000);
                int randomY = StdRandom.uniform(-1000, 1000);
                kd.nearest(randomX,randomY);
            }

            double timeInSeconds = sw.elapsedTime();
            timingTest.add(x);
            times.add(timeInSeconds);
        }
        printTimingTable(timingTest,times,timingTest);
    }

    private static void printTimingTable(List<Integer> Ns, List<Double> times, List<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }
}
