import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class TCPServer {
	static final byte key[] = "hello".getBytes();
	
	public static void main(String[] args) {
        Socket rSocket;
        int port = 2018;
        try {
        	ServerSocket datasocket = new ServerSocket(port);
            while (true) {
                rSocket = datasocket.accept();
                System.out.println("Accepted socket");//debug line
        		try {
        	        BufferedReader din = new BufferedReader(new InputStreamReader(rSocket.getInputStream()));
                    System.out.println(din.readLine());//debug  line;
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                TCPRun t = new TCPRun(rSocket, key);
                System.out.println("created new TCPRun");//debug line
                t.start();
				t.join();
            
            }
        } catch (SocketException se) {
            System.err.println(se);
        }catch (IOException e) {
            System.err.println(e);
        }catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}