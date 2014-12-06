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
	
	
	public void getSwitchPortNum() throws IOException {
		String httpURL = "http://localhost:" + restApiPort + "/wm/core/controller/switches/json";
	    URL myurlSwitch = new URL(httpURL);
	    HttpURLConnection connection = (HttpURLConnection)myurlSwitch.openConnection();
	    InputStream inputStream = connection.getInputStream();
	    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	    BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
	    String inputLine = null;    
	 
	    if ((inputLine = bufferedRead.readLine()) != null)
	    {
	    	//System.out.println(inputLine);
	    	
	      String arg[] = inputLine.split("inetAddress");	
	      System.out.println(arg.length + "!!!!!!!!");
	      //for (int i = 1; i < arg.length; i++) 
	      //	  System.out.println(arg[i] + "pppppppppppppppp");
	      
	      for (int i = 1; i < arg.length; i++) { 
	    	  String arg3[] = arg[i].split("dpid");
	    	  for(String id : arg3) {
	    		  System.out.println("There are strings dpids = " + id);
	    	  }
	    	  
	    	  ArrayList<Short> tmp = new ArrayList<Short>();
	    	  String arg2[] = arg[i].split("portNumber\":");
	    	  for (int j = 1; j < arg2.length; j++) {
	    	  	  String arg4[] = arg2[j].split(",\"config\"");
	    	  	  System.out.println(Integer.parseInt(arg4[0]) + "xxxxxxxxxxx");
	    	  	  if (Integer.parseInt(arg4[0]) < 65534) {
	    	  		  tmp.add(Short.parseShort(arg4[0]));
	    		  }
	    	  }
	    	  controller.portOfSwitches.put(arg3[1].substring(3, 26), tmp);
	    	  
	      }
	      
	    }
	    bufferedRead.close();
	}	

}
