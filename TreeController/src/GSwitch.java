import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class GSwitch {
	String name;
	public int portCount;
	public HashMap<Integer, String> portHostMap;
	public HashMap<String, ArrayList<Integer>> hostPortsMap;
	public HashSet<Long> switchIdSet;
	
	public GSwitch(String n, int c, String[] ports, String[] switchIds){
		name = n;
		portCount = c;
		portHostMap = new HashMap<Integer, String>();
		hostPortsMap = new HashMap<String, ArrayList<Integer>>();
		switchIdSet = new HashSet<Long>();
		for(String sid : switchIds)
			switchIdSet.add(SidToLong(sid));
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
	
	public long SidToLong(String sid) {
		if (sid.length() != 23) return -1;
		int sec = 0;
		int index = 0;
		long rst = 0;
		
		while(sec < 8) {
			int pos = 3 * sec + index;
			
			rst = rst * 16 + charToLong(sid.charAt(pos));
			if (index == 0) index++;
			else if (index == 1) {
				index = 0;
				sec++;
			}
			
		}
		
		return rst;
	}
	
	public long charToLong(char c) {
		long rst = 10;
		switch(c) {
			case '0' : rst = 0; break;
			case '1' : rst = 1; break;
			case '2' : rst = 2; break;
			case '3' : rst = 3; break;
			case '4' : rst = 4; break;
			case '5' : rst = 5; break;
			case '6' : rst = 6; break;
			case '7' : rst = 7; break;
			case '8' : rst = 8; break;
			case '9' : rst = 9; break;
			case 'a' : rst = 10; break;
			case 'b' : rst = 11; break;
			case 'c' : rst = 12; break;
			case 'd' : rst = 13; break;
			case 'e' : rst = 14; break;
			case 'f' : rst = 15; break;
			
		}
		return rst;
	}
}
