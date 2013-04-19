import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class TCPClient {
	public static void main(String[] args) {
		String hostname;
		int port = 2019;
		if (args.length > 0)
		    hostname = args[0];
		else
		    hostname = "localhost";
		try {
		    InetAddress ia = InetAddress.getByName(hostname);
		    ServerSocket datasocket;
			datasocket = new ServerSocket(port);
		    BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
		    while (true) {
		        try {
		        	Socket sSocket = new Socket(ia, port-1);
		        	PrintWriter pout = new PrintWriter(sSocket.getOutputStream());
		        	
		        	System.out.println("Enter command:");
		            String echoline = new String();
		            echoline = stdinp.readLine();
		            
		            if (echoline.equals("done")) break;
		            else if(echoline.equals("get")) Get(sSocket, "Username", "Filename");
		            else if(echoline.equals("put")) Put(sSocket, "Username", "Filename", "File contents");
		           
		            //pout.println(echoline);
		            System.out.println("Finished outputting");//debug line
		            
		            Socket rSocket = datasocket.accept();
		            BufferedReader din = new BufferedReader(new InputStreamReader(rSocket.getInputStream()));
		            String retstring = din.readLine();
		            System.out.println(retstring);
		            pout.flush();
		        } catch (IOException e) {
		            System.err.println(e);
		        }
		    } // while
		} catch (UnknownHostException e) {
		    System.err.println(e);
		} catch (SocketException se) {
		    System.err.println(se);
		}catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
	private static String Put(Socket s, String username, String filename, String file){
		//do the communication. assumption is that authentication is complete
		return "fail";
	}
	private static String Get(Socket s, String username, String filename){
		//do the communication. assumption is that authentication is complete
		return "fail";
	}
}
