package earlywarn.definiciones;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class OutputMap {

    public Map<String, Double> out;

    public OutputMap(TreeMap<String, Double> map) {
        out = new HashMap<String, Double>(map);
    }
}