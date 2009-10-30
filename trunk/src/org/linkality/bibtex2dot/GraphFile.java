package org.linkality.bibtex2dot;
import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


/**
 * Every entry is a String[3]: start, end, weight
 * @author julian
 *
 */
public class GraphFile {
	public final static int UNDIRECTED = 0; 
	public final static int DIRECTED = 1; 
	private Hashtable<String, String> nodes = new Hashtable<String, String>();
	private Hashtable<String, String> weights = new Hashtable<String, String>();
	private Hashtable<String, String> colors = new Hashtable<String, String>();
	private int type = UNDIRECTED;
	
	public void addNode(String node1, String node2) {
		Random r = new Random();
		Color  c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		String key = node1+node2;
		String key2 = node2+node1;
		if (nodes.containsKey(key)) {
			String old_weight = (String) weights.get(key);
			double new_weight = (Double.parseDouble(old_weight)) + 0.1;
			weights.put(key, Double.toString(new_weight));
			colors.put(key, Integer.toHexString(c.getRGB()));
		} else if (nodes.containsKey(key2)) {
			String old_weight = (String) weights.get(key2);
			double new_weight = (Double.parseDouble(old_weight)) + 0.1;
			weights.put(key2, Double.toString(new_weight));
			colors.put(key2, Integer.toHexString(c.getRGB()));
		} else {
			double weight = 0.1;
			nodes.put(key, node1+","+node2);
			weights.put(key, Double.toString(weight));
			colors.put(key, Integer.toHexString(c.getRGB()));
		}
	}
	
	public String toString() {
		String result = "";
		String seperator;
		if (type==UNDIRECTED) {
			seperator = " -- ";
		} else {
			seperator = " -> ";
		}
		
		Set<String> keys = nodes.keySet();
		Iterator<String> keyIt = keys.iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			String node1 = nodes.get(key).split(",")[0];
			String node2 = nodes.get(key).split(",")[1];
			String weight = weights.get(key);
			String color = colors.get(key);
			result += node1 + seperator + node2 + "   [color=\"#"+color.substring(2)+"\" weight="+weight+"];\n";
		}
		return result;
	}
	
}
