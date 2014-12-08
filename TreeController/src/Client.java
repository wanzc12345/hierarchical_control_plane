import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
	public static void main(String[] argv) throws NumberFormatException, UnknownHostException, IOException{
		
		Socket socket = new Socket(argv[0], Integer.parseInt(argv[1]));
		PrintWriter pw = new PrintWriter(socket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println("#####################");
		System.out.println("#     HCP Client    #");
		System.out.println("#####################");
		System.out.print(">");
		Scanner scanner = new Scanner(System.in);
		String line = "";
		while(!(line=scanner.nextLine()).equals("exit")){
			if(line.equals("help")){
				System.out.println("Commands list:");
				System.out.println("\tadd gswitch <vport1>:<vport2>:...:<vportn> <switchId1>:<switchId2>:...:<switchIdn>");
				System.out.println("\tremove gswitch <gswitch_name>");
				System.out.println("\tremove host <host_name>");
				System.out.println("\tpacketin <gswitch_name> <vport> <src_mac> <src_ip>");
				System.out.println("\tgetvport <gswitch_name> ip <dest_ip>");
				System.out.println("\tdump");
				System.out.println("\tdrawtopology");
				System.out.println("\trestor <logfile>");
				System.out.println("\texit");
				System.out.println("\tshutdown");
			}else{
				pw.println(line);
				pw.flush();
				if(line.equals("shutdown"))
					break;
				System.out.println(br.readLine());
			}
			System.out.print(">");
		}
		
		pw.close();
		br.close();
		socket.close();
		scanner.close();
	}
}
