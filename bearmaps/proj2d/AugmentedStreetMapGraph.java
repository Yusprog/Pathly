package bearmaps.proj2d;

import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.TrieSet;
import bearmaps.proj2ab.WeirdPointSet;
import bearmaps.proj2c.WeirdSolver;
import bearmaps.proj2c.streetmap.StreetMapGraph;
import bearmaps.proj2c.streetmap.Node;
import org.eclipse.jetty.util.Trie;

import java.util.*;

public class AugmentedStreetMapGraph extends StreetMapGraph {
    private List<Node> nodes;
    private HashMap<Point, Node> pointNodeHashMap;
    private WeirdPointSet pointSet;
    private TrieSet trieSet;
    private HashMap<String, Node> stringToNodeHashMap;
    private HashMap<String, HashSet<String>> cleanNameToDirtyNames;
    private HashMap<String, Node> cleanNameToNode;
    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        nodes = this.getNodes();
        pointNodeHashMap = new HashMap<Point, Node>();
        List<Point> points = new ArrayList<Point> ();
        trieSet = new TrieSet();
        stringToNodeHashMap = new HashMap<String, Node>();
        cleanNameToDirtyNames = new HashMap<String, HashSet<String>>();
        cleanNameToNode = new HashMap<String, Node>();
        for (Node a : nodes) {
            if (!neighbors(a.id()).isEmpty()) {
                pointNodeHashMap.put(new Point(a.lon(), a.lat()), a);
                points.add(new Point(a.lon(), a.lat()));
            }
            if (a.name() != null) {
                stringToNodeHashMap.put(a.name(), a);
                if (cleanNameToDirtyNames.containsKey(cleanString(a.name()))) {
                    cleanNameToDirtyNames.get(cleanString(a.name())).add(a.name());
                    cleanNameToNode.put(cleanString(a.name()), a);
                } else {
                    HashSet<String> temp = new HashSet<String>();
                    cleanNameToDirtyNames.put(cleanString(a.name()), temp);
                    cleanNameToNode.put(cleanString(a.name()), a);
                    temp.add(a.name());
                }
                if (!trieSet.contains(cleanString(a.name()))) {
                    trieSet.add(cleanString(a.name()));
                }
            }
        }
        pointSet = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        return pointNodeHashMap.get(pointSet.nearest(lon, lat)).id();
    }

    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        String fixed = cleanString(prefix);
        Iterable<String> nameList = trieSet.keysWithPrefix(fixed);
        Iterator<String> nameIterator = nameList.iterator();
        List<String> results = new ArrayList<String> ();
        while (nameIterator.hasNext()) {
            for (String name : cleanNameToDirtyNames.get(nameIterator.next())) {
                results.add(name);
            }
        }
        return results;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<String> temp = getLocationsByPrefix(locationName);
        String clean = cleanString(locationName);
        List results = new LinkedList<Map<String, Object>>();
        for (String a : temp) {
            if (a.equals(clean)) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("lat", stringToNodeHashMap.get(a).lat());
                map.put("lon", stringToNodeHashMap.get(a).lon());
                map.put("name", stringToNodeHashMap.get(a).name());
                map.put("id", stringToNodeHashMap.get(a).id());
                results.add(map);
            }
        }
        return results;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
