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




/**
 * @author ubuntu
 *
 */
public class QuerySwitch2 {

	int dpid = 0;
	String restApiPort;
	int MAX_LINKED_SWITCHES;
    controllerInfo controller = new controllerInfo();
	Graph localSwitchGraph = new Graph();

    public QuerySwitch2(int maxSwitchNum, String apiPort) {
        restApiPort = apiPort;
        MAX_LINKED_SWITCHES = maxSwitchNum;
    }

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
	
	//get all link information of switches
	public void getSwitchLinkInfo() throws IOException{
		
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
	    	  localSwitchGraph.addEdge((String)arg[i].subSequence(0, 23), (String) arg[i].subSequence(71, 94), Short.parseShort(s[0]));
	      }
	    }
	   localSwitchGraph.buildConnectInfo(controller.dpid);
	     
	    bufferedRead.close();
	}
	
	
	public void getSwitchPortNum() throws IOException, ParseException {
		String httpURL = "http://localhost:" + restApiPort + "/wm/core/controller/switches/json";
	    URL myurlSwitch = new URL(httpURL);
	    HttpURLConnection connection = (HttpURLConnection)myurlSwitch.openConnection();
	    InputStream inputStream = connection.getInputStream();
	    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	    BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
	    String inputLine = null;    
	 
	    if ((inputLine = bufferedRead.readLine()) != null) {
			System.out.println(inputLine);
			
			  JSONParser parser=new JSONParser();
	          controllerInfo coninfo = new controllerInfo();
			  Object obj=parser.parse(inputLine);
			  JSONArray array=(JSONArray)obj;
			  JSONObject[] sw = new JSONObject[array.size()];
			  for(int i = 0; i < array.size(); ++i){
				  sw[i] = (JSONObject)array.get(i);
				  String swid = (String) sw[i].get("dpid");
				  coninfo.dpid.add(swid);
				  JSONArray port = (JSONArray)sw[i].get("ports");
				  ArrayList<Short> list = new ArrayList<Short>();
				  for(int j = 1; j < port.size(); ++j){
					  JSONObject tmp = (JSONObject) port.get(j);
					  Short portnum =Short.parseShort(tmp.get("portNumber").toString());
					  list.add(portnum);
					  coninfo.portOfSwitches.put(swid, list);
				  }
			  }
		}
	    bufferedRead.close();
	}	

}
