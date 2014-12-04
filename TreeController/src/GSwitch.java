import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class GSwitch {
	String name;
	public int portCount;
	public HashMap<Integer, String> portHostMap;
	public HashMap<String, ArrayList<Integer>> hostPortsMap;
	public HashSet<String> switchIdSet;
	
	public GSwitch(String n, int c, String[] ports, String[] switchIds){
		name = n;
		portCount = c;
		portHostMap = new HashMap<Integer, String>();
		hostPortsMap = new HashMap<String, ArrayList<Integer>>();
		switchIdSet = new HashSet<String>();
		for(String sid : switchIds)
			switchIdSet.add(sid);
	}
	
	public boolean addLink(int port, String name){
		return true;
	}
	
	public boolean deleteLink(int port){
		return true;
	}
	
	public boolean isConnectedTo(String name){
		return hostPortsMap.containsKey(name);
	}
	
	public String whatConnectedTo(int port){
		return portHostMap.get(port);
	}
	
	public ArrayList<Integer> getPortsConnectedTo(String name){
		return hostPortsMap.get(name);
	}
}
