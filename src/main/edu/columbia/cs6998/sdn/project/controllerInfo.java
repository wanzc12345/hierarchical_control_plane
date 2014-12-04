package edu.columbia.cs6998.sdn.project;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class controllerInfo {
	
	//record the dpid of switches
	public ArrayList<String> dpid;
	
	//record the connection information 
	public Map<String, Map<String, Short>> linkBetweenSwitch;
	
	//record the port information of each switch
	public Map<String, ArrayList<Short>> portOfSwitches;
	
	public controllerInfo() {
		dpid = new ArrayList<String>();
		linkBetweenSwitch = new HashMap<String, Map<String, Short>>();
		portOfSwitches = new LinkedHashMap<String, ArrayList<Short>>();
	}
	
}
