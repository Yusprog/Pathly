package bearmaps;
import java.util.List;

public class KDTree implements PointSet {
    // Nested Node class
    private class Node implements Comparable<Node> {
        Point p;
        Node left;
        Node right;
        int depth;

        public Node (Point p, Node left, Node right) {
            this.p = p;
            this.left = left;
            this.right = right;
        }

        public int compareTo(Node other) {
            if (this.depth % 2 == 0) {
                return this.p.compareX(other.p);
            } else {
                 return this.p.compareY(other.p);
            }
        }

        public int compareTo(Point other) {
            if (this.depth % 2 == 0) {
                return this.p.compareX(other);
            } else {
                return this.p.compareY(other);
            }
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

    }

    private int size;
    private Node root;
    private int depth;

    public KDTree(List<Point> points) {
        // Create root node and set its depth to zero
        this.root = new Node(points.get(0), null, null);
        root.setDepth(0);

        // Add point nodes to the root
        this.depth = 0;
        for (int i = 1; i < points.size(); i++) {
            this.insert(points.get(i));
        }
    }

    private void insert(Point p) {
        // Create new node object with the new point
        Node newNode = new Node(p, null, null);
        traverse(root, newNode);

        // Update depth of current node then reset depth to zero
        newNode.setDepth(depth);
        depth = 0;
    }

    private Node traverse(Node curr, Node newNode) {
        // If the child is unoccupied, fill it
        if (curr == null) {
            return newNode;
        }

        // Compare and traverse left/right child depending on depth
        int cmp = curr.compareTo(newNode);
        if (cmp > 0) {
            depth += 1;
            curr.left = traverse(curr.left,newNode);
        } else if (cmp < 0) {
            depth += 1;
            curr.right = traverse(curr.right, newNode);
        } else {
            depth += 1;
            curr.right = traverse(curr.right, newNode);
        }

        return curr;
    }

    public Point nearest(double x, double y) {
        //Node goal = new Node(new Point(x,y), null, null);
        Point goal = new Point(x,y);
        Node found = nearest(root, goal, root);
        return found.p;
    }

    public Node nearest(Node n, Point goal, Node best) {
        if (n == null) {
            return best;
        }

        // Update best node
        if (Point.distance(n.p, goal) < Point.distance(best.p, goal)) {
            best = n;
        }

        Node goodSide, badSide;
        // Goal is smaller than current node
        if (n.compareTo(goal) > 0) {
            goodSide = n.left;
            badSide = n.right;
        } else {
            goodSide = n.right;
            badSide = n.left;
        }

        best = nearest(goodSide, goal, best);
        if (this.bestBadPoint(n, goal, best)) {
            best = nearest(badSide, goal, best);
        }
        return best;
    }

    // Returns the node in the bad side that could be potentially useful, if there aren't any, return null
    public boolean bestBadPoint(Node curr, Point goal, Node best) {
        double cmp;
        if (curr.depth % 2 == 0) {
            cmp = curr.p.compareBadSide(goal,0);
        } else {
            cmp = curr.p.compareBadSide(goal,1);
        }

        if (cmp < Point.distance(best.p, goal)) {
            return true;
        }
        return false;
    }
}
