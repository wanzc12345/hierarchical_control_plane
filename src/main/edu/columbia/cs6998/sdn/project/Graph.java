package edu.columbia.cs6998.sdn.hw1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Graph {
	public int V;
	public int E;
	public HashMap<String, Map<String, Short>> adjMap; //e.g "s1"->["1:s2","2:h1"]
	public HashMap<String, Map<String, Short>> connectMap; 

	public Graph(){
		V = 0;
		E = 0;
		adjMap = new HashMap<String, Map<String, Short>>();
		connectMap = new HashMap<String, Map<String, Short>>();
	}

	public boolean addNode(String nodeName){
		if (!adjMap.containsKey(nodeName)) {
			Map<String, Short> tmp = new HashMap<String, Short>();
			adjMap.put(nodeName, tmp);
			V++;
		}
		return true;
	}

	public boolean deleteNode(String nodeName){
		if (adjMap.containsKey(nodeName)) {
			adjMap.remove(nodeName);
			V--;
		}
		return true;
	}

	public Map<String, Short> getAdjacentNodes(String nodeName){
		return adjMap.get(nodeName);
	}

	public boolean addEdge(String src, String des, short srcPort){
		if (adjMap.containsKey(src) && adjMap.containsKey(des)) {
			Map<String, Short> tmp = adjMap.get(src);
			tmp.put(des, srcPort);
			E++;
		}

		return true;
	}

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

	public short isConnectedinDirect(String src, String des){
		return adjMap.get(src).get(des);
	}

	public boolean buildConnectInfo(ArrayList<String> dpid) {
		for (String sw : dpid) {
			ArrayList<String> visit = new ArrayList<String>();
			ArrayList<String> neibourList = new ArrayList<String>();
			Map<String, Short> connect = new HashMap<String, Short>();
			visit.add(sw);
			Map<String, Short> map = adjMap.get(sw);
			//System.out.println("for " + sw);
			for (String neibour : map.keySet()) {
				//System.out.println(neibour);
				if (!visit.contains(neibour)) {
					neibourList.add(neibour);
					visit.add(neibour);			
					connect.put(neibour, map.get(neibour));
					//System.out.println("put " + neibour + " on " + map.get(neibour));
				}
				//connectMap.put(sw, new )
			}
			for (String switches : neibourList) {
				if (!sw.equals(switches))
				BFS(switches, visit, connect.get(switches), connect); 
			}
			connectMap.put(sw, connect);		
		}
		
		for (String sw : connectMap.keySet()) {
			Map<String, Short> map = connectMap.get(sw);
			//System.out.println(sw);
			for (String sw2 : connectMap.keySet()) {
				System.out.println("connect to " + sw2 + " by port " + map.get(sw2));
			}
		}
		return true;
	}

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


	/*
	public String getNextHop(String src, String dest){
		return src;
	}
	 */
}



