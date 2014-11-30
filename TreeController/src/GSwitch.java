import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GSwitch {
	String name;
	public int portCount;
	public HashMap<Integer, String> portHostMap;
	public HashMap<String, ArrayList<Integer>> hostPortsMap;
	
	public GSwitch(String n, int c, String[] ports, String[] hosts){
		name = n;
		portCount = c;
		portHostMap = new HashMap<Integer, String>();
		hostPortsMap = new HashMap<String, ArrayList<Integer>>();
		for(int i=0;i<c;i++){
			portHostMap.put(Integer.parseInt(ports[i]), hosts[i]);
			if(!hostPortsMap.containsKey(hosts[i])){
				hostPortsMap.get(hosts[i]).add(Integer.parseInt(ports[i]));
			}else{
				hostPortsMap.put(hosts[i], new ArrayList<Integer>());
			}
		}
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
