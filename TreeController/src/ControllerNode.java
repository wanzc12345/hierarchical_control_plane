import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ControllerNode {
	public String name;
	public String parentAddress;
	public List<String> childrenAddresses;
	public List<GSwitch> gswitches;
	public HashMap<String, String> switchGswitchMap;
	public List<Host> hosts;
	public Graph topology;
	public int port;
	
	public String process(String command){
		String result = "";
		String[] tokens = command.split(" ");

		if(tokens[0].equals("add")){
			if(tokens[1].equals("gswitch")){
				String[] ports = tokens[2].split(";");
				String[] switchIds = tokens[3].split(";");
				GSwitch gswitch = new GSwitch("gs"+String.valueOf(gswitches.size()+1), ports.length, ports, switchIds);
				for(int i=0;i<switchIds.length;i++){
					switchGswitchMap.put(switchIds[i], gswitch.name);
				}
				gswitches.add(gswitch);
				topology.addNode(gswitch.name);
				result = gswitch.name;
			}else{
				result = "Wrong command! Try help";
			}
		}else if(tokens[0].equals("remove")){
			if(tokens[1].equals("gswitch")){
				for(int i=0;i<gswitches.size();i++){
					if(gswitches.get(i).name.equals(tokens[2])){
						gswitches.remove(i);
					}
				}
				topology.deleteNode(tokens[2]);
				result = "Ok";
			}else if(tokens[1].equals("host")){
				String hostname = "";
				for(int i=0;i<hosts.size();i++){
					if(hosts.get(i).mac==tokens[2]){
						hosts.remove(i);
						hostname = hosts.get(i).name;
					}
				}
				topology.deleteNode(hostname);
				result = "Ok";
			}else{
				result = "Wrong command! Try help";
			}
		}else if(tokens[0].equals("packetin")){
			String gswitchName = tokens[1], inPort = tokens[2], srcMac = tokens[3], srcIp = tokens[4];
			GSwitch gSwitch = null;
			for(int i=0;i<gswitches.size();i++){
				if(gswitches.get(i).name.equals(gswitchName)){
					gSwitch = gswitches.get(i);
					break;
				}
			}
			if(!switchGswitchMap.containsKey(srcMac)){
				Host newHost = new Host("h"+String.valueOf(hosts.size()+1), srcMac, srcIp);
				hosts.add(newHost);
				topology.addNode(newHost.name);
				topology.addEdge(gswitchName, newHost.name, Short.parseShort(inPort));
				gSwitch.addLink(Short.parseShort(inPort), newHost.name);
				result = "host";
			}else{
				gSwitch.addLink(Short.parseShort(inPort), switchGswitchMap.get(srcMac));
				topology.addEdge(gSwitch.name, switchGswitchMap.get(srcMac), Short.parseShort(inPort));
				result = "switch";
			}
		}else if(tokens[0].equals("getvport")){
			String hostname = "";
			if(tokens[2].equals("ip")){
				for(int i=0;i<hosts.size();i++){
					if(hosts.get(i).ip.equals(tokens[3])){
						hostname = hosts.get(i).name;
						break;
					}
				}
				int i = 0;
				for(;i<gswitches.size();i++){
					if(gswitches.get(i).name.equals(tokens[1])){
						result = String.valueOf(topology.getNextHopPort(tokens[1], hostname));
						if(result.equals("-1"))
							result = "flood";
						break;
					}
				}
				if(i==gswitches.size())
					result = "flood";
			}else{
				result = "Wrong command! Try help";
			}
		}else if(tokens[0].equals("dump")){
			result = topology.dump();
		}else if(tokens[0].equals("drawtopology")){
			topology.drawGraph();
			result = "Ok";
		}else{
			result = "Wrong command! Try help";
		}
		return result;
	}
	
	public class ChildThread implements Runnable{
		private Socket clientSocket;
		
		public ChildThread(Socket clientSoc){
			clientSocket = clientSoc;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String line = "";
				while((line = br.readLine())!=null){
					System.out.println(line);
					if(line.equals("shutdown"))
						System.exit(0);
					pw.println(process(line));
					pw.flush();
				}
				pw.close();
				br.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public ControllerNode() throws IOException{
		parentAddress = "";
		childrenAddresses = new ArrayList<String>();
		gswitches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		switchGswitchMap = new HashMap<String, String>();
		topology = new Graph();
		port = 12091;
	}
	
	public ControllerNode(String configfilename) throws IOException{
		parentAddress = "";
		childrenAddresses = new ArrayList<String>();
		gswitches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		topology = new Graph();
		switchGswitchMap = new HashMap<String, String>();
		port = 12091;
		parseConfigFile(configfilename);
	}
	
	private void parseConfigFile(String filename){
		
	}
	
	void run() throws IOException{
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket;
		while((clientSocket=serverSocket.accept())!=null){
			System.out.println("new local controller connected.");
			ChildThread p = new ChildThread(clientSocket);
			new Thread(p).start();
		}
		serverSocket.close();
	}
}
