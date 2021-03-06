package edu.columbia.cs6998.sdn.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuerySwitch2 {

	//Properties
	int dpid = 0;
	int MAX_LINKED_SWITCHES;
	String restApiPort;	
	controllerInfo controller = new controllerInfo();
	Graph localSwitchGraph = new Graph();
	
	//Constructor
	public QuerySwitch2(int maxSwitchNum, String apiPort) {
		restApiPort = apiPort;
		MAX_LINKED_SWITCHES = maxSwitchNum;
	}

	//Collect all switch ID by Rest API
	public void getSwitchID () throws IOException {
		String httpURL = "http://localhost:" + restApiPort + "/wm/core/controller/switches/json";
		URL myurlSwitch = new URL(httpURL);
		HttpURLConnection connection = (HttpURLConnection)myurlSwitch.openConnection();
		InputStream inputStream = connection.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
		String inputLine = null;    
		if ((inputLine = bufferedRead.readLine()) != null)
		{
			String arg[] = inputLine.split("dpid\":\"");      
			for (int i = 1; i < arg.length; i++) {
				localSwitchGraph.addNode(arg[i].substring(0, 23));
				controller.dpid.add(arg[i].substring(0, 23));   	
			}
		}
		bufferedRead.close();
	}

	//Collect all connection status by Rest API and BFS algorithm
	public void getSwitchLinkInfo() throws IOException{

		//Collect direct connection status by Rest API 
		String httpURL = "http://localhost:" + restApiPort + "/wm/topology/links/json";
		URL myurlSwitchLink = new URL(httpURL);
		HttpURLConnection connection = (HttpURLConnection)myurlSwitchLink.openConnection();
		InputStream inputStream = connection.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
		String inputLine = null;
		if ((inputLine = bufferedRead.readLine()) != null)
		{
			String arg[] = inputLine.split("src-switch\":\"");
			String port[] = inputLine.split("src-port\":");
			for (int i = 1; i < arg.length; i++) {
				String s[] = port[i].split(",");
				Map<String, Short> swMap = controller.linkBetweenSwitch.get((String) arg[i].subSequence(0, 23));
				if (swMap == null) {
					swMap = new HashMap<String, Short>();
					swMap.put((String) arg[i].subSequence(71, 94), Short.parseShort(s[0]));  
					controller.linkBetweenSwitch.put((String) arg[i].subSequence(0, 23), swMap);	  
				}  	
				else
					swMap.put((String) arg[i].subSequence(71, 94), Short.parseShort(s[0]));    
				localSwitchGraph.addEdge((String)arg[i].subSequence(0, 23), (String) arg[i].subSequence(71, 94), 	Short.parseShort(s[0]));
			}
		}
		System.out.println("Controller dpid:"+controller.dpid);

		//Implement BFS algorithm to get routing information
		localSwitchGraph.buildConnectInfo();
		bufferedRead.close();
	}

	//Collect all ports information for switches
	public void getSwitchPortNum() throws IOException, ParseException {
		String httpURL = "http://localhost:" + restApiPort + "/wm/core/controller/switches/json";
		URL myurlSwitch = new URL(httpURL);
		HttpURLConnection connection = (HttpURLConnection)myurlSwitch.openConnection();
		InputStream inputStream = connection.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
		String inputLine = null;    

		if ((inputLine = bufferedRead.readLine()) != null) {

          		//Extract switch information from Json
			JSONParser parser=new JSONParser();
			Object obj=parser.parse(inputLine);
			JSONArray array=(JSONArray)obj;
			JSONObject[] sw = new JSONObject[array.size()];
			for(int i = 0; i < array.size(); ++i){
				sw[i] = (JSONObject)array.get(i);
				String swid = (String) sw[i].get("dpid"); 
				//controller.dpid.add(swid);
				JSONArray port = (JSONArray)sw[i].get("ports");
				ArrayList<Short> list = new ArrayList<Short>();
				for(int j = 1; j < port.size(); ++j){
					JSONObject tmp = (JSONObject) port.get(j);
					Short portnum =Short.parseShort(tmp.get("portNumber").toString());
					list.add(portnum);
					controller.portOfSwitches.put(swid, list);
				}
			}
		}
		bufferedRead.close();
	}	

    	//check if controller contains specific switch id
	public boolean containsSwitchId(Long sourceMac) {
		for (String sid : controller.dpid) {
			if (SidToLong(sid) == sourceMac) return true;
		}
		return false;
	}

    	//change switch id of string type into long
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
  
   	//a mapping from char to long
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


