/**
*    Copyright 2014, Columbia University.
*    Homework 1, COMS E6998-10 Fall 2014
*    Software Defined Networking
*    Originally created by Shangjin Zhang, Columbia University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

/**
 * Floodlight
 * A BSD licensed, Java based OpenFlow controller
 *
 * Floodlight is a Java based OpenFlow controller originally written by David Erickson at Stanford
 * University. It is available under the BSD license.
 *
 * For documentation, forums, issue tracking and more visit:
 *
 * http://www.openflowhub.org/display/Floodlight/Floodlight+Home
 **/

package edu.columbia.cs6998.sdn.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionDataLayerSource;
import org.openflow.util.HexString;
import org.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Switch 
    implements IFloodlightModule, IOFMessageListener {
    protected static Logger log = LoggerFactory.getLogger(Switch.class);
    
    // Module dependencies
    protected IFloodlightProviderService floodlightProvider;
    
/* CS6998: data structures for the learning switch feature
    // Stores the learned state for each switch
*/
    protected Map<IOFSwitch, Map<Long, Short>> macToSwitchPortMap;


/* CS6998: data structures for the firewall feature
    // Stores the MAC address of hosts to block: <Macaddr, blockedTime>
*/
    protected Map<Long, Long> blacklist;
    
    /**
     * project:
     *     the first short in Map<Short, Short> below is virtual port; Second short
     *	represents realport number
     *	this map is for getting real port numbers with virtual port
     *	numbers are known
     */
    Map<String, Map<Short, Short>> virtualPortToReal;

   
    /**
     *      the first short in Map<Short, Short> below is realport; second
     *	short is virtual port number. this map is for getting virtual
     *	port numbers with real port numbers
     *	for each controller
     * 
     */
    Map<String, Map<Short, Short>> realPortToVirtual;
    Map<Short, String> vportToRport;


    HashMap<Long, String> portToSwitchID;
    HashMap<String, Long> switchIDToPort;
    
    genPort nextport;

    // flow-mod - for use in the cookie
    public static final int SWITCH_APP_ID = 10;
    // LOOK! This should probably go in some class that encapsulates
    // the app cookie management
    public static final int APP_ID_BITS = 12;
    public static final int APP_ID_SHIFT = (64 - APP_ID_BITS);
    public static final long SWITCH_COOKIE = (long) (SWITCH_APP_ID & ((1 << APP_ID_BITS) - 1)) << APP_ID_SHIFT;
    
    // more flow-mod defaults 
    protected static final short IDLE_TIMEOUT_DEFAULT = 10;
    protected static final short HARD_TIMEOUT_DEFAULT = 0;
    protected static final short PRIORITY_DEFAULT = 100;
    
    // for managing our map sizes
    protected static final int MAX_MACS_PER_SWITCH  = 1000;    

    // maxinum allowed elephant flow number for one switch
    protected static final int MAX_ELEPHANT_FLOW_NUMBER = 1;

    // maximum allowed destination number for one host
    protected static final int MAX_DESTINATION_NUMBER = 3;

    // maxinum allowed transmission rate
    protected static final int ELEPHANT_FLOW_BAND_WIDTH = 500;

    // time duration the firewall will block each node for
    protected static final int FIREWALL_BLOCK_TIME_DUR = (10 * 1000);
    
    protected static boolean isFirstPacket = true;
    
    protected static final int PARENT_PORT= 12091;
    
    protected static String GSWITCH_ID;
    
    protected Map<Integer, String> hostIp;
    
    protected Map<String, ArrayList<Long>> switchPortList;
    
    protected static final String apiPort = "8080";
    
    protected List<Long> externalSwitchMac;
    
    protected List<Long> externalHostMac;
    
    protected static String cName = "Controller1";
    
    //add by Yuanhui
    //
    //controlller information

    //protected controllerInfo InfoTable;
    protected QuerySwitch2 thisTable;
    
    /**
     * @param floodlightProvider the floodlightProvider to set
     */
    public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
        this.floodlightProvider = floodlightProvider;
    }
    
    @Override
    public String getName() {
        return Switch.cName;
    }

    //project
    public void buildAgent(){
          controllerInfo sw = thisTable.controller;
          for(String sw1 : sw.dpid){
        	  System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
        	  System.out.println("The Switch id is " + sw1);
        	  System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooo");        	  
              List<Short> list = sw.portOfSwitches.get(sw1);
              System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
              System.out.println("There are " + list.size() + " ports in switch " + sw1);
              System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");             
              Map<String, Short> map = sw.linkBetweenSwitch.get(sw1);
        	  if(map != null) {
        		  for(Short val : map.values()) {
	        		  System.out.println("*******************************************************************");
	            	  System.out.println("The link between switches is " + val); 
	        		  System.out.println("*******************************************************************");
        		  }
        	  }
              for(Short p : list){
                  if(map != null && map.containsValue(p)) {
                	  continue;
                  }
                  
                  short tmp = getNextVirtualPort(p);
                  Map<Short, Short> map1;
                  Map<Short, Short> map2;
                  String s = new String();
                  if(realPortToVirtual.containsKey(sw1)){
                      map1 = realPortToVirtual.get(sw1);
                  }
                  else map1 = new HashMap<Short, Short>();
                  if(virtualPortToReal.containsKey(sw1)){
                      map2 = virtualPortToReal.get(sw1);
                  }
                  else map2 = new HashMap<Short, Short>();
                  
                  map1.put(p, tmp);
                  map2.put(tmp, p);
                  System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                  System.out.println("The port number " + p + " for switch " + sw1 + " has virtual port " + tmp + " created for it");
                  System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");                
                  s = sw1 + " " + Short.toString(p);
                  realPortToVirtual.put(sw1, map1);
                  virtualPortToReal.put(sw1, map2);
                  vportToRport.put(tmp, s);
              }
          }
    }

    public short getNextVirtualPort(Short portNum){
         nextport.portCollection[nextport.next++] = portNum;
         nextport.length++;
         return (short) (nextport.next - 1);
    }

//    public boolean removeVirtualPort(Short vport){
//         int num = int) vport;
//         if(num > nextport.length - 1) return false;
//         nextport.portCollection[num] = nextport.portCollection[nextport.length - 1];
         //update map! save this method for later
//         return true;
//    } 

    public short translate(IOFSwitch sw, OFPacketIn pi){
         short inport = pi.getInPort();
        // OFMatch match = new OFMatch();
        // match.loadFromPacket(pi.getPacketData(), pi.getInPort());
        // Long sourceMac = Ethernet.toLong(match.getDataLayerDestination());
         System.out.println("realtovirtual map:"+realPortToVirtual+" switchid:"+sw.getStringId());
         short vport = realPortToVirtual.get(sw.getStringId()).get(inport);
         return vport;
    }
    public short translate(IOFSwitch sw, short rport){
         return realPortToVirtual.get(sw.getStringId()).get(rport);
    }
    
    public String translateback(short vport){
         return vportToRport.get(vport);
    }

    /**
     * Adds a host to the MAC->SwitchPort mapping
     * @param sw The switch to add the mapping to
     * @param mac The MAC address of the host to add
     * @param portVal The switchport that the host is on
     */
/* CS6998: fill out the following ????s
 */
    protected void addToPortMap(IOFSwitch sw, long mac, short portVal) {
        Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
        
        if (swMap == null) {
            // May be accessed by REST API so we need to make it thread safe
            swMap = Collections.synchronizedMap(new LRULinkedHashMap<Long, Short>(MAX_MACS_PER_SWITCH));
            macToSwitchPortMap.put(sw, swMap);
        }
        swMap.put(mac, portVal);
    }

    
    /**
     * Removes a host from the MAC->SwitchPort mapping
     * @param sw The switch to remove the mapping from
     * @param mac The MAC address of the host to remove
     */
/* CS6998: fill out the following ????s
*/
   protected void removeFromPortMap(IOFSwitch sw, long mac) {
        Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
        if (swMap != null)
            swMap.remove(mac);
    }


    /**
     * Get the port that a MAC is associated with
     * @param sw The switch to get the mapping from
     * @param mac The MAC address to get
     * @return The port the host is on
     */
/* CS6998: fill out the following method
*/
   public Short getFromPortMap(IOFSwitch sw, long mac) {
        Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
        if (swMap != null)
            return swMap.get(mac);

        return null;
    }

    
    /**
     * Writes a OFFlowMod to a switch.
     * @param sw The switch tow rite the flowmod to.
     * @param command The FlowMod actions (add, delete, etc).
     * @param bufferId The buffer ID if the switch has buffered the packet.
     * @param match The OFMatch structure to write.
     * @param outPort The switch port to output it to.
     */
    private void writeFlowMod(IOFSwitch sw, short command, int bufferId,
            OFMatch match, short outPort, long switchId) {
        // from openflow 1.0 spec - need to set these on a struct ofp_flow_mod:
        // struct ofp_flow_mod {
        //    struct ofp_header header;
        //    struct ofp_match match; /* Fields to match */
        //    uint64_t cookie; /* Opaque controller-issued identifier. */
        //
        //    /* Flow actions. */
        //    uint16_t command; /* One of OFPFC_*. */
        //    uint16_t idle_timeout; /* Idle time before discarding (seconds). */
        //    uint16_t hard_timeout; /* Max time before discarding (seconds). */
        //    uint16_t priority; /* Priority level of flow entry. */
        //    uint32_t buffer_id; /* Buffered packet to apply to (or -1).
        //                           Not meaningful for OFPFC_DELETE*. */
        //    uint16_t out_port; /* For OFPFC_DELETE* commands, require
        //                          matching entries to include this as an
        //                          output port. A value of OFPP_NONE
        //                          indicates no restriction. */
        //    uint16_t flags; /* One of OFPFF_*. */
        //    struct ofp_action_header actions[0]; /* The action length is inferred
        //                                            from the length field in the
        //                                            header. */
        //    };
        OFFlowMod flowMod = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flowMod.setMatch(match);
        flowMod.setCookie(Switch.SWITCH_COOKIE);
        flowMod.setCommand(command);
        flowMod.setIdleTimeout(Switch.IDLE_TIMEOUT_DEFAULT);
        flowMod.setHardTimeout(Switch.HARD_TIMEOUT_DEFAULT);
        flowMod.setPriority(Switch.PRIORITY_DEFAULT);
        flowMod.setBufferId(bufferId);
        flowMod.setOutPort((command == OFFlowMod.OFPFC_DELETE) ? outPort : OFPort.OFPP_NONE.getValue());
        flowMod.setFlags((command == OFFlowMod.OFPFC_DELETE) ? 0 : (short) (1 << 0)); // OFPFF_SEND_FLOW_REM

        // set the ofp_action_header/out actions:
        // from the openflow 1.0 spec: need to set these on a struct ofp_action_output:
        // uint16_t type; /* OFPAT_OUTPUT. */
        // uint16_t len; /* Length is 8. */
        // uint16_t port; /* Output port. */
        // uint16_t max_len; /* Max length to send to controller. */
        // type/len are set because it is OFActionOutput,
        // and port, max_len are arguments to this constructor
        flowMod.setActions(Arrays.asList((OFAction) new OFActionOutput(outPort, (short) 0xffff), (OFAction) new OFActionDataLayerSource(Ethernet.toByteArray(switchId))));
        flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH + OFActionDataLayerSource.MINIMUM_LENGTH));

        if (log.isTraceEnabled()) {
            log.trace("{} {} flow mod {}", 
                      new Object[] { sw, (command == OFFlowMod.OFPFC_DELETE) ? "deleting" : "adding", flowMod });
        }

        // and write it out
        try {
            sw.write(flowMod, null);
        } catch (IOException e) {
            log.error("Failed to write {} to switch {}", new Object[] { flowMod, sw }, e);
        }
    }

    /**
     * Writes an OFPacketOut message to a switch.
     * @param sw The switch to write the PacketOut to.
     * @param packetInMessage The corresponding PacketIn.
     * @param egressPort The switchport to output the PacketOut.
     */
    private void writePacketOutForPacketIn(IOFSwitch sw, 
                                          OFPacketIn packetInMessage, 
                                          short egressPort) {

        // from openflow 1.0 spec - need to set these on a struct ofp_packet_out:
        // uint32_t buffer_id; /* ID assigned by datapath (-1 if none). */
        // uint16_t in_port; /* Packet's input port (OFPP_NONE if none). */
        // uint16_t actions_len; /* Size of action array in bytes. */
        // struct ofp_action_header actions[0]; /* Actions. */
        /* uint8_t data[0]; */ /* Packet data. The length is inferred
                                  from the length field in the header.
                                  (Only meaningful if buffer_id == -1.) */
        
        OFPacketOut packetOutMessage = (OFPacketOut) floodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
        short packetOutLength = (short) OFPacketOut.MINIMUM_LENGTH; // starting length

        // Set buffer_id, in_port, actions_len
        packetOutMessage.setBufferId(packetInMessage.getBufferId());
        packetOutMessage.setInPort(packetInMessage.getInPort());
        packetOutMessage.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
        packetOutLength += OFActionOutput.MINIMUM_LENGTH;
        
        // set actions
        List<OFAction> actions = new ArrayList<OFAction>(1);      
        actions.add(new OFActionOutput(egressPort, (short) 0));
        packetOutMessage.setActions(actions);

        // set data - only if buffer_id == -1
        if (packetInMessage.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            byte[] packetData = packetInMessage.getPacketData();
            packetOutMessage.setPacketData(packetData); 
            packetOutLength += (short) packetData.length;
        }
        
        // finally, set the total length
        packetOutMessage.setLength(packetOutLength);              
            
        // and write it out
        try {
            sw.write(packetOutMessage, null);
        } catch (IOException e) {
            log.error("Failed to write {} to switch {}: {}", new Object[] { packetOutMessage, sw, e });
        }
    }
    
    /**
     * Processes a OFPacketIn message. If the switch has learned the MAC to port mapping
     * for the pair it will write a FlowMod for. If the mapping has not been learned the 
     * we will flood the packet.
     * @param sw
     * @param pi
     * @param cntx
     * @return
     */
    
    private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {

    	// Added by Adeyemi
		List<Object> socketList = this.getSocketIO(null, this.PARENT_PORT);
		Socket socket = (Socket) socketList.get(0);
		BufferedReader in = (BufferedReader) socketList.get(1);
		PrintWriter out = (PrintWriter) socketList.get(2);
		
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());
        
        Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
        Long destMac = Ethernet.toLong(match.getDataLayerDestination());        
        int sourceIp = match.getNetworkSource();
        int destIp = match.getNetworkDestination();
        Long switchId = sw.getId();
        Short inputPort = match.getInputPort();
    	log.info("Packet received with the sourceMac { " + sourceMac + " }, and destMAc { " + destMac + " }, and sourceIp { " + sourceIp + " }, and destIp { " +destIp + " }");
        if(destIp == 0) {
			this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());
			log.info("INFO: Flow Flood sent to the switch for Mininet generated packet");       	
        	return Command.CONTINUE;
        }
        
        if(Switch.isFirstPacket) {
    		// get the virtual port for the packet and pass to the Parent Controller
    		createControlTable();
                buildAgent();
    		getSwitchePort(sw);
    		    		
    		List<String> switchIdList = this.thisTable.controller.dpid;
    		Set<Short> setOfAllVirtualPort = new HashSet<Short>();
    		StringBuffer Id = new StringBuffer();
    		for(int index=0; index < switchIdList.size(); ++index) {
    			String id = switchIdList.get(index);
    			if(this.virtualPortToReal.containsKey(id)) {
    				if(!this.virtualPortToReal.get(id).keySet().isEmpty()) {
    					setOfAllVirtualPort.addAll(this.virtualPortToReal.get(id).keySet()); 
    				}
    			}
    			Id.append(id);
    			if(index != switchIdList.size()-1) Id.append(";");
    		}

    		StringBuffer virtualPort = new StringBuffer();
    		Object[] virtualPorts =  setOfAllVirtualPort.toArray();
    		for(int index=0; index < virtualPorts.length; ++index) {
    			Short vport = (Short) virtualPorts[index];
    			virtualPort.append(vport);
    			if(index != virtualPorts.length-1) virtualPort.append(";");
    		}
    	
    		out.println("add gswitch " + virtualPort.toString() + " " + Id.toString());
    		System.out.println("Command add gswitch sent to the Parent by " + this.cName);
    		String response;
    		try {
				response = in.readLine();
				this.GSWITCH_ID = response;
				Switch.isFirstPacket = false;
				System.out.println("-------------------------------------------------------");			
				System.out.println("Gswitch Name received from the Parent is " + this.GSWITCH_ID);
				System.out.println("-------------------------------------------------------");
				System.out.println("-------------------------------------------------------");			
				System.out.println("Response received from the Parent wass " + response);
				System.out.println("-------------------------------------------------------");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Socket InputStream: There was a problem reading from the input stream");
				e.printStackTrace();
				Switch.isFirstPacket = true;
			}
    	}
        
    	/*
    	 * Get the device type from the parent
    	 * 
    	 */
        if(!(externalSwitchMac.contains(sourceMac) || this.hostIp.containsKey(sourceIp))) {
            Short virtualPort = this.translate(sw, pi);
    		out.println("packetin " + this.GSWITCH_ID + " " + virtualPort + " " + sourceMac + " " + sourceIp);
    		System.out.println("Command: packetin sent to the Parent for sourceIp " + sourceIp);
        	String device = null;
    		try {
    			device = in.readLine();
				if("Switch".equalsIgnoreCase(device)) externalSwitchMac.add(sourceMac);
				else if("Host".equalsIgnoreCase(device)) this.hostIp.put(sourceIp, sw.getStringId() + " " + inputPort.toString());
    		} catch (IOException e) {
    			System.out.println("Socket InputStream: There was a problem reading from the input stream");
    			e.printStackTrace();
    		}
    	}

        String[] argString = new String[2];
        String outputSwitchPort = this.hostIp.get(destIp);
        if (outputSwitchPort != null) {
        	argString = outputSwitchPort.split(" ");
        }
		
		System.out.println("hostIp:"+hostIp);
    	if(!this.hostIp.containsKey(destIp)) {
    		out.println("getvport " + this.GSWITCH_ID + " ip " + destIp);
    		System.out.println("Command: getvport sent to the Parent for destIp " + destIp);
    		String response;
    		try {
				response = in.readLine();
					if(response.equalsIgnoreCase("Flood")) {
						// flood throughout subnet
						this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());
						log.info("INFO: Flow Flood sent to the switch");

					} else if(response != null) {
						// forward along path as this is a host
						String switchIdPort = this.translateback(Short.parseShort(response));
						String[] argSwitchPort = switchIdPort.split(" ");
						String swId = argSwitchPort[0];
						if(swId.equals(sw.getStringId())) {
						    match.setWildcards(((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
						    		& ~OFMatch.OFPFW_DL_DST
						    		& ~OFMatch.OFPFW_NW_DST_MASK);
				            // CS6998: Fill out the following ????
						    System.out.println("set up match" + Short.parseShort(argSwitchPort[1]));
						    System.out.println("####################################################");
						    System.out.println("Context: {response != null} Writing " + ~OFMatch.OFPFW_DL_DST + " as the destination mac into flow table of switch " + sw.getStringId());
						    System.out.println("####################################################");
				            this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, pi.getBufferId(), match, Short.parseShort(argSwitchPort[1]), sw.getId());
						}
						else {
							Short outputPort = this.thisTable.localSwitchGraph.getNextHopPort(sw.getStringId(), swId);
							match.setWildcards(((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
						    		& ~OFMatch.OFPFW_DL_DST
						    		& ~OFMatch.OFPFW_NW_DST_MASK);
				            // CS6998: Fill out the following ????
							System.out.println("set up match ----- " + outputPort);
						    System.out.println("####################################################");
						    System.out.println("Context: {after response != null} Writing " + ~OFMatch.OFPFW_DL_DST + " as the destination mac into flow table of switch " + sw.getStringId());
						    System.out.println("####################################################");
				            this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, pi.getBufferId(), match, outputPort, sw.getId());
						}
						
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Socket InputStream: There was a problem reading from the input stream");
				e.printStackTrace();
			}
    	}
    	else if (Short.parseShort(argString[1]) == match.getInputPort()) {
    		System.out.println("switch"+sw.getStringId()+"outputPort is inputPort");
            log.trace("ignoring packet that arrived on same port as learned destination:"
                    + " switch {} dest MAC {} port {}",
                    new Object[]{ sw, HexString.toHexString(destMac), this.getFromPortMap(sw, destMac) });
        }
    	else {
    		short outputPort = Short.parseShort(argString[1]);
    		if(!sw.getStringId().equals(argString(0)))
    				outputPort = this.thisTable.localSwitchGraph.getNextHopPort(sw.getStringId(), argString[0]);
    		System.out.println("switch"+sw.getStringId()+":forward to port"+outputPort);
            match.setWildcards(((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
                    & ~OFMatch.OFPFW_DL_DST
                    & ~OFMatch.OFPFW_NW_DST_MASK);
            // CS6998: Fill out the following ????
		    System.out.println("####################################################");
		    System.out.println("Context: {Else, it is out map} Writing " + ~OFMatch.OFPFW_DL_DST + " as the destination mac into flow table of switch " + sw.getStringId());
		    System.out.println("####################################################");
            this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, pi.getBufferId(), match, outputPort, sw.getId());
    	}
    	
    	try {
			in.close();
	    	socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket");
			e.printStackTrace();
		}
    	out.close();


        return Command.CONTINUE;
    }

    /**
     * Processes a flow removed message. 
     * @param sw The switch that sent the flow removed message.
     * @param flowRemovedMessage The flow removed message.
     * @return Whether to continue processing this message or stop.
     */

    // IOFMessageListener
    
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch (msg.getType()) {
            case PACKET_IN:
                return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
            /*
            case FLOW_REMOVED:
                return this.processFlowRemovedMessage(sw, (OFFlowRemoved) msg);
            */
            case ERROR:
                log.info("received an error {} from switch {}", (OFError) msg, sw);
                return Command.CONTINUE;
            default:
                break;
        }
        log.error("received an unexpected message {} from switch {}", msg, sw);
        return Command.CONTINUE;
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    // IFloodlightModule
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m = 
                new HashMap<Class<? extends IFloodlightService>,
                    IFloodlightService>();
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider =
                context.getServiceImpl(IFloodlightProviderService.class);
/* CS6998: Initialize data structures
*/
       macToSwitchPortMap = 
                new ConcurrentHashMap<IOFSwitch, Map<Long, Short>>();
        nextport = new genPort();
        realPortToVirtual = new HashMap<String, Map<Short, Short>>();
        virtualPortToReal = new HashMap<String, Map<Short, Short>>();
        vportToRport = new HashMap<Short, String>();
        hostIp = new ConcurrentHashMap<Integer, String>();
        externalSwitchMac = new ArrayList<Long>();
    	switchPortList = new HashMap<String, ArrayList<Long>>();
        thisTable = new QuerySwitch2(MAX_MACS_PER_SWITCH, apiPort);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        //floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
    }
    public short getPort(Long mac){
        return 0;
    }
    
    /**
     * 
     * @param host: InetAddress of the Parent Controller, null if localhost
     * @param port: Port Parent Controller listens on
     * @return List of objects with the indexes corresponding to
     * 	0: Socket
     *  1: BufferedReader
     *  2: PrintWriter
     */
    public List<Object> getSocketIO(InetAddress host, int port) {
    	host = (host != null) ? host : Inet4Address.getLoopbackAddress();

        Socket commandSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
		try {
			commandSocket = new Socket(host, port);
			out =
			    new PrintWriter(commandSocket.getOutputStream(), true);
			in =
			    new BufferedReader(
			        new InputStreamReader(commandSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Couldn't get I/O for the connection to " +
            host);
			System.exit(1);
		}
		
		List<Object> socketDetails = new ArrayList<Object>();
		socketDetails.add(0, commandSocket);	
		socketDetails.add(1, in);
		socketDetails.add(2, out);
		return socketDetails;
    }
    
    /**
     * 
     * @param switchId: the Switch Id on the packetIn message (switch connected to the controller enroute to the host
     * @param hostIp The Ip address of the host, which is the source Ip of the packet In message
     */
    public void learnHostIp(String switchId, int Ip, short inputPort) {
    	
    	Short inputP = inputPort;
    	String tmpString = switchId + " " + inputP.toString();
    	if (!hostIp.containsKey(Ip)) {
    		hostIp.put(Ip, tmpString);
    	}
    	
    	
    }
    
	//modified by Yuanhui
    private void createControlTable() {
		try {
        	System.out.println("get switch 10D");
        	thisTable.getSwitchID();
       	 	System.out.println("info");
       	 	} catch(IOException e) {}
        	try {
        	System.out.println("get switch link info");
        	thisTable.getSwitchLinkInfo();
        	} catch(IOException e) {}
        	try {
        	System.out.println("getSwitchPortNum");
        	thisTable.getSwitchPortNum();
        	} catch(IOException e) {}
           //     buildAgent();
	}

    private void getSwitchePort(IOFSwitch sw) {
		for (String key : thisTable.controller.portOfSwitches.keySet()) {
			System.out.println(key);
	    		ArrayList<Short> tmp = thisTable.controller.portOfSwitches.get(key);
	    		ArrayList<Long> macSet = new ArrayList<Long>();
			for (short portNum : tmp) {
				System.out.println(portNum);
				System.out.printf("",sw.getPort(portNum).getHardwareAddress());
				macSet.add(Ethernet.toLong(sw.getPort(portNum).getHardwareAddress()));		
			}			
			switchPortList.put(key, macSet);
	        }

	}
    
    
}

class genPort{
    Short [] portCollection;
    int length;
    int next;
    public genPort(){
      length = 0;
      next = 0;
      portCollection = new Short[100];
    }
}
