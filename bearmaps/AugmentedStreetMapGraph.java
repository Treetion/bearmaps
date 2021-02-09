package bearmaps;


import bearmaps.utils.graph.streetmap.Node;
import bearmaps.utils.graph.streetmap.StreetMapGraph;
import bearmaps.utils.ps.Point;
import bearmaps.utils.ps.PointSet;
import bearmaps.utils.ps.WeirdPointSet;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {
    private HashMap<Point, Node> pnMap;
    private PointSet ps;
    private List<Node> nodes;
    private Trie dictionary;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        nodes = this.getNodes();
        pnMap = new HashMap<Point, Node>();
        dictionary = new Trie();
        List<Point> points = new ArrayList<Point>();
        for (Node temp : nodes) {
            if (neighbors(temp.id()).size() > 0) {
                pnMap.put(new Point(temp.lon(), temp.lat()), temp);
                points.add(new Point(temp.lon(), temp.lat()));
            }
            if (temp.name() != null && temp.name() != "") {
                dictionary.add(temp.name());
            }
        }
        ps = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point toFind = ps.nearest(lon, lat);
        return pnMap.get(toFind).id();
    }


    /**
     * For Project Part III (extra credit)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        if (prefix == null || prefix.length() <= 1) {
            return null;
        } else {
            String prefix2 = prefix.substring(0,1).toUpperCase() + prefix.substring(1);
            return dictionary.keysWithPrefix(prefix2);
        }
    }

    /**
     * For Project Part III (extra credit)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
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
        if (dictionary.contains(locationName)){
            List<Map<String, Object>> toReutrn = new LinkedList<Map<String, Object>>();
            for (Node temp : nodes){
                if (temp.name().equals(locationName)){
                    HashMap<String,Object> toInsert = new HashMap<String,Object>();
                    toInsert.put("lat",temp.lat());
                    toInsert.put("lon",temp.lon());
                    toInsert.put("name",temp.name());
                    toInsert.put("id",temp.id());
                    toReutrn.add(toInsert);
                }
            }
            return toReutrn;
        }else{
            return null;
        }
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }


    private class Trie {
        TrieNode root;

        public Trie() {
            root = new TrieNode();
        }

        public void clear() {
            root = new TrieNode();
        }

        public boolean contains(String key) {
            if (key == null || key.equals("")) {
                return false;
            } else {
                return root.contains(key);
            }
        }

        public void add(String key) {
            if (key != null && !key.equals("")) {
                root.add(key);
            }
        }

        public List<String> keysWithPrefix(String prefix) {
            if (prefix == null || prefix.equals("")) {
                return null;
            } else {
                if (longestPrefixOf(prefix).equals(prefix)) {
                    ArrayList<String> rtn = new ArrayList<String>();
                    String temp = prefix;
                    TrieNode pointer = root;
                    while (temp.length() > 0) {
                        pointer = pointer.children.get(String.valueOf(temp.charAt(0)));
                        temp = temp.substring(1);
                    }
                    pointer.prefixListHelper(prefix, rtn);
                    return rtn;
                } else {
                    return null;
                }
            }
        }

        public String longestPrefixOf(String key) {
            if (key == null || key.equals("")) {
                return "";
            } else {
                return root.longestPrefixOf(key);
            }
        }

        private class TrieNode {
            public HashMap<String, TrieNode> children;
            boolean flag;

            public TrieNode() {
                children = new HashMap<String, TrieNode>();
                flag = false;
            }

            public void add(String temp) {
                String toInsert = String.valueOf(temp.charAt(0));
                if (temp.length() == 1) {
                    if (!children.containsKey(toInsert)) {
                        children.put(toInsert, new TrieNode());
                    }
                    children.get(toInsert).flag = true;
                } else {
                    if (!children.containsKey(toInsert)) {
                        children.put(toInsert, new TrieNode());
                    }
                    children.get(toInsert).add(temp.substring(1));
                }
            }

            public boolean contains(String key) {
                String toFind = String.valueOf(key.charAt(0));
                if (children.containsKey(toFind)) {
                    if (key.length() > 1) {
                        return children.get(toFind).contains(key.substring(1));
                    } else {
                        return children.get(toFind).flag;
                    }
                } else {
                    return false;
                }
            }

            public String longestPrefixOf(String key) {
                String toFind = String.valueOf(key.charAt(0));
                if (children.containsKey(toFind)) {
                    if (key.length() > 1) {
                        return toFind + children.get(toFind).longestPrefixOf(key.substring(1));
                    } else {
                        return toFind;
                    }
                }
                return "";
            }

            public void prefixListHelper(String word, ArrayList<String> list) {
                if (!children.isEmpty()) {
                    for (HashMap.Entry<String, TrieNode> entry : children.entrySet()) {
                        if (entry.getValue().flag) {
                            list.add(word + entry.getKey());
                        }
                        entry.getValue().prefixListHelper(word + entry.getKey(), list);
                    }
                }
            }
        }
    }

}
