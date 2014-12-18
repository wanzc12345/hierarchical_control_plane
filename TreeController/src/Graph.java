import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Graph {
	
	//properties of graph
	public int V;
	public int E;
	public HashMap<String, Map<String, Short>> adjMap; //e.g "s1"->["1:s2","2:h1"]
	public HashMap<String, Map<String, Short>> connectMap; 

	//initialize
	public Graph(){
		V = 0;
		E = 0;
		adjMap = new HashMap<String, Map<String, Short>>();
		connectMap = new HashMap<String, Map<String, Short>>();
	}

	//add new node to graph
	public boolean addNode(String nodeName){
		if (!adjMap.containsKey(nodeName)) {
			Map<String, Short> tmp = new HashMap<String, Short>();
			adjMap.put(nodeName, tmp);
			V++;
		}
		return true;
	}

	//delete exist node in graph
	public boolean deleteNode(String nodeName){
		if (adjMap.containsKey(nodeName)) {
			adjMap.remove(nodeName);
			V--;
		}
		return true;
	}

	//return all neighbour nodes of a node
	public Map<String, Short> getAdjacentNodes(String nodeName){
		return adjMap.get(nodeName);
	}

	//add new edge in graph
	public boolean addEdge(String src, String des, short srcPort){
		if (adjMap.containsKey(src) && adjMap.containsKey(des)) {
			Map<String, Short> tmp = adjMap.get(src);
			tmp.put(des, srcPort);
			E++;
		}
		return true;
	}

	//delete existed edge in graph
	public boolean deleteEdge(String src, String des){
		if (adjMap.containsKey(src) && adjMap.containsKey(des)) {
			Map<String, Short> tmp = adjMap.get(src);
			if (tmp.containsKey(des)) {
				tmp.remove(des);
			}
			E--;
		}
		return true;
	}

	//check if two node are connected directly
	public short isConnectedinDirect(String src, String des){
		return adjMap.get(src).get(des);
	}
	
	//Collect and calculate connection status information for local controller
	public boolean buildConnectInfo() {
		for (String sw : adjMap.keySet()) {
			ArrayList<String> visit = new ArrayList<String>();
			ArrayList<String> neibourList = new ArrayList<String>();
			Map<String, Short> connect = new HashMap<String, Short>();
			visit.add(sw);
			Map<String, Short> map = adjMap.get(sw);
			for (String neibour : map.keySet()) {
				if (!visit.contains(neibour)) {
					neibourList.add(neibour);
					visit.add(neibour);			
					connect.put(neibour, map.get(neibour));
				}
			}
			for (String switches : neibourList) {
				if (!sw.equals(switches))
				BFS(switches, visit, connect.get(switches), connect); 
			}
			connectMap.put(sw, connect);		
		}
		return true;
	}

	//implement BFS algorithm to calculate connections of all nodes 
	public void BFS(String sw, ArrayList<String> visit, short port, Map<String, Short> connect) {
			Map<String, Short> tmp = adjMap.get(sw);
			for (String s : tmp.keySet()) {
				if (!visit.contains(s)) {
					visit.add(s);
					connect.put(s, port);
					//System.out.println("put " + s + " on " + port);
					BFS(s, visit, port, connect);		
				}
			}
	}	
	
	/**consult port numbers to controller for a packet with source and destination address in local area. 
	 * for local controller
	 * if controller has those information, it return the port number
	 * else, it will return -1
	 */
	public short getNextHopPort(String src, String des) {

		Map<String, Short> conn = connectMap.get(src);
		if (conn != null) {
			if (conn.containsKey(des)) return conn.get(des); 
		}

		return -1;
	}

	/**consult port numbers to controller for a packet with source and destination address in global area.
	 * for parents controller 
	 * if controller has those information, it return the port number
	 * else, it will return -1
	 */
	public short getNextHopPortForNonLocal(String src, String des){
		
		buildConnectInfo();
		Map<String, Short> conn = connectMap.get(src);
		if (conn != null) {
			if (conn.containsKey(des)) return conn.get(des); 
		}

		return -1;
		
	}

	//dump topology 
	public String dump() {
		String rst = "$";
		for (String ssw : adjMap.keySet()) {
			for (String dsw : adjMap.get(ssw).keySet()) {
				rst = rst + ssw + "->" + dsw + " ";
			}
		}
		return rst;
	}
      
	//show out the topology for hierarchical control system 
	public boolean drawGraph(){

			  try {
				  String head = "<script src=\"srcjs/sigma.core.js\"></script>" + 
				          "<script src=\"srcjs/conrad.js\"></script>" + 
				          "<script src=\"srcjs/utils/sigma.utils.js\"></script>" + 
				          "<script src=\"srcjs/utils/sigma.polyfills.js\"></script>" + 
				          "<script src=\"srcjs/sigma.settings.js\"></script>" + 
				          "<script src=\"srcjs/classes/sigma.classes.dispatcher.js\"></script>" + 
				          "<script src=\"srcjs/classes/sigma.classes.configurable.js\"></script>" + 
				          "<script src=\"srcjs/classes/sigma.classes.graph.js\"></script>" + 
				          "<script src=\"srcjs/classes/sigma.classes.camera.js\"></script>" + 
				          "<script src=\"srcjs/classes/sigma.classes.quad.js\"></script>" + 
				          "<script src=\"srcjs/captors/sigma.captors.mouse.js\"></script>" + 
				          "<script src=\"srcjs/captors/sigma.captors.touch.js\"></script>" + 
				          "<script src=\"srcjs/renderers/sigma.renderers.canvas.js\"></script>" + 
				          "<script src=\"srcjs/renderers/sigma.renderers.webgl.js\"></script>" + 
				          "<script src=\"srcjs/renderers/sigma.renderers.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/webgl/sigma.webgl.nodes.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/webgl/sigma.webgl.nodes.fast.js\"></script>" + 
				          "<script src=\"srcjs/renderers/webgl/sigma.webgl.edges.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/webgl/sigma.webgl.edges.fast.js\"></script>" + 
				          "<script src=\"srcjs/renderers/webgl/sigma.webgl.edges.arrow.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.labels.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.hovers.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.nodes.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.edges.def.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.edges.curve.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.edges.arrow.js\"></script>" + 
				          "<script src=\"srcjs/renderers/canvas/sigma.canvas.edges.curvedArrow.js\"></script>" + 
				          "<script src=\"srcjs/middlewares/sigma.middlewares.rescale.js\"></script>" + 
				          "<script src=\"srcjs/middlewares/sigma.middlewares.copy.js\"></script>" + 
				          "<script src=\"srcjs/misc/sigma.misc.animation.js\"></script>" + 
				          "<script src=\"srcjs/misc/sigma.misc.bindEvents.js\"></script>" + 
				          "<script src=\"srcjs/misc/sigma.misc.drawHovers.js\"></script>" +
				          "<script src=\"srcjs/plugins/sigma.plugins.dragNodes/sigma.plugins.dragNodes.js\"></script>";
				  String head1 = "<div id=\"container\">" +
						  "<style>" +
						    "#graph-container {" +
						    "  top: 0;" +
						    "  bottom: 0;" +
						    "  left: 0;" +
						    "  right: 0;" +
						    "  position: absolute;" +
						    "}" +
						    "</style>" +
						  "<div id=\"graph-container\">" +
						  "</div>" +
						  "</div>" + 
						  "<script>";
				  String head2 = "var i,s,N = " + Integer.toString(V) + ",E = " + Integer.toString(E) + ",g = {nodes: [],edges: []};";
			  
			   String tmp1 = new String();
			   String tmp2 = new String();
			   int i = 0, j = 0;
			   for(String sw1 : adjMap.keySet()){
				   Map<String, Short> map = adjMap.get(sw1);
				   j++;
				   String ss = sw1.charAt(0) == 'g'? "0.3": "0.3";
				   String color = sw1.charAt(0) == 'g'? "F00": "2BF";
				   tmp1 += "g.nodes.push({id:" + "'n' + " + "'" + sw1 + "'" + ",label: '" + sw1 + "', x:Math.random(),y:Math.random(),size:" + ss + ",color:'#" + color + "'});";
				   for(String sw2 : map.keySet()){
					   i++;
					   tmp2 += "g.edges.push({id: 'e' + " + Integer.toString(i) +",source: " + "'n' + " + "'" + sw1 + "'" + ",target: " + "'n' + " + "'" + sw2 + "'" + ",size: 0.2,color: '#ccc'});";
				   }
			   }
		       String end = "sigma.renderers.def = sigma.renderers.canvas;" + "s = new sigma({graph: g,container: 'graph-container'});sigma.plugins.dragNodes(s, s.renderers[0]);</script>";
		       String res = head + head1 + head2 + tmp1 + tmp2 + end;
			   File file = new File("topo.html");

			   // if file doesnt exists, then create it
			   if (!file.exists()) {
			    file.createNewFile();
			   }
			   FileWriter fw = new FileWriter(file.getAbsoluteFile());
			   BufferedWriter bw = new BufferedWriter(fw);
			   bw.write(res);
			   bw.close();
			   return true;

			  } catch (IOException e) {
			   e.printStackTrace();
			  }
			return false;
		}
}



