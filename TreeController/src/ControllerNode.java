import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	public HashMap<Long, String> switchGswitchMap;
	public List<Host> hosts;
	public Graph topology;
	public int port;
	public boolean log;
	public String logfilename;
	
	public String process(String command){
		String result = "";
		String[] tokens = command.split(" ");
		
		if(log){
			if(tokens[0].equals("add")||tokens[0].equals("remove")||tokens[0].equals("packetin")){
				try {
					PrintWriter pw = new PrintWriter(new FileWriter("tree.log"));
					pw.println(command);
					pw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if(tokens[0].equals("add")){
			if(tokens[1].equals("gswitch")){
				String[] ports = tokens[2].split(";");
				String[] switchIds = tokens[3].split(";");
				GSwitch gswitch = new GSwitch("gs"+String.valueOf(gswitches.size()+1), ports.length, ports, switchIds);
				for(int i=0;i<switchIds.length;i++){
					switchGswitchMap.put(SidToLong(switchIds[i]), gswitch.name);
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
					if(hosts.get(i).ip==tokens[2]){
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
			String gswitchName = tokens[1], inPort = tokens[2], srcMacString = tokens[3], srcIp = tokens[4];
			long srcMac = Long.parseLong(srcMacString);
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
					//System.out.print("hostip:"+hosts.get(i).ip);
					if(hosts.get(i).ip.equals(tokens[3])){
						hostname=hosts.get(i).name;
						break;
					}
				}
				int i = 0;
				for(;i<gswitches.size();i++){
					if(gswitches.get(i).name.equals(tokens[1])){
						result = String.valueOf(topology.getNextHopPortForNonLocal(tokens[1], hostname));
						//System.out.println("getnexthopbetween:"+tokens[1]+" and "+hostname+" is "+result);
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
		}else if(tokens[0].equals("backup")){
			
		}else if(tokens[0].equals("restore")){
			try {
				BufferedReader br = new BufferedReader(new FileReader("tree.log"));
				String line = "";
				while((line=br.readLine())!=null){
					process(line);
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			result = "Wrong command! Try help";
		}
		System.out.println(result);
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
		switchGswitchMap = new HashMap<Long, String>();
		topology = new Graph();
		port = 12091;
		log = false;
		logfilename = "tree.log";
		
		File file = new File(logfilename);
		if(file.exists()&&!file.isDirectory()){
			file.delete();
		}
	}
	
	public ControllerNode(String configfilename) throws IOException{
		parentAddress = "";
		childrenAddresses = new ArrayList<String>();
		gswitches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		topology = new Graph();
		switchGswitchMap = new HashMap<Long, String>();
		port = 12091;
		log = false;
		logfilename = "";
		
		parseConfigFile(configfilename);
		
		File file = new File(logfilename);
		if(file.exists()&&!file.isDirectory()){
			file.delete();
		}
	}
	
	private void parseConfigFile(String filename){
		try {
			BufferedReader br = new BufferedReader(new FileReader("config.txt"));
			String line = "";
			while((line=br.readLine())!=null){
				String[] tokens = line.split("=");
				if(tokens[0].equals("parent")){
					parentAddress = tokens[1];
				}else if(tokens[0].equals("port")){
					port = Integer.parseInt(tokens[1]);
				}else if(tokens[0].equals("log")){
					log = Boolean.getBoolean(tokens[1]);
				}else if(tokens[0].equals("logfilename")){
					logfilename = tokens[1];
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
